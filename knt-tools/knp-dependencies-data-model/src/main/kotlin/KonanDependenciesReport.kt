package dev.adamko.kntoolchain.tools.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class KonanDependenciesReport(
  val data: List<KotlinVersionTargetDependencies>,
)
