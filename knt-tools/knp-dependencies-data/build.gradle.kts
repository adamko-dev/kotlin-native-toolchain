@file:Suppress("UnstableApiUsage")

plugins {
//  id("conventions.kotlin-jvm") apply false
//  id("conventions.maven-publishing")
//  id("conventions.kotlin-gradle-plugin")
  id("dev.adamko.kntoolchain.tools.konan-dependencies-data-fetcher")
  id("conventions.kotlin-gradle-plugin")
}


//val knpDependenciesDataModelCoords: Provider<String> = gitVersion.map { version ->
//  "dev.adamko.kotlin-native-toolchain:knp-dependencies-data-model:$version"
//}

dependencies {
  implementation("dev.adamko.kotlin-native-toolchain:knp-dependencies-data-model")

  implementation(platform(libs.kotlinxSerialization.bom))
  implementation(libs.kotlinxSerialization.json)
}

//val konanDependenciesReportConsumable: NamedDomainObjectProvider<ConsumableConfiguration> =
//  configurations.consumable("konanDependenciesReportConsumable") {
//    attributes {
//      attribute(Usage.USAGE_ATTRIBUTE, objects.named("konan-dependencies-report"))
//    }
//    outgoing {
//      artifact(tasks.konanDependenciesReport)
//    }
//  }

val prepareKonanDependenciesReport by tasks.registering(Sync::class) {
  from(tasks.konanDependenciesReport) {
    into("/dev/adamko/kn-toolchains")
  }
  into(temporaryDir)
}
kotlin.sourceSets.main {
  resources.srcDir(prepareKonanDependenciesReport)
}

gradlePlugin {
  plugins.register("knp-dependencies-data") {
    id = "dev.adamko.knp.KnpDataGenPlugin"
    implementationClass = "dev.adamko.knp.KnpDataGenPlugin"
  }
}
