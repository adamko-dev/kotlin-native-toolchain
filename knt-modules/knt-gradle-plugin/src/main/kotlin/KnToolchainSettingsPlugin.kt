package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.internal.KnToolchainService
import dev.adamko.kntoolchain.internal.KnToolchainsDirSource.Companion.knToolchainsDir
import dev.adamko.kntoolchain.internal.utils.isRootProject
import dev.adamko.kntoolchain.tasks.CheckKnToolchainIntegrityTask
import java.nio.file.Path
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin

abstract class KnToolchainSettingsPlugin
@Inject
internal constructor(
  private val providers: ProviderFactory,
  private val objects: ObjectFactory,
) : Plugin<Settings> {
  override fun apply(settings: Settings) {

    val settingsExtension = createExtension(settings)

    val kntService = registerKnToolchainService(settings)

    configureKnpRepository(settings)

    settings.gradle.rootProject { rootProject ->
      configureRootProject(
        rootProject = rootProject,
        kntService = kntService,
      )
    }

    settings.gradle.afterProject { project ->
      project.plugins.withType<KnToolchainProjectPlugin>().configureEach { _ ->
        val projectExt = project.extensions.getByType<KnToolchainProjectExtension>()
        kntService.get().requestedKnpDists.add(projectExt.kotlinNativePrebuiltDistribution)

        projectExt.baseInstallDirFromSettings.convention(settingsExtension.baseInstallDir)
        projectExt.checksumsDirFromSettings.convention(settingsExtension.checksumsDir)
      }
    }
  }


  private fun createExtension(settings: Settings): KnToolchainSettingsExtension {
    return settings.extensions.create<KnToolchainSettingsExtension>(KnToolchainSettingsExtension.EXTENSION_NAME).apply {
      baseInstallDir.convention(
        objects.directoryProperty()
          .fileProvider(providers.knToolchainsDir().map(Path::toFile))
      )
      checksumsDir.convention(baseInstallDir.dir("checksums"))
    }
  }


  private fun registerKnToolchainService(settings: Settings): Provider<KnToolchainService> =
    settings.gradle.sharedServices.registerIfAbsent(KnToolchainService.SERVICE_NAME, KnToolchainService::class) { _ ->
    }


  @Suppress("UnstableApiUsage")
  private fun configureKnpRepository(settings: Settings): Unit = context(settings) {
    settings.dependencyResolutionManagement.repositories { repositories ->
      repositories.kotlinNativePrebuiltDependencies()
    }
  }


  private fun configureRootProject(
    rootProject: Project,
    kntService: Provider<KnToolchainService>,
  ) {
    val checkKnToolchainTask = registerCheckKonanDataIntegrityTask(
      project = rootProject,
      kntService = kntService,
    )

    rootProject.pluginManager.apply("base")
    rootProject.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { task ->
      task.dependsOn(checkKnToolchainTask)
    }
  }


  /**
   * Register [CheckKnToolchainIntegrityTask] if [project] is the root project.
   */
  private fun registerCheckKonanDataIntegrityTask(
    project: Project,
    kntService: Provider<KnToolchainService>,
  ): TaskProvider<CheckKnToolchainIntegrityTask> {
    require(project.isRootProject()) {
      "$CHECK_KN_TOOLCHAIN_TASK_NAME can only be registered in the root project."
    }

    val layout = project.layout

    return project.tasks.register(CHECK_KN_TOOLCHAIN_TASK_NAME, CheckKnToolchainIntegrityTask::class) { task ->
      task.group = LifecycleBasePlugin.VERIFICATION_GROUP
      task.description = "Checks that the Kotlin/Native prebuilt toolchain installation is valid."

      task.knpDists.convention(kntService.flatMap { it.requestedKnpDists })

      task.binaryResultsDirectory.convention(
        layout.buildDirectory.dir("test-results/${CheckKnToolchainIntegrityTask.TEST_EVENT_ROOT_GROUP_NAME}")
      )
      task.reportDirectory.convention(
        layout.buildDirectory.dir("reports/tests/${CheckKnToolchainIntegrityTask.TEST_EVENT_ROOT_GROUP_NAME}")
      )
    }
  }

  companion object {
    const val CHECK_KN_TOOLCHAIN_TASK_NAME = "checkKnToolchains"
  }
}
