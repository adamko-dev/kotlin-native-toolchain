package dev.adamko.kntoolchain.tools.internal.utils

//import dev.adamko.kntoolchain.tools.datamodel.KonanDependenciesReport
//import kotlinx.serialization.json.Json
//
//@Deprecated("generated dynamically...")
//internal val konanDependenciesReportJson: String by lazy {
//  val path = "/dev/adamko/kn-toolchains/KonanDependenciesReport.json"
//  val resourceStream = object {}::class.java.getResourceAsStream(path)
//  requireNotNull(resourceStream) { "$path not found in classpath" }
//  resourceStream.use { it.readAllBytes().decodeToString() }
//}
//
//@Deprecated("generated dynamically...")
//internal val konanDependenciesReport: KonanDependenciesReport by lazy {
//  Json.decodeFromString(
//    KonanDependenciesReport.serializer(),
//    konanDependenciesReportJson,
//  )
//}
