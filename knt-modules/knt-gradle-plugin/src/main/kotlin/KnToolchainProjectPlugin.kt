package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.KnToolchainProjectExtension.Companion.EXTENSION_NAME
import dev.adamko.kntoolchain.internal.CACHE_DIR_TAG_FILENAME
import dev.adamko.kntoolchain.internal.KnToolchainsDirSource.Companion.knToolchainsDir
import dev.adamko.kntoolchain.internal.KnpDependenciesCoordsSpec.Companion.knpDependenciesCoordsSpec
import dev.adamko.kntoolchain.internal.KnpDistributionConfigurations
import dev.adamko.kntoolchain.internal.KnpDistributionDependencySpec.Companion.kotlinNativePrebuiltToolchainDependencySpec
import dev.adamko.kntoolchain.model.KnToolchainArchitecture
import dev.adamko.kntoolchain.model.KnToolchainOsFamily
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
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra

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

//    configureRepositories()
  }

  internal fun createKnToolchainExtension(project: Project): KnToolchainProjectExtension {
    return project.extensions.create<KnToolchainProjectExtension>(EXTENSION_NAME).apply {

      knToolchainsDir.convention(
        layout.dir(
          knToolchainsDirFromSettings
            .orElse(providers.knToolchainsDir())
            .map(Path::toFile)
        )
      )
      checksumsDir.convention(
        layout.dir(
          checksumsDirFromSettings
            .map(Path::toFile)
            .orElse(knToolchainsDir.dir("checksums").map { it.asFile })
        )
      )

      hostOsFamily.convention(hostOs())
      hostArchitecture.convention(hostArch())
      currentKotlinVersion.convention(providers.provider { kotlinGradlePluginVersion })

      knToolchain.installFileExcludes.convention(
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
      providers.kotlinNativePrebuiltToolchainDependencySpec(
        osName = knpToolchainExtension.hostOsFamily.map { it.name },
        archName = knpToolchainExtension.hostArchitecture.map { it.name },
        kotlinVersion = knpToolchainExtension.currentKotlinVersion,
      )
        .map {
          project.dependencies.create(it)
        }

    knpConfigurations.knpDistribution.configure { c ->
      c.defaultDependencies { dependencies ->
        dependencies.addLater(kotlinNativePrebuiltDependency)
      }
    }

    knpToolchainExtension.knToolchain.sourceArchive.convention(
      knpConfigurations.knpDistribution()
    )

    val knpPrebuiltDependencies: Provider<List<Dependency>> =
      providers.knpDependenciesCoordsSpec(
        osName = knpToolchainExtension.hostOsFamily.map { it.name },
        archName = knpToolchainExtension.hostArchitecture.map { it.name },
        kotlinVersion = knpToolchainExtension.currentKotlinVersion,
      )
        .map { coords ->
          coords.map { coord ->
            project.dependencies.create(coord.asDependencyNotation()) {
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

    knpToolchainExtension.knToolchain.sourceDependencies.from(
      knpConfigurations.knpDistributionDependenciesResolver
    )
  }

//  private fun configureRepositories(project: Project) {
////    val repositories = project.repositories
////
////    if (repositories !is ExtensionAware) return
////
////    repositories.extra.set(
////      KN_PREBUILT_DEPS_URL_PROPERTY,
////      providers.gradleProperty(KN_PREBUILT_DEPS_URL_PROPERTY).orNull
////    )
////
////    repositories.extensions.add("objectFactory", objects)
//  }

  private fun RegularFileProperty.convention(file: Provider<File>): RegularFileProperty =
    convention(objects.fileProperty().fileProvider(file))

  companion object {
    private fun hostOs(): KnToolchainOsFamily {
      val javaOsName = System.getProperty("os.name")
      return when {
        javaOsName == "Mac OS X"         -> KnToolchainOsFamily.MacOs
        javaOsName == "Linux"            -> KnToolchainOsFamily.Linux
        javaOsName.startsWith("Windows") -> KnToolchainOsFamily.Windows
        else                             -> throw IllegalStateException("Unknown operating system: $javaOsName")
      }
    }

    private fun hostArch(): KnToolchainArchitecture {
      return when (val osArch = System.getProperty("os.arch")) {
        "x86_64",
        "amd64"   -> KnToolchainArchitecture.X86_64

        "arm64",
        "aarch64" -> KnToolchainArchitecture.AArch64

        else      -> throw IllegalStateException("Unknown hardware platform: $osArch")
      }
    }

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
