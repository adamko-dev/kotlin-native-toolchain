package dev.adamko.kntoolchain.tools.datamodel

import dev.adamko.kntoolchain.tools.datamodel.internal.KonanTargetTriplet
import kotlinx.serialization.Serializable

@Serializable
data class KotlinVersionTargetDependencies(
  val dist: KonanDist,
  val dependencyCoords: Map<KonanTargetTriplet, Set<Coordinates>>,
) {
  @Serializable
  data class Coordinates(
    val group: String,
    val module: String,
    val version: String,
    val extension: String,
    val classifier: String?,
    val artifact: String?,
    val originalUrl: String,
  )
}
