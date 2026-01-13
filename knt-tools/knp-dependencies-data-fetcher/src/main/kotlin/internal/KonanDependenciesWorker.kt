package dev.adamko.kntoolchain.tools.internal

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import dev.adamko.kntoolchain.tools.datamodel.KonanDist
import dev.adamko.kntoolchain.tools.datamodel.KotlinVersionTargetDependencies
import dev.adamko.kntoolchain.tools.datamodel.internal.KonanTargetTriplet
import kotlinx.serialization.json.Json
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.internal.extensions.core.debug
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jetbrains.kotlin.konan.properties.KonanPropertiesLoader
import org.jetbrains.kotlin.konan.properties.loadProperties
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.loadConfigurables
import org.jetbrains.kotlin.konan.util.ArchiveExtractor
import org.jetbrains.kotlin.konan.util.ArchiveType
import org.jetbrains.kotlin.util.prefixIfNot
import java.io.File
import java.io.OutputStream
import java.io.OutputStream.nullOutputStream
import java.io.PrintStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import javax.inject.Inject
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.createTempDirectory
import kotlin.io.path.invariantSeparatorsPathString

/**
 * Determine the Konan dependencies required for each Kotlin Native target.
 *
 * Produces JSON [KotlinVersionTargetDependencies].
 *
 * Requires:
 * - `org.jetbrains.kotlin:kotlin-native-utils`
 * - `org.jetbrains.kotlin:kotlin-compiler-embeddable`
 */
