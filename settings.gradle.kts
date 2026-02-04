import org.gradle.api.initialization.resolve.RepositoriesMode.PREFER_SETTINGS

rootProject.name = "kotlin-native-toolchain"

pluginManagement {
  includeBuild("./build-tools/build-plugins/")
  includeBuild("./build-tools/settings-plugins/")
  includeBuild("./build-tools/knp-dependencies-data/")
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(PREFER_SETTINGS)

  repositories {
    mavenCentral()
  }
}

plugins {
  id("settings.conventions.git-versioning")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":knt-modules:knt-gradle-plugin")
include(":knt-modules:knt-dependency-data")
