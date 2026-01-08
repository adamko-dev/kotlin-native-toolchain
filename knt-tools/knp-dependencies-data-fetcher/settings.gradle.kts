rootProject.name = "knp-dependencies-data-fetcher"

pluginManagement {
  includeBuild("../../build-tools/build-plugins/")
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

includeBuild("../knp-dependencies-data-model/")
