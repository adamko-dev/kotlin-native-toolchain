package dev.adamko.kntoolchain.tools.internal

import dev.adamko.kntoolchain.tools.internal.datamodel.KotlinNativePrebuiltData
import dev.adamko.kntoolchain.tools.internal.datamodel.KotlinNativePrebuiltData.ArchiveType
import groovy.json.JsonSlurper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

/**
 * Fetches all known variants of a kotlin-native-prebuilt artifact, published to Maven Central.
 */
internal fun getKotlinNativePrebuiltVariants(
  kotlinVersion: KotlinToolingVersion,
): Set<KotlinNativePrebuiltData.PrebuiltVariant> {
  return try {
    getKotlinNativePrebuiltVariantsHtml(kotlinVersion)
  } catch (_: Exception) {
    getKotlinNativePrebuiltVariantsFromSearch(kotlinVersion)
  }
}

/**
 * Scrape the variants from the HTML repo index.
 */
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

private fun downloadKnpVariantsDataUsingRepoIndex(
  kotlinVersion: String
): String {
  val request =
    HttpRequest.newBuilder().apply {
      uri(URI("https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-native-prebuilt/${kotlinVersion}/"))
      timeout(Duration.ofSeconds(30))
      GET()
    }.build()

  HttpClient.newHttpClient().use { httpClient ->
    return httpClient
      .send(request, HttpResponse.BodyHandlers.ofString())
      .body()
  }
}

/**
 * Download variants using Maven search API.
 *
 * But the search server is slow/buggy/unreliable
 * (~95% uptime https://status.maven.org/),
 * so prefer the repo HTML index [getKotlinNativePrebuiltVariantsHtml].
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

  HttpClient.newHttpClient().use { httpClient ->
    return httpClient
      .send(request, HttpResponse.BodyHandlers.ofString())
      .body()
  }
}
