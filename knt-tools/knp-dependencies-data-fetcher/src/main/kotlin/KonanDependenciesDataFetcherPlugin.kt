@file:Suppress("UnstableApiUsage")

package dev.adamko.kntoolchain.tools

import dev.adamko.kntoolchain.tools.datamodel.KotlinNativePrebuiltData
import dev.adamko.kntoolchain.tools.internal.BuildConstants
import dev.adamko.kntoolchain.tools.internal.KotlinNativePrebuiltVariantsSource
import dev.adamko.kntoolchain.tools.internal.KotlinVersionsDataSource
import dev.adamko.kntoolchain.tools.tasks.KonanDependenciesReportTask
import javax.inject.Inject
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

abstract class KonanDependenciesDataFetcherPlugin
@Inject
internal constructor(
  private val providers: ProviderFactory,
  private val layout: ProjectLayout,
  private val objects: ObjectFactory,
) : Plugin<Project> {
  override fun apply(project: Project) {
    val gradle = project.gradle

    val kotlinVersionsData: Provider<Set<KotlinToolingVersion>> =
      providers.of(KotlinVersionsDataSource::class) { vs ->
        vs.parameters {
          it.stateDir.set(layout.projectDirectory.dir("data"))
          it.currentKotlinVersion.set(BuildConstants.kotlinVersion)
        }
      }

    val kotlinNativePrebuiltData: Provider<KotlinNativePrebuiltData> =
      providers.of(KotlinNativePrebuiltVariantsSource::class) { vs ->
        vs.parameters {
          it.stateDir.set(layout.projectDirectory.dir("data"))
          it.kotlinVersions.set(kotlinVersionsData)
        }
      }

    val allKonanDistributions: ConfigurableFileCollection = project.objects.fileCollection()

    kotlinNativePrebuiltData.get().data.forEach { (kotlinVersion, variants) ->
      logger.info("[${project.path}] registering dependency resolvers for $kotlinVersion variants: $variants")
      val konanDist = registerDependencyResolvers(project, kotlinVersion, variants)
      allKonanDistributions.from(konanDist)
    }

    val konanDependenciesReportTaskClasspath: NamedDomainObjectProvider<DependencyScopeConfiguration> =
      project.configurations.dependencyScope("konanDependenciesReportTaskClasspath") { c ->
        c.defaultDependencies { deps ->
          deps.addAll(
            listOf(
              "org.jetbrains.kotlin:kotlin-native-utils:${BuildConstants.kotlinVersion}",
              "org.jetbrains.kotlin:kotlin-compiler-embeddable:${BuildConstants.kotlinVersion}",
            ).map(project.dependencies::create)
          )
        }
      }

    val konanDependenciesReportTaskClasspathResolver: NamedDomainObjectProvider<ResolvableConfiguration> =
      project.configurations.resolvable(konanDependenciesReportTaskClasspath.name + "Resolver") { c ->
        c.extendsFrom(konanDependenciesReportTaskClasspath.get())
        c.attributes.attribute(USAGE_ATTRIBUTE, objects.named(JAVA_RUNTIME))
      }

    project.tasks.withType<KonanDependenciesReportTask>().configureEach { task ->
      task.group = project.name
      task.workerClasspath.from(konanDependenciesReportTaskClasspathResolver)
    }

    project.tasks.register("konanDependenciesReport", KonanDependenciesReportTask::class) { task ->
      task.konanDistributions.from(allKonanDistributions)
      task.reportFile.set(layout.projectDirectory.dir("data").file("KonanDependenciesReport.json"))
    }
  }

  companion object {
    private val logger: Logger = Logging.getLogger(KonanDependenciesDataFetcherPlugin::class.java)
  }
}
