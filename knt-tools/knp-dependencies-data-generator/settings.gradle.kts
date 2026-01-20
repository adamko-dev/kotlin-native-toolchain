rootProject.name = "knp-dependencies-data-generator"

pluginManagement {
  includeBuild("../../build-tools/build-plugins/")
  includeBuild("../knp-dependencies-data-fetcher/")
  includeBuild("../knp-dependencies-data-model/")

  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }

  versionCatalogs {
    create("libs") {
      from(files("../../gradle/libs.versions.toml"))
    }
  }
}
