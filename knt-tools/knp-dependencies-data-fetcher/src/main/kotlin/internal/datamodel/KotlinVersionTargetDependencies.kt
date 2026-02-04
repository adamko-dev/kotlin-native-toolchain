package dev.adamko.kntoolchain.tools.internal.datamodel

import kotlinx.serialization.Serializable

@Serializable
internal data class KotlinVersionTargetDependencies(
  val version: String,
  /** The platform on which the compilation tools are executed. */
  val buildPlatform: Platform,
  /** The platform that the compiler will generate code for. */
  val targetPlatform: KonanTargetData,
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
