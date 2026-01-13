@file:OptIn(ExperimentalSerializationApi::class)

package dev.adamko.kntoolchain.tools.internal

import dev.adamko.kntoolchain.tools.datamodel.KotlinNativePrebuiltData
import dev.adamko.kntoolchain.tools.datamodel.KotlinNativePrebuiltData.ArchiveType
import groovy.json.JsonSlurper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

/**
 * Downloads [KotlinNativePrebuiltData].
 *
 * Caches the downloaded file. Does not download if Gradle is offline.
 */
// This could probably be adapted to download other files,
// but it makes an assumption about the etag.
internal abstract class KotlinNativePrebuiltVariantsSource
internal constructor() : ValueSource<KotlinNativePrebuiltData, KotlinNativePrebuiltVariantsSource.Parameters> {

  interface Parameters : ValueSourceParameters {
    /** Output file. */
    val stateDir: DirectoryProperty

    /** The value of [org.gradle.StartParameter.isOffline]. */
    val gradleOffline: Property<Boolean>

    val kotlinVersions: SetProperty<KotlinToolingVersion>
  }

  private val dataFile: Path
    get() = stateDir.resolve("KotlinNativePrebuiltVariants.json")

  private val stateDir: Path
    get() = checkNotNull(parameters.stateDir.orNull?.asFile?.toPath()?.createDirectories()) {
      "No value set for stateDir"
    }

  override fun obtain(): KotlinNativePrebuiltData {
    if (parameters.gradleOffline.orNull != true) {
      updateData()
    }
    return loadData()
  }

  private fun loadData(): KotlinNativePrebuiltData {
    if (!dataFile.exists()) {
      dataFile.parent.createDirectories()

      val emptyData = KotlinNativePrebuiltData(emptyMap())
      dataFile.outputStream().use { sink ->
        json.encodeToStream(KotlinNativePrebuiltData.serializer(), emptyData, sink)
      }
    }

    return dataFile.inputStream().use { src ->
      json.decodeFromStream(KotlinNativePrebuiltData.serializer(), src)
    }
  }

  private fun updateData() {
    val data = loadData()
    val kotlinVersions = parameters.kotlinVersions.get()

    check((data.data.keys subtract kotlinVersions).isEmpty()) {
      // sanity check to make sure Kotlin versions were correctly downloaded,
      // and make sure the data file doesn't have old versions that `getKotlinVersions()` filters out.
      "Unexpected Kotlin versions in stored data.\n\tkotlinVersions:$kotlinVersions\n\tdata:${data.data.keys}"
    }

    val storedKotlinVersions = data.data.filterValues { it.isNotEmpty() }.keys

    // need to update KNP data for versions that are not in the data file
    val versionsToUpdate = kotlinVersions subtract storedKotlinVersions

    val newData = versionsToUpdate.associateWith { version ->
      val variants = getKotlinNativePrebuiltVariants(version)
      check(variants.isNotEmpty()) {
        "No variants found for Kotlin version $version"
      }
      logger.lifecycle("Updating Kotlin Native Prebuilt data for $version: $variants")
      variants
    }

    val updatedData = data.copy(
      data = data.data + newData
    )

    dataFile.outputStream().use { sink ->
      json.encodeToStream(KotlinNativePrebuiltData.serializer(), updatedData, sink)
    }
  }

  private fun getKotlinNativePrebuiltVariants(
    kotlinVersion: KotlinToolingVersion,
  ): Set<KotlinNativePrebuiltData.PrebuiltVariant> {
    return try {
      getKotlinNativePrebuiltVariantsHtml(kotlinVersion)
    } catch (_: Exception) {
      getKotlinNativePrebuiltVariantsFromSearch(kotlinVersion)
    }
  }

  /**
   * Download variants using Maven search API.
   *
   * But the search server is slow/buggy/unreliable (95% uptime),
   * so prefer the repo index [getKotlinNativePrebuiltVariantsHtml].
   */
  private fun getKotlinNativePrebuiltVariantsFromSearch(
    kotlinVersion: KotlinToolingVersion,
  ): Set<KotlinNativePrebuiltData.PrebuiltVariant> {
    val variantsJson = downloadKotlinNativePrebuiltVariantsUsingSearch(kotlinVersion.toString())

    try {
      return JsonSlurper().parseText(variantsJson)
        .let { it as Map<*, *> }
        .let { it["response"] as Map<*, *> }
        .let { it["docs"] as List<*> }
        .asSequence()
        .map { it as Map<*, *> }
        .flatMap { it["ec"] as List<*> }
        .map { it as String }
        .mapNotNull map@{
          val archiveType = ArchiveType.fromFileNameOrNull(it)
            ?: return@map null

          val classifier = it
            .removePrefix("-")
            .removeSuffix(archiveType.fileExtension)

          KotlinNativePrebuiltData.PrebuiltVariant(
            classifier = classifier,
            archiveType = archiveType,
          )
        }
        .toSet()
    } catch (e: Exception) {
      System.err.println("Error parsing JSON: $variantsJson")
      throw e
    }
  }

  private fun getKotlinNativePrebuiltVariantsHtml(
    kotlinVersion: KotlinToolingVersion,
  ): Set<KotlinNativePrebuiltData.PrebuiltVariant> {
    val variantsHtml = downloadKnpVariantsDataUsingRepoIndex(kotlinVersion.toString())

    return variantsHtml
      .substringAfter("<main>")
      .substringBefore("</main>")
      .split("</a>")
      .map { a ->
        a.substringAfter("<a href=\"kotlin-native-prebuilt-$kotlinVersion")
          .substringBefore("\"")
      }
      .mapNotNull map@{ a ->
        val archiveType = ArchiveType.fromFileNameOrNull(a)
          ?: return@map null

        val classifier = a
          .removePrefix("-")
          .removeSuffix(archiveType.fileExtension)

        KotlinNativePrebuiltData.PrebuiltVariant(
          classifier = classifier,
          archiveType = archiveType,
        )
      }
      .toSet()
  }

  companion object {
    private val logger: Logger = Logging.getLogger(KotlinNativePrebuiltVariantsSource::class.java)

    private val json = Json {
      prettyPrint = true
      prettyPrintIndent = "  "
    }

    /**
     * Query Maven Central for the available kotlin-native-prebuilt variants.
     */
    private fun downloadKotlinNativePrebuiltVariantsUsingSearch(
      kotlinVersion: String,
    ): String {
      val request =
        HttpRequest.newBuilder().apply {
          uri(URI("https://search.maven.org/solrsearch/select?q=g:org.jetbrains.kotlin+AND+a:kotlin-native-prebuilt+AND+v:${kotlinVersion}&rows=20&wt=json"))
          timeout(Duration.ofSeconds(30))
          GET()
        }.build()

      HttpClient.newHttpClient().let { httpClient ->
        return httpClient
          .send(request, HttpResponse.BodyHandlers.ofString())
          .body()
      }
    }

    private fun downloadKnpVariantsDataUsingRepoIndex(
      kotlinVersion: String
    ): String {
      val request =
        HttpRequest.newBuilder().apply {
          uri(URI("https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-native-prebuilt/${kotlinVersion}/"))
          timeout(Duration.ofSeconds(30))
          GET()
        }.build()

      HttpClient.newHttpClient().let { httpClient ->
        return httpClient
          .send(request, HttpResponse.BodyHandlers.ofString())
          .body()
      }
    }
  }
}
