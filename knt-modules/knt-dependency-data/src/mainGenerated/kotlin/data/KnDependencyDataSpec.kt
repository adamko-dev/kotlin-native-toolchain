package dev.adamko.kntoolchain.tools.data.content

import dev.adamko.kntoolchain.tools.data.*

/**
 * Data for a K/N compile target.
 */
sealed class KnDependencyDataSpec {

  abstract val version: KnpVersion

  abstract val buildPlatform: KnBuildPlatform

  abstract val compileTarget: KnCompileTarget

  abstract val dependencies: Set<KnpDependency>

  companion object
}