internal abstract class KonanDependenciesWorker
@Inject
internal constructor() : WorkAction<KonanDependenciesWorker.Parameters> {

  internal interface Parameters : WorkParameters {
    /** The result file, containing JSON encoded [KotlinVersionTargetDependencies]. */
    val targetDependenciesReportFile: RegularFileProperty

    val distVersion: Property<String>
    val hostFamily: Property<String>
    val hostArch: Property<String>

    /** `konan.properties` file, extracted from a konan-native-prebuilt dist. */
    val konanPropertiesFile: RegularFileProperty
  }

  override fun execute() {
    val konanPropertiesFile = parameters.konanPropertiesFile.get().asFile.toPath()

    logger.lifecycle("Computing dependencies from ${konanPropertiesFile.invariantSeparatorsPathString}")

    val targetToUrls: Map<KonanTarget, Set<String>> = try {
      val konanProperties: Properties = loadKonanProperties(konanPropertiesFile.toFile())
      KonanTarget.predefinedTargets.values
        .filter { konanTarget ->
          // cannot compute dependencies if the current host does not this target
          HostManager().isEnabled(konanTarget)
        }
        .associateWith { konanTarget ->
          try {
            computeDependencyUrls(konanTarget, konanProperties)
          } catch (ex: Throwable) {
            throw RuntimeException("Error computing dependencies for $konanTarget", ex)
          }
        }
    } catch (ex: Throwable) {
      throw RuntimeException("Error processing $konanPropertiesFile", ex)
    }

    val data = KotlinVersionTargetDependencies(
      dist = KonanDist(
        version = parameters.distVersion.get(),
        hostFamily = parameters.hostFamily.get(),
        hostArch = parameters.hostArch.get(),
      ),
      dependencyCoords = targetToUrls
        .mapKeys { (target, _) ->
          KonanTargetTriplet.encode(target)
        }
        .mapValues { (_, urls) ->
          convertToDependencyCoords(urls)
        }
    )

    val jsonDataString = json.encodeToString(KotlinVersionTargetDependencies.serializer(), data)

    logger.debug { ("[KotlinVersionTargetDependenciesWorker] ${jsonDataString.replace('\n', ' ')}") }
    parameters.targetDependenciesReportFile.get().asFile.writeText(jsonDataString)
  }


  private fun loadKonanProperties(konanPropFile: File): Properties {
    val konanPropertiesFile = konanPropFile.invariantSeparatorsPath
    return loadProperties(konanPropertiesFile)
  }

  /**
   * Get the URLs of dependencies required by [konanTarget].
   *
   * The logic for computing the required dependencies is opaque and confusing.
   * kotlin-native-utils provides no way of just fetching the required dependencies.
   *
   * Instead, let's hack. Start a dummy local host server that returns a string
   * instead of the requested archive, and use a no-op archive extractor so
   * the string doesn't get treated as an archive.
   *
   * Use a callback to capture the requested URLs of the dependencies.
   */
  private fun computeDependencyUrls(
    konanTarget: KonanTarget,
    konanProperties: Properties,
  ): Set<String> {
    withHttpServer { server ->
      val serverAddress = "http://localhost:${server.address.port}"
      val overriddenKonanProperties =
        Properties(konanProperties).apply {
          put("dependenciesUrl", serverAddress)
        }

      val urls = getDependencyUrls(konanTarget, overriddenKonanProperties)

      val dependenciesUrl = konanProperties["dependenciesUrl"]?.toString()
        ?: error("dependenciesUrl missing in konan.properties")

      return urls
        .map { it.removePrefix(serverAddress) }
        .map { "$dependenciesUrl$it" }
        .toSet()
    }
  }

  private fun getDependencyUrls(
    konanTarget: KonanTarget,
    konanProperties: Properties,
  ): Set<String> {
    val urls = mutableSetOf<String>()
    val konanPropertiesLoader = loadConfigurables(
      target = konanTarget,
      properties = konanProperties,
      dependenciesRoot = createTempDirectory().invariantSeparatorsPathString,
      progressCallback = { url: String, _: Long, _: Long ->
        urls += url
      }
    ) as KonanPropertiesLoader

    val systemOut = System.out
    try {
      System.setOut(PrintStream(nullOutputStream()))
      konanPropertiesLoader.downloadDependencies(NoopArchiveExtractor)
    } finally {
      System.setOut(systemOut)
    }

    return urls
  }

  private object NoopArchiveExtractor : ArchiveExtractor {
    override fun extract(
      archive: File,
      targetDirectory: File,
      archiveType: ArchiveType,
    ) {
    }
  }

  private fun convertToDependencyCoords(
    urls: Set<String>,
  ): Set<KotlinVersionTargetDependencies.Coordinates> {
    val fileNameElementsRegex =
      Regex("""(?<fileId>.+)-(?<version>[\d.]+)(?:-(?<classifier>.+?))?\.(?<extension>[a-zA-Z0-9.]+)""")

    return urls.map { url ->
      val uri = URI(url)

      val fileName = uri.path.substringAfterLast("/", "")
      val match = fileNameElementsRegex.matchEntire(fileName)
      check(match != null) { "Invalid dependency file name: $fileName" }

      val fileId = match.groups["fileId"]?.value ?: error("artifact missing from $fileName")
      val version = match.groups["version"]?.value ?: error("version missing from $fileName")
      val classifier = match.groups["classifier"]?.value
      val extension = match.groups["extension"]?.value ?: error("extension missing from $fileName")

      val groupPath = uri.path.removePrefix("/")
        .substringBeforeLast("/", "")

      val groupPathElements = groupPath.split("/")

      val group: String
      val module: String
      val artifact: String?
      if ("." in groupPathElements.lastOrNull().orEmpty()) {
        group = groupPathElements.dropLast(1).joinToString(".")
        module = groupPathElements.last()
        artifact = fileId
      } else {
        group = groupPathElements.joinToString(".")
        module = fileId
        artifact = null
      }

      KotlinVersionTargetDependencies.Coordinates(
        group = group,
        module = module,
        version = version,
        extension = extension,
        classifier = classifier,
        artifact = artifact,
        originalUrl = url,
      )
    }
      .toSet()
  }

  companion object {
    private val logger: Logger = Logging.getLogger(KonanDependenciesWorker::class.java)

    private val json = Json {
      prettyPrint = true
      prettyPrintIndent = "  "
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun <T> withHttpServer(
      block: (server: HttpServer) -> T
    ): T {
      contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
      }
      val server = HttpServer.create(
        InetSocketAddress(InetAddress.getLoopbackAddress(), 0),
        0,
      )

      // respond with a mock 200 response
      val requestHandler = HttpHandler { exchange ->
        val mockResponse = "Mock response"
        exchange.sendResponseHeaders(200, mockResponse.toByteArray().size.toLong())
        exchange.responseBody.use { os: OutputStream ->
          os.write(mockResponse.toByteArray())
        }
      }

      // root context, handle all requests
      server.createContext("/", requestHandler)

      try {
        server.start()

        if (!waitForHttpOkResponse(server.address)) {
          error("Failed to start server $server")
        }

        return block(server)
      } finally {
        server.stop(0)
      }
    }
  }
}

private fun waitForHttpOkResponse(
  serverAddress: InetSocketAddress,
  path: String = "/foo",
  retries: Int = 10,
): Boolean {
  require(retries >= 0) { "retries must be >= 0, but was $retries" }

  val host = serverAddress.hostString?.takeIf { it.isNotBlank() } ?: "127.0.0.1"
  val port = serverAddress.port

  @Suppress("HttpUrlsUsage")
  val uri = URI.create("http://$host:$port${path.prefixIfNot("/")}")

  val request = HttpRequest.newBuilder(uri)
    .timeout(Duration.ofMillis(250))
    .GET()
    .build()

  HttpClient.newBuilder()
    .connectTimeout(Duration.ofMillis(250))
    .build().let { client ->

      var attempt = 0
      while (attempt++ <= retries) {
        try {
          val response = client.send(request, HttpResponse.BodyHandlers.discarding())
          val isResponseOk = response.statusCode() in 200..299
          if (isResponseOk) {
            return true
          }
        } catch (_: Exception) {
          // ignore and retry
        }

        Thread.sleep(Duration.ofMillis(100).toMillis())
      }

      return false
    }
}
