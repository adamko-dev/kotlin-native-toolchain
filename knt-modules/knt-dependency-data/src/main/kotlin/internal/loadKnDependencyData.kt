package dev.adamko.kntoolchain.tools.data.internal

import dev.adamko.kntoolchain.tools.data.KnBuildPlatform
import dev.adamko.kntoolchain.tools.data.KnDependencyDataSpec
import dev.adamko.kntoolchain.tools.data.KnpVersion
import java.util.concurrent.ConcurrentHashMap

/**
 * Load the [KnDependencyDataSpec]s for [version], grouped by build platform.
 *
 * The data is cached, so each version is only loaded once.
 */
internal fun loadKnDependencyData(version: KnpVersion): Map<KnBuildPlatform, Set<KnDependencyDataSpec>> =
  KnDependencyDataCache.computeIfAbsent(version, ::loadKnDependencyDataSpecs)


private fun loadKnDependencyDataSpecs(version: KnpVersion): Map<KnBuildPlatform, MutableSet<KnDependencyDataSpec>> {
  val resourceName = "knpdeps-${version.version}.toml"
  val parsed = parseKnDependencyData(resourceName, readResource(resourceName))
  if (parsed.kotlinVersion != version.version) {
    error("Invalid $resourceName: expected Kotlin version '${version.version}', but was '${parsed.kotlinVersion}'")
  }
  return parsed.dependencies.mapValues { (buildPlatform, compileTargets) ->
    compileTargets.mapTo(mutableSetOf()) { (compileTarget, dependencies) ->
      KnDependencyDataSpec(version, buildPlatform, compileTarget, dependencies)
    }
  }
}

private val KnDependencyDataCache: MutableMap<KnpVersion, Map<KnBuildPlatform, Set<KnDependencyDataSpec>>> =
  ConcurrentHashMap()

private fun readResource(resourceName: String): String {
  val resourceStream = KnDependencyDataSpec::class.java.getResourceAsStream(resourceName)
    ?: error("Missing resource $resourceName")
  return resourceStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
}
