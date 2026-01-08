package dev.adamko.kntoolchain.tools.datamodel

import kotlinx.serialization.Serializable

@Serializable
data class KonanDist(
  val version: String,
  val hostFamily: String,
  val hostArch: String,
)
