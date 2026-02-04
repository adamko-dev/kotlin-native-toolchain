package dev.adamko.kntoolchain.tools.internal

import dev.adamko.kntoolchain.tools.internal.datamodel.KotlinNativePrebuiltData
import dev.adamko.kntoolchain.tools.internal.utils.createDependencyNotation
import dev.adamko.kntoolchain.tools.internal.utils.toEscapedString
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

internal fun registerDependencyResolvers(
  project: Project,
  kotlinVersion: KotlinToolingVersion,
  knVariants: Set<KotlinNativePrebuiltData.PrebuiltVariant>,
): Provider<FileCollection> {

  val knPrebuiltConf: NamedDomainObjectProvider<DependencyScopeConfiguration> =
    project.configurations.dependencyScope("knPrebuiltConf_${kotlinVersion.toEscapedString()}") { c ->
      c.description = "Kotlin/Native $kotlinVersion prebuilt dependencies."

      c.defaultDependencies { dependencies ->
        knVariants.forEach { (classifier, archiveType) ->
          dependencies.add(
            project.dependencies.create(
              createDependencyNotation(
                group = "org.jetbrains.kotlin",
                name = "kotlin-native-prebuilt",
                version = kotlinVersion.toString(),
                classifier = classifier,
                extension = archiveType.dependencyExtension,
              )
            )
          )
        }
      }
    }

  val knPrebuiltConfResolver: NamedDomainObjectProvider<ResolvableConfiguration> =
    project.configurations.resolvable(knPrebuiltConf.name + "Resolver") { c ->
      c.description = "Resolves Kotlin/Native $kotlinVersion prebuilt dependencies declared in ${knPrebuiltConf.name}."
      c.extendsFrom(knPrebuiltConf.get())

      c.isTransitive = false
    }

  return knPrebuiltConfResolver.map { resolver ->
    resolver.incoming.files
  }
}
