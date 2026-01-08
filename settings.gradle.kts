import org.gradle.api.initialization.resolve.RepositoriesMode.PREFER_SETTINGS

rootProject.name = "kotlin-native-toolchain"

pluginManagement {
  includeBuild("./build-tools/build-plugins/")
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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("./knt-tools/knp-dependencies-data/")
includeBuild("./knt-tools/knp-dependencies-data-model/")

include(":knt-modules:knt-gradle-plugin")
