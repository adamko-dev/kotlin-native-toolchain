package dev.adamko.kntoolchain.tools.data

import dev.adamko.kntoolchain.tools.data.internal.loadKnDependencyData
import java.io.Serializable as JavaIoSerializable

/**
 * Details the compilation tools required for a specific host machine and compilation target.
 */
data class KnDependencyDataSpec internal constructor(
  /** The version of the kotlin-native-prebuilt distribution. */
  val version: KnpVersion,
  /** The platform on which the compilation tools are executed. */
  val buildPlatform: KnBuildPlatform,
  /** The Kotlin Native target platform that the compiler will generate code for. */
  val compileTarget: KnCompileTarget,
  /** The required ancillary dependencies for the kotlin-native-prebuilt distribution. */
  val dependencies: Set<KnpDependency>,
) : JavaIoSerializable

/**
 * All known [KnDependencyDataSpec] instances, for all versions.
 *
 * @see knDependencyDataFor
 */
val knDependencyData: Set<KnDependencyDataSpec> by lazy {
  KnpVersion.allVersions.flatMapTo(mutableSetOf()) { knDependencyDataFor(it) }
}

/**
 * All known [KnDependencyDataSpec] instances for a specific [version], for all build platforms.
 *
 * Only the data for [version] is loaded.
 */
fun knDependencyDataFor(version: KnpVersion): Set<KnDependencyDataSpec> =
  loadKnDependencyData(version).values.flatMapTo(mutableSetOf()) { it }

/**
 * All known [KnDependencyDataSpec] instances for a specific [version] and [buildPlatform].
 *
 * Only the data for [version] is loaded.
 * Returns an empty set if there is no data for [buildPlatform].
 */
fun knDependencyDataFor(version: KnpVersion, buildPlatform: KnBuildPlatform): Set<KnDependencyDataSpec> =
  loadKnDependencyData(version)[buildPlatform].orEmpty()
