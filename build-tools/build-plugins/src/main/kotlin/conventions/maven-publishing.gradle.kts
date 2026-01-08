package conventions

import ext.skipPublishingTestFixtures

plugins {
  id("conventions.base")
  `maven-publish`
  id("dev.adamko.dev-publish")
}

publishing {
  publications.withType<MavenPublication>().configureEach {
    versionMapping {
      usage("java-api") {
        fromResolutionOf("runtimeClasspath")
      }
      usage("java-runtime") {
        fromResolutionResult()
      }
    }
  }
}

pluginManager.withPlugin("java-test-fixtures") {
  skipPublishingTestFixtures()
}
