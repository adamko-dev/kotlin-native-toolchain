package dev.adamko.kntoolchain.tools.internal.datamodel

import kotlinx.serialization.Serializable

@Serializable
internal data class KotlinVersionTargetDependencies(
  val version: String,
  /** The platform on which the compilation tools are executed. */
  val buildPlatform: Platform,
  /** The platform that the compiler will generate code for. */
  val targetPlatform: KonanTargetData,
  // TODO is there a way to detect if the dependencies don't change per version?
  //      E.g. for 2.0.0-2.4.0 the deps for linux don't change.
  //      So dependencies could be an array, or a reference to another entry.
  //      `dependencies : "@linux_arm64:LINUX:ARM64:2.0.0"`
  //      `dependencies : [ ... ]`
  val dependencies: Set<Coordinates>,
) {
  @Serializable(with = CoordinatesSerializer::class)
  data class Coordinates(
    val group: String,
    val module: String,
    val version: String,
    val extension: String,
    val classifier: String?,
    val artifact: String? = null,
  ) {
    fun coords(): String {
      return buildString {
        append(group)
        append(":")
        append(module)
        append(":")
        append(version)
        if (classifier != null) {
          append(":")
          append(classifier)
        }
        append("@")
        append(extension)
      }
    }
  }
}
