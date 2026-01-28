import org.gradle.api.initialization.resolve.RepositoriesMode.PREFER_SETTINGS

rootProject.name = "buildSrc"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://central.sonatype.com/repository/maven-snapshots") {
      mavenContent { snapshotsOnly() }
    }
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(PREFER_SETTINGS)

  repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots") {
      mavenContent { snapshotsOnly() }
    }
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
