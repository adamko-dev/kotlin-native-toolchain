package dev.adamko.kntoolchain.tools.internal

import dev.adamko.kntoolchain.tools.utils.md5ChecksumOrNull
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

/**
 * Downloads all Kotlin Versions from Maven Central.
 *
 * Caches the downloaded file.
 */
internal abstract class KotlinVersionsDataSource
internal constructor() : ValueSource<Set<KotlinToolingVersion>, KotlinVersionsDataSource.Parameters> {

  interface Parameters : ValueSourceParameters {
    /** Cache directory. */
    val stateDir: DirectoryProperty

    val currentKotlinVersion: Property<String>
  }

  private val stateDir: Path
    get() = checkNotNull(parameters.stateDir.orNull?.asFile?.toPath()?.createDirectories()) {
      "No value set for stateDir"
    }

  override fun obtain(): Set<KotlinToolingVersion> {
    val kotlinStdlibMavenMetadataFile = loadKotlinStdlibMavenMetadataFile()

    val pomVersions: Sequence<KotlinToolingVersion> =
      extractKotlinVersions(kotlinStdlibMavenMetadataFile)

    return pomVersions
      // Versions prio to 2.0.0 did not host Konan on Maven Central, skip for simplicity of downloading.
      .filter { it >= KotlinToolingVersion("2.0.0") }

      // Max by version, so Beta/RC releases are only considered if there's no subsequent release.
      .groupingBy { v -> v.run { "$major:$minor:$patch" } }
      .reduce { _, v1, v2 ->
        maxOf(v1, v2)
      }
      .values

      .toSet()
  }

  private fun loadKotlinStdlibMavenMetadataFile(): Path {
    val metadataFile: Path = stateDir.resolve("kotlin-stdlib.maven-metadata.xml")

    require(metadataFile.exists()) {
      "Required metadata file $metadataFile does not exist"
    }

    val pomVersions = extractKotlinVersions(metadataFile)

    if (pomVersions.any { it.toString() == parameters.currentKotlinVersion.get() }) {
      logger.info("[KotlinVersionsDataSource] Current Kotlin version is already downloaded, skipping download.")
      return metadataFile
    }

    downloadKotlinStdlibMavenMetadata(metadataFile)

    check(metadataFile.exists()) {
      "${metadataFile.name} file does not exist after download"
    }

    return metadataFile
  }

  private fun extractKotlinVersions(file: Path): Sequence<KotlinToolingVersion> {
    return file
      .readText()
      .substringAfter("<versions>", "")
      .substringBefore("</versions>", "")
      .splitToSequence("<version>", "</version>")
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .map(::KotlinToolingVersion)
  }

  companion object {
    private val logger: Logger = Logging.getLogger(KotlinVersionsDataSource::class.java)

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
      // just in case Sonatype changes the behaviour in the future.
      check(responseEtag?.ifBlank { null } != null) {
        "Expected response to have an ETag header, but it was $responseEtag."
      }
      val updatedChecksum = destination.md5ChecksumOrNull()
      check(responseEtag == updatedChecksum) {
        "Expected saved file had checksum $responseEtag, but was $updatedChecksum (original checksum: $fileChecksum)"
      }
    }
  }
}
