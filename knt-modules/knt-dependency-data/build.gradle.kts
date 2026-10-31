@file:Suppress("UnstableApiUsage")

plugins {
  id("conventions.kotlin-jvm")
  id("conventions.maven-publishing")
  id("dev.adamko.kntoolchain.tools.konan-dependencies-generator")
}

kotlin {
  sourceSets {
    main {
      generatedKotlin.srcDir(tasks.knpDataSync.flatMap { it.committedDataDir.dir("kotlin") })
      resources.srcDir(tasks.knpDataSync.flatMap { it.committedDataDir.dir("resources") })
    }
  }
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      dependencies {
        implementation(platform(libs.junit.bom))
        implementation(libs.junit.jupiter)
        runtimeOnly(libs.junit.platformLauncher)

        implementation(libs.kotest.assertions)

        implementation(libs.semver)

        // verifies that the generated data is valid TOML
        implementation(libs.ktoml.core)
      }
    }
  }
}

publishing {
  publications.register<MavenPublication>("maven") {
    from(components["java"])
  }
}
