package dev.adamko.kntoolchain.tools.data.content

import dev.adamko.kntoolchain.tools.data.*

/**
 * Details the compilation tools required for a specific host machine and compilation target.
 */
sealed class KnDependencyDataSpec {

  /** The version of the kotlin-native-prebuilt distribution. */
  abstract val version: KnpVersion

  /** The platform on which the compilation tools are executed. */
  abstract val buildPlatform: KnBuildPlatform

  /** The Kotlin Native target platform that the compiler will generate code for. */
  abstract val compileTarget: KnCompileTarget

  /** The required ancillary dependencies for the kotlin-native-prebuilt distribution. */
  abstract val dependencies: Set<KnpDependency>

  companion object
}
