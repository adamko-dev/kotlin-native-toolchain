@file:Suppress("UnstableApiUsage")

plugins {
  id("conventions.kotlin-jvm")
  id("conventions.maven-publishing")
  id("dev.adamko.kntoolchain.tools.konan-dependencies-generator")
}

kotlin {
  sourceSets.main {
    kotlin.srcDir(tasks.knpDataSync)
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
      }
    }
  }
}

publishing {
  publications.register<MavenPublication>("maven") {
    from(components["java"])
  }
}
