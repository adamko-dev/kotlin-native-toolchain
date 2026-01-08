@file:Suppress("UnstableApiUsage")

import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE

plugins {
  id("conventions.kotlin-jvm") apply false
  id("conventions.maven-publishing")
  id("dev.adamko.kntoolchain.tools.konan-dependencies-data-fetcher")
}

val konanDependenciesReportConsumable: NamedDomainObjectProvider<ConsumableConfiguration> =
  configurations.consumable("konanDependenciesReportConsumable") {
    attributes {
      attribute(USAGE_ATTRIBUTE, objects.named("konan-dependencies-report"))
    }
    outgoing {
      artifact(tasks.konanDependenciesReport)
    }
  }
