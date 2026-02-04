package dev.adamko.kntoolchain.tools.internal

import dev.adamko.kntoolchain.tools.internal.datamodel.KotlinNativePrebuiltData
import dev.adamko.kntoolchain.tools.internal.utils.md5ChecksumOrNull
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

/**
 * Create a [KotlinNativePrebuiltData] file, containing all Kotlin Native prebuilt variants
 * for all Kotlin versions.
 *
 * Downloads a list of all Kotlin versions from Maven Central.
 */
internal abstract class KotlinNativePrebuiltDataSource :
  ValueSource<KotlinNativePrebuiltData, KotlinNativePrebuiltDataSource.Parameters> {

  interface Parameters : ValueSourceParameters {
    /**
     * The generated `KotlinNativePrebuiltVariants.json` file.
     */
    val knpVariantsDataFile: RegularFileProperty
    val regenerateData: Property<Boolean>
    val workDir: DirectoryProperty
  }

  private val knpVariantsDataFile: Path
    get() = parameters.knpVariantsDataFile.get().asFile.toPath()

  /**
   * Locally cache kotlin-stdlib maven metadata file.
   * @see allKotlinVersions
   * @see downloadKotlinStdlibMavenMetadata
   */
  private val kotlinStdlibMavenMetadataFile: Path
    get() = parameters.workDir.get().asFile.toPath()
      .createDirectories()
      .resolve("kotlin-stdlib.maven-metadata.xml")

  override fun obtain(): KotlinNativePrebuiltData {
    val knpVariantsDataFile = parameters.knpVariantsDataFile.get().asFile.toPath()

    if (!knpVariantsDataFile.exists() || parameters.regenerateData.get()) {
      // for each Kotlin version get the associated kotlin-native-prebuilt variants
      val mapVersionToVariants: Map<KotlinToolingVersion, Set<KotlinNativePrebuiltData.PrebuiltVariant>> =
        allKotlinVersions().associateWith { version ->
          getKotlinNativePrebuiltVariants(version)
        }
      val knpData = KotlinNativePrebuiltData(mapVersionToVariants)
      storeKnpVariantsData(knpData)
    }

    return loadKnpVariantsData()
  }

  private fun loadKnpVariantsData(): KotlinNativePrebuiltData {
    return try {
      knpVariantsDataFile.inputStream().use { source ->
        json.decodeFromStream(KotlinNativePrebuiltData.serializer(), source)
      }
    } catch (ex: SerializationException) {
      throw RuntimeException(
        "Failed to decode KotlinNativePrebuiltData from ${knpVariantsDataFile.invariantSeparatorsPathString}",
        ex,
      )
    }
  }

  private fun storeKnpVariantsData(knpData: KotlinNativePrebuiltData) {
    knpVariantsDataFile.parent.createDirectories()
    knpVariantsDataFile.outputStream().use { sink ->
      json.encodeToStream(KotlinNativePrebuiltData.serializer(), knpData, sink)
    }
  }

  /**
   * Returns all known versions of Kotlin.
   *
   * Extracts the data from the `kotlin-stdlib` Maven metadata file
   * from Maven Central.
   *
   * @see downloadKotlinStdlibMavenMetadata
   */
  private fun allKotlinVersions(): Set<KotlinToolingVersion> {
    downloadKotlinStdlibMavenMetadata(kotlinStdlibMavenMetadataFile)

    val pomVersions: Sequence<KotlinToolingVersion> =
      kotlinStdlibMavenMetadataFile
        .readText()
        .substringAfter("<versions>", "")
        .substringBefore("</versions>", "")
        .splitToSequence("<version>", "</version>")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map(::KotlinToolingVersion)

    return pomVersions
      // Versions prio to 2.0.0 did not host Konan on Maven Central, skip for simplicity of downloading.
      .filter { it >= KotlinToolingVersion("2.0.0") }

      // Max by version, so Beta/RC releases are only considered if there's no later, stable release.
      .groupingBy { v -> v.run { "$major:$minor:$patch" } }
      .reduce { _, v1, v2 ->
        maxOf(v1, v2)
      }
      .values

      .toSet()
  }

  companion object
}


/**
 * Downloads `kotlin-stdlib/maven-metadata.xml` from Maven Central.
 *
 * Uses ETag for download avoidance.
 * (This assumes that the ETag of a `maven-metadata.xml` is the md5 checksum of the file.)
 */
private fun downloadKotlinStdlibMavenMetadata(
  destination: Path,
) {
  val fileChecksum = destination.md5ChecksumOrNull()

  val request =
    HttpRequest.newBuilder().apply {
      uri(URI("https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/maven-metadata.xml"))
      timeout(Duration.ofSeconds(30))
      GET()
      fileChecksum?.let {
        header("If-None-Match", it)
      }
    }.build()

  val response = HttpClient.newHttpClient().use { httpClient ->
    httpClient
      .send(request, HttpResponse.BodyHandlers.ofFile(destination))
  }

  if (response.statusCode() !in setOf(200, 304)) {
    error("Failed to download file: HTTP ${response.statusCode()}")
  }

  val responseEtag = response.headers().firstValue("ETag")
    .orElseGet { null }
    ?.removeSurrounding("\"")

  // This code assumes the etag of a maven-metadata.xml is the md5 checksum of the file.
  // Here it immediately verifies the etag matches the checksum,
  // just in case Sonatype will change the behaviour in the future.
  check(responseEtag?.ifBlank { null } != null) {
    "Expected response to have an ETag header, but it was $responseEtag."
  }
  val updatedChecksum = destination.md5ChecksumOrNull()
  check(responseEtag == updatedChecksum) {
    "Expected saved file had checksum $responseEtag, but was $updatedChecksum (original checksum: $fileChecksum)"
  }
}
