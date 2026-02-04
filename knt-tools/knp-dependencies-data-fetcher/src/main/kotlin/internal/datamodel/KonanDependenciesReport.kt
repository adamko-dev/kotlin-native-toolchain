package dev.adamko.kntoolchain.tools.internal.datamodel

import kotlinx.serialization.Serializable

@Serializable
internal data class KonanDependenciesReport(
  val data: List<KotlinVersionTargetDependencies>,
)
