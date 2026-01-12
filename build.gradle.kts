import ext.excludeProjectConfigurationDirs

plugins {
  id("conventions.base")
  idea
  alias(libs.plugins.nmcp.aggregation)
}

idea {
  module {
    excludeProjectConfigurationDirs(layout, providers)
  }
}

tasks.updateDaemonJvm {
  languageVersion = JavaLanguageVersion.of(21)
}

nmcpAggregation {
  centralPortal {
    username = providers.gradleProperty("dev.adamko.kxstsgen.mavenCentralUsername")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_USERNAME"))
    password = providers.gradleProperty("dev.adamko.kxstsgen.mavenCentralPassword")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_PASSWORD"))

    // publish manually from the portal
    publishingType = "USER_MANAGED"
  }
}

dependencies {
  nmcpAggregation(projects.kntModules.kntGradlePlugin)
  nmcpAggregation("dev.adamko.kotlin-native-toolchain:knp-dependencies-data-model")
}

val isReleaseVersion: Provider<Boolean> =
  providers.provider { !project.version.toString().endsWith("-SNAPSHOT") }

tasks.nmcpPublishAggregationToCentralPortal {
  val isReleaseVersion = isReleaseVersion
  onlyIf("is release version") { _ -> isReleaseVersion.get() }
}

tasks.nmcpPublishAggregationToCentralPortalSnapshots {
  val isReleaseVersion = isReleaseVersion
  onlyIf("is snapshot version") { _ -> !isReleaseVersion.get() }
}

tasks.register("nmcpPublish") {
  group = PublishingPlugin.PUBLISH_TASK_GROUP
  dependsOn(tasks.nmcpPublishAggregationToCentralPortal)
  dependsOn(tasks.nmcpPublishAggregationToCentralPortalSnapshots)
}
