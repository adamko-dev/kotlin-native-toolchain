@file:Suppress("UnstableApiUsage")

package dev.adamko.kntoolchain.tools

import dev.adamko.kntoolchain.tools.internal.datamodel.KotlinNativePrebuiltData
import dev.adamko.kntoolchain.tools.internal.BuildConstants
import dev.adamko.kntoolchain.tools.internal.registerDependencyResolvers
import dev.adamko.kntoolchain.tools.tasks.KnpDataGenTask
import dev.adamko.kntoolchain.tools.tasks.KonanDependenciesReportTask
import dev.adamko.kntoolchain.tools.internal.KotlinNativePrebuiltDataSource
import javax.inject.Inject
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*

abstract class KonanDependenciesDataFetcherPlugin
@Inject
internal constructor(
  private val providers: ProviderFactory,
  private val layout: ProjectLayout,
  private val objects: ObjectFactory,
) : Plugin<Project> {

  override fun apply(project: Project) {
    //apply1(project)
    apply2(project)
  }

//  private fun apply1(project: Project) {
//    val kotlinVersionsData: Provider<Set<KotlinToolingVersion>> =
//      providers.of(KotlinVersionsDataSource::class) { vs ->
//        vs.parameters {
//          it.stateDir.set(layout.projectDirectory.dir("data"))
//          it.currentKotlinVersion.set(BuildConstants.kotlinVersion)
//        }
//      }
//
//    val kotlinNativePrebuiltData: Provider<KotlinNativePrebuiltData> =
//      providers.of(KotlinNativePrebuiltVariantsSource::class) { vs ->
//        vs.parameters {
//          it.stateDir.set(layout.projectDirectory.dir("data"))
//          it.kotlinVersions.set(kotlinVersionsData)
//        }
//      }
//
//    val allKonanDistributions: ConfigurableFileCollection = project.objects.fileCollection()
//
//    kotlinNativePrebuiltData.get().data.forEach { (kotlinVersion, variants) ->
//      logger.info("[${project.path}] registering dependency resolvers for $kotlinVersion variants: $variants")
//      val konanDist = registerDependencyResolvers(project, kotlinVersion, variants)
//      allKonanDistributions.from(konanDist)
//    }
//
//    val konanDependenciesReportTaskClasspath: NamedDomainObjectProvider<DependencyScopeConfiguration> =
//      project.configurations.dependencyScope("konanDependenciesReportTaskClasspath") { c ->
//        c.defaultDependencies { deps ->
//          deps.addAll(
//            listOf(
//              "org.jetbrains.kotlin:kotlin-native-utils:${BuildConstants.kotlinVersion}",
//              "org.jetbrains.kotlin:kotlin-compiler-embeddable:${BuildConstants.kotlinVersion}",
//            ).map(project.dependencies::create)
//          )
//        }
//      }
//
//    val konanDependenciesReportTaskClasspathResolver: NamedDomainObjectProvider<ResolvableConfiguration> =
//      project.configurations.resolvable(konanDependenciesReportTaskClasspath.name + "Resolver") { c ->
//        c.extendsFrom(konanDependenciesReportTaskClasspath.get())
//        c.attributes.attribute(USAGE_ATTRIBUTE, objects.named(JAVA_RUNTIME))
//      }
//
//    project.tasks.withType<KonanDependenciesReportTask>().configureEach { task ->
//      task.group = project.name
//      task.workerClasspath.from(konanDependenciesReportTaskClasspathResolver)
//    }
//
//    project.tasks.register("konanDependenciesReport", KonanDependenciesReportTask::class) { task ->
//      task.konanDistributions.from(allKonanDistributions)
//      task.reportFile.set(layout.projectDirectory.dir("data").file("KonanDependenciesReport.json"))
//    }
//  }

//  private val dataDir: Directory = layout.projectDirectory.dir("knt-data")
//  private val knpVariantsDataFile: RegularFile = dataDir.file("KotlinNativePrebuiltVariants.json")

  private fun apply2(project: Project) {
    val knpDataExt: KnpDataExtension =
      createExtension(
        project = project,
      )

//    val createKotlinNativePrebuiltDataTask: TaskProvider<CreateKotlinNativePrebuiltDataTask> =
//      registerCreateKotlinNativePrebuiltDataTask(
//        project = project,
//        knpDataExt = knpDataExt,
//      )
    val kotlinNativePrebuiltData: Provider<KotlinNativePrebuiltData> =
      providers.of(KotlinNativePrebuiltDataSource::class) { spec ->
        spec.parameters.regenerateData.set(knpDataExt.regenerateData)
        spec.parameters.workDir.set(layout.buildDirectory.dir("temp/KotlinNativePrebuiltDataSource"))
        spec.parameters.knpVariantsDataFile.set(knpDataExt.knpVariantsDataFile)
      }

//    val kotlinNativePrebuiltData: KotlinNativePrebuiltData? =
//      loadKnpData(
//        knpDataExt = knpDataExt,
//      )

    val allKonanDistributions: FileCollection =
      collectKonanDistributions(
        project = project,
//        knpDataExt = knpDataExt,
        kotlinNativePrebuiltData = kotlinNativePrebuiltData,
//        createKotlinNativePrebuiltDataTask = createKotlinNativePrebuiltDataTask,
      )

    val konanDependenciesReportTask =
      registerKonanDependenciesReportTask(
        project = project,
        knpDataExt = knpDataExt,
        allKonanDistributions = allKonanDistributions,
      )

    registerKnpDataGenTask(
      project = project,
      knpDataExt = knpDataExt,
      konanDependenciesReportTask = konanDependenciesReportTask,
    )

    val konanDependenciesReportTaskClasspathResolver =
      registerKonanDependenciesReportTaskClasspathResolver(
        project = project,
      )

    configureTaskConventions(
      project = project,
      knpDataExt = knpDataExt,
      konanDependenciesReportTaskClasspathResolver = konanDependenciesReportTaskClasspathResolver,
    )
  }

  private fun createExtension(project: Project): KnpDataExtension {
    return project.extensions.create("asd", KnpDataExtension::class).also { ext ->
      ext.dataDir.convention(layout.projectDirectory.dir("knt-data"))
      ext.generatedKnpDependenciesDataDir.convention(
        layout.projectDirectory.dir("src/mainGenerated/kotlin/")
      )
      ext.regenerateData.convention(
        providers.gradleProperty("kntRegenerate").map(String::toBoolean).orElse(false)
      )
    }
  }

//  /**
//   * Load data from an existing [KnpDataExtension.knpVariantsDataFile].
//   */
//  private fun loadKnpData(
//    knpDataExt: KnpDataExtension,
//  ): KotlinNativePrebuiltData? {
//    return knpDataExt.knpVariantsDataFile.get().asFile.toPath()
//      .takeIf { it.exists() }
//      ?.inputStream()?.use { source ->
//        json.decodeFromStream(KotlinNativePrebuiltData.serializer(), source)
//      }
//  }

  private fun registerKonanDependenciesReportTaskClasspathResolver(
    project: Project,
  ): NamedDomainObjectProvider<ResolvableConfiguration> {
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

    return project.configurations.resolvable(konanDependenciesReportTaskClasspath.name + "Resolver") { c ->
      c.extendsFrom(konanDependenciesReportTaskClasspath.get())
      c.attributes.attribute(USAGE_ATTRIBUTE, objects.named(JAVA_RUNTIME))
    }
  }

  private fun collectKonanDistributions(
    project: Project,
//    knpDataExt: KnpDataExtension,
    kotlinNativePrebuiltData: Provider<KotlinNativePrebuiltData>,
//    createKotlinNativePrebuiltDataTask: TaskProvider<*>,
  ): FileCollection {
    val allKonanDistributions = objects.fileCollection()

//    if (kotlinNativePrebuiltData == null) {
//      logger.error(
//        "Failed to load KotlinNativePrebuiltData from ${knpDataExt.knpVariantsDataFile.get().asFile.invariantSeparatorsPath}. "
////            + "Run task ${project.path}:${createKotlinNativePrebuiltDataTask.name} to generate the report."
//      )
//    } else {
//    }
    kotlinNativePrebuiltData.get().data.forEach { (kotlinVersion, variants) ->
      logger.info("[${project.path}] registering dependency resolvers for $kotlinVersion variants: $variants")
      val konanDist = registerDependencyResolvers(project, kotlinVersion, variants)
      allKonanDistributions.from(konanDist)
    }

    return allKonanDistributions
  }

  //region TASKS

  private fun registerKonanDependenciesReportTask(
    project: Project,
    knpDataExt: KnpDataExtension,
    allKonanDistributions: FileCollection,
  ): TaskProvider<KonanDependenciesReportTask> {
    return project.tasks.register("konanDependenciesReport", KonanDependenciesReportTask::class) { task ->
      task.konanDistributions.from(allKonanDistributions)
      task.reportFile.convention(knpDataExt.konanDependenciesReportFile)
    }
  }

//  private fun registerCreateKotlinNativePrebuiltDataTask(
//    project: Project,
//    knpDataExt: KnpDataExtension,
//  ): TaskProvider<CreateKotlinNativePrebuiltDataTask> {
//    return project.tasks.register("createKotlinNativePrebuiltData", CreateKotlinNativePrebuiltDataTask::class) { task ->
//      task.group = project.name
//      task.knpVariantsDataFile.convention(knpDataExt.knpVariantsDataFile)
//    }
//  }

  private fun registerKnpDataGenTask(
    project: Project,
    knpDataExt: KnpDataExtension,
    konanDependenciesReportTask: TaskProvider<KonanDependenciesReportTask>,
  ): TaskProvider<KnpDataGenTask> {
    return project.tasks.register("knpDataGen", KnpDataGenTask::class) { task ->
      task.outputDir.convention(knpDataExt.generatedKnpDependenciesDataDir)
      task.konanDependenciesReportFile.convention(
        konanDependenciesReportTask.flatMap { it.reportFile }
      )
    }
  }

  private fun configureTaskConventions(
    project: Project,
    knpDataExt: KnpDataExtension,
    konanDependenciesReportTaskClasspathResolver: NamedDomainObjectProvider<ResolvableConfiguration>,
  ) {
    project.tasks.withType<KonanDependenciesReportTask>().configureEach { task ->
      task.group = project.name
      task.workerClasspath.from(konanDependenciesReportTaskClasspathResolver)
      task.onlyIf("regenerate is enabled") { _ ->
        knpDataExt.regenerateData.get()
      }
    }

//    project.tasks.withType<CreateKotlinNativePrebuiltDataTask>().configureEach { task ->
//      task.onlyIf("regenerate is enabled") { _ ->
//        knpDataExt.regenerateData.get()
//      }
//    }

    project.tasks.withType<KnpDataGenTask>().configureEach { task ->
      task.onlyIf("regenerate is enabled") { _ ->
        knpDataExt.regenerateData.get()
      }
    }
  }
  //endregion

  companion object {
    private val logger: Logger = Logging.getLogger(KonanDependenciesDataFetcherPlugin::class.java)
  }
}
