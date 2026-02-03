@file:Suppress("UnstableApiUsage")

package dev.adamko.kntoolchain.internal

import java.io.File
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named

internal class KnpDistributionConfigurations(
  project: Project,
) {

  val knpDistribution: NamedDomainObjectProvider<DependencyScopeConfiguration> =
    project.configurations.dependencyScope("kotlinNativePrebuiltToolchain") { c ->
      c.description = "Kotlin/Native prebuilt distribution."
    }

  private val knpDistributionResolver: NamedDomainObjectProvider<ResolvableConfiguration> =
    project.configurations.resolvable("${knpDistribution.name}Resolver") { c ->
      c.description = "Resolves the Kotlin/Native prebuilt distribution declared in ${knpDistribution.name}."
      c.extendsFrom(knpDistribution.get())
    }

  fun knpDistribution(): Provider<File> {
    return knpDistributionResolver.flatMap { resolver ->
      resolver.incoming.files.elements
        .map { files ->
          requireNotNull(files.singleOrNull()?.asFile) {
            "Failed to resolve sourceArchive from ${resolver.name}. " +
                "Incoming files: " + files.map { it.asFile.name }
          }
        }
    }
  }

  val knpDistributionDependencies: NamedDomainObjectProvider<DependencyScopeConfiguration> =
    project.configurations.dependencyScope(knpDistribution.name + "Dependencies") { c ->
      c.description = "Kotlin/Native prebuilt distribution dependencies."
    }

  val knpDistributionDependenciesResolver: NamedDomainObjectProvider<ResolvableConfiguration> =
    project.configurations.resolvable("${knpDistributionDependencies.name}Resolver") { c ->
      c.description =
        "Resolves all Kotlin/Native prebuilt distribution dependencies declared in ${knpDistributionDependencies.name}."
      c.extendsFrom(knpDistributionDependencies.get())
    }
}
