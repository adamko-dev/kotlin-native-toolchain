package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.KnToolchainProjectExtension.Companion.EXTENSION_NAME
import dev.adamko.kntoolchain.internal.utils.CACHE_DIR_TAG_FILENAME
import dev.adamko.kntoolchain.internal.KnToolchainsDirSource.Companion.knToolchainsDir
import dev.adamko.kntoolchain.internal.KnpDependenciesCoordsSpec.Companion.knpDependenciesCoordsSpec
import dev.adamko.kntoolchain.internal.KnpDistributionConfigurations
import dev.adamko.kntoolchain.internal.KnpDistributionDependencyCoordsSource.Companion.kotlinNativePrebuiltToolchainDependencySpec
import dev.adamko.kntoolchain.tools.data.KnBuildPlatform
import dev.adamko.kntoolchain.tools.data.KnpVersion
import java.io.File
import java.nio.file.Path
import java.util.*
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create

/**
 * Installs a Kotlin/Native prebuilt distribution, including all dependencies.
 */
abstract class KnToolchainProjectPlugin
@Inject
internal constructor(
  private val objects: ObjectFactory,
  private val providers: ProviderFactory,
  private val layout: ProjectLayout,
) : Plugin<Project> {

  override fun apply(project: Project) {
    val extension = createKnToolchainExtension(
      project = project,
    )

    val knpConfigurations = KnpDistributionConfigurations(
      project = project,
    )

    configureKotlinNativePrebuiltTool(
      project = project,
      knpToolchainExtension = extension,
      knpConfigurations = knpConfigurations,
    )
  }

  internal fun createKnToolchainExtension(project: Project): KnToolchainProjectExtension {
    return project.extensions.create<KnToolchainProjectExtension>(EXTENSION_NAME).apply {

      baseInstallDir.convention(layout.dir(providers.knToolchainsDir().map(Path::toFile)))
      checksumsDir.convention(baseInstallDir.dir("checksums"))

      val currentOs = providers.systemProperty("os.name").map { osName ->
        when {
          osName.startsWith("Win")   -> KnBuildPlatform.OsFamily.Windows
          osName.startsWith("Linux") -> KnBuildPlatform.OsFamily.Linux
          osName.startsWith("Mac")   -> KnBuildPlatform.OsFamily.MacOs
          else                       -> error("Unknown OS: $osName")
        }
      }

      val currentArch = providers.systemProperty("os.arch").map { arch ->
        when (arch) {
          "amd64"   -> KnBuildPlatform.OsArch.X86_64
          "x86_64"  -> KnBuildPlatform.OsArch.X86_64
          "aarch64" -> KnBuildPlatform.OsArch.AArch64
          else      -> error("Unknown arch: $arch")
        }
      }

      kotlinNativePrebuiltDistribution.buildPlatform.convention(
        providers.zip(currentOs, currentArch) { os, arch ->
          KnBuildPlatform.allPlatforms.firstOrNull { platform ->
            platform.family == os && platform.arch == arch
          } ?: error("Unknown platform: $os/$arch")
        }
      )
      kotlinNativePrebuiltDistribution.version.convention(
        providers.provider {
          KnpVersion.allVersions.firstOrNull {
            it.value == kotlinGradlePluginVersion
          }
        }
      )

      kotlinNativePrebuiltDistribution.coordinates.convention(
        providers.kotlinNativePrebuiltToolchainDependencySpec(
          buildPlatform = kotlinNativePrebuiltDistribution.buildPlatform,
          version = kotlinNativePrebuiltDistribution.version,
        )
      )

      kotlinNativePrebuiltDistribution.installFileExcludes.convention(
        setOf(
          "*/license/**",
          "*/licenses/**",
          "*/cache/**",
          "*/caches/**",
          "*/source/**",
          "*/sources/**",
          "*/NOTICE",
          "**/${CACHE_DIR_TAG_FILENAME}",
        )
      )
    }
  }

  private fun configureKotlinNativePrebuiltTool(
    project: Project,
    knpToolchainExtension: KnToolchainProjectExtension,
    knpConfigurations: KnpDistributionConfigurations,
  ) {

    val kotlinNativePrebuiltDependency: Provider<Dependency> =
      knpToolchainExtension.kotlinNativePrebuiltDistribution.coordinates.map { coords ->
        project.dependencies.create(coords)
      }

    knpConfigurations.knpDistribution.configure { c ->
      c.defaultDependencies { dependencies ->
        dependencies.addLater(kotlinNativePrebuiltDependency)
      }
    }

    knpToolchainExtension.kotlinNativePrebuiltDistribution.sourceArchive.convention(
      knpConfigurations.knpDistribution()
    )

    val knpPrebuiltDependencies: Provider<List<Dependency>> =
      providers.knpDependenciesCoordsSpec(
        knpSpec = knpToolchainExtension.kotlinNativePrebuiltDistribution,
      ).map { coords ->
        coords.map { coord ->
          project.dependencies.create(coord.coord) {
            val coordArtifact = coord.artifact
            if (coordArtifact != null) {
              // If `coord.artifact` is present, it indicates module != artifact.
              // We must specify the artifact; otherwise Gradle assumes the
              // artifact name is the same as the module name.
              // Must clear artifacts first https://github.com/gradle/gradle/issues/33781
              artifacts.clear()
              artifact { artifact ->
                artifact.name = coordArtifact
                artifact.extension = coord.extension
                artifact.type = coord.extension
                artifact.classifier = coord.classifier
              }
            }
          }
        }
      }

    knpConfigurations.knpDistributionDependencies.configure { c ->
      c.defaultDependencies { dependencies ->
        dependencies.addAllLater(knpPrebuiltDependencies)
      }
    }

    knpToolchainExtension.kotlinNativePrebuiltDistribution.sourceDependencies.from(
      knpConfigurations.knpDistributionDependenciesResolver
    )
  }

  private fun RegularFileProperty.convention(file: Provider<File>): RegularFileProperty =
    convention(objects.fileProperty().fileProvider(file))

  companion object {
    private val kotlinGradlePluginVersion: String? by lazy {
      val propFileName = "project.properties"
      val inputStream = this::class.java.classLoader!!.getResourceAsStream(propFileName)
        ?: return@lazy null
      val props = Properties()
      inputStream.use { props.load(it) }
      props.getProperty("project.version")
    }
  }
}
