package dev.adamko.knp.internal.utils

import dev.adamko.kntoolchain.tools.datamodel.KonanDependenciesReport
import kotlinx.serialization.json.Json

internal val konanDependenciesReportJson: String by lazy {
  val path = "/dev/adamko/kn-toolchains/KonanDependenciesReport.json"
  val resourceStream = object {}::class.java.getResourceAsStream(path)
  requireNotNull(resourceStream) { "$path not found in classpath" }
  resourceStream.use { it.readAllBytes().decodeToString() }
}

internal val konanDependenciesReport: KonanDependenciesReport by lazy {
  Json.decodeFromString(
    KonanDependenciesReport.serializer(),
    konanDependenciesReportJson,
  )
}
