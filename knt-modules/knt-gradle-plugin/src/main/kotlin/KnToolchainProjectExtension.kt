package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.model.KotlinNativePrebuiltDistributionSpec
import dev.adamko.kntoolchain.model.OsFamily
import dev.adamko.kntoolchain.operations.InstallKnToolchains
import java.nio.file.Path
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.of

abstract class KnToolchainProjectExtension
@Inject
internal constructor(
  objects: ObjectFactory,
  private val providers: ProviderFactory,
) {

  /**
   * Installation directory for all Kotlin/Native Toolchains.
   */
  abstract val baseInstallDir: DirectoryProperty

  /**
   * Directory containing checksum files used for avoiding re-provisioning toolchains
   * and for verifying the integrity of the provisioned distribution.
   */
  abstract val checksumsDir: DirectoryProperty

  internal abstract val baseInstallDirFromSettings: DirectoryProperty
  internal abstract val checksumsDirFromSettings: DirectoryProperty

  /**
   * The specification of the Kotlin/Native toolchain distribution to install.
   *
   * The properties can be configured to specify the desired Kotlin/Native toolchain
   * distribution to install.
   *
   * Use [provisionInstallation] to obtain a [Provider] of the distribution.
   */
  val kotlinNativePrebuiltDistribution: KotlinNativePrebuiltDistributionSpec =
    objects.newInstance()

  /**
   * The specification of the Kotlin/Native toolchain distribution to install.
   *
   * The properties can be configured to specify the desired Kotlin/Native toolchain
   * distribution to install.
   *
   * Use [provisionInstallation] to obtain a [Provider] of the distribution.
   */
  fun kotlinNativePrebuiltDistribution(configure: Action<KotlinNativePrebuiltDistributionSpec>) {
    configure.execute(kotlinNativePrebuiltDistribution)
  }

  /**
   * Returns a [Provider] for the installed kotlin-native-prebuilt distribution,
   * a.k.a. `KONAN_DATA_DIR`.
   *
   * **Note**: The provider *must* be evaluated during Gradle's task execution phase.
   *
   * Evaluating the provider will trigger downloading and installation
   * of the distribution (if necessary).
   * Gradle requires downloading is performed during the task execution phase.
   */
  fun provisionInstallation(): Provider<Path> {
    return providers.of(InstallKnToolchains::class) { vs ->
      vs.parameters.knpDistSpecs.add(kotlinNativePrebuiltDistribution)
      vs.parameters.baseInstallDir.set(baseInstallDir)
      vs.parameters.checksumsDir.set(checksumsDir)
    }.map { installs ->
      if (installs.size > 1) {
        logger.warn(
          "Expected a single toolchain installation, but found ${installs.size}: ${installs.joinToString { it.toString() }}\n" +
              "\t${kotlinNativePrebuiltDistribution.debugString()}\n" +
              "\tknToolchainsDir:${baseInstallDir.orNull?.asFile?.invariantSeparatorsPath}\n" +
              "\tchecksumsDir:${checksumsDir.orNull?.asFile?.invariantSeparatorsPath}"
        )
      } else if (installs.isEmpty()) {
        logger.warn(
          "Expected a single toolchain installation, but found none" +
              "\t${kotlinNativePrebuiltDistribution.debugString()}\n" +
              "\tknToolchainsDir:${baseInstallDir.orNull?.asFile?.invariantSeparatorsPath}\n" +
              "\tchecksumsDir:${checksumsDir.orNull?.asFile?.invariantSeparatorsPath}"
        )
      }
      installs.singleOrNull()
    }
  }

  fun runKonan(
    pathToRunKonan: Provider<String> = kotlinNativePrebuiltDistribution.osFamily.map { os ->
      if (os is OsFamily.Windows) "bin/run_konan.bat" else "bin/run_konan"
    },
  ): Provider<Path> {
    return providers.zip(
      provisionInstallation(),
      pathToRunKonan,
    ) { knpDir, path ->
      val actual = knpDir.resolve(path)
      require(actual.startsWith(knpDir)) {
        "Path '$actual' is not within the installation directory '$knpDir'"
      }
      actual
    }
  }

  private val extensions: ExtensionContainer
    get() = (this as ExtensionAware).extensions

  companion object {
    private val logger: Logger = Logging.getLogger(KnToolchainProjectExtension::class.java)
    const val EXTENSION_NAME = "knToolchain"
  }
}
