plugins {
  id("dev.adamko.kntoolchain.tools.konan-dependencies-data-fetcher")
  id("conventions.kotlin-gradle-plugin")
}

dependencies {
  implementation("dev.adamko.kotlin-native-toolchain:knp-dependencies-data-model")

  implementation(platform(libs.kotlinxSerialization.bom))
  implementation(libs.kotlinxSerialization.json)
}

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
