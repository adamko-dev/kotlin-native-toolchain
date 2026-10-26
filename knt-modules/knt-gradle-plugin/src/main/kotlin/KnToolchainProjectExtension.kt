package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.model.KotlinNativePrebuiltDistributionSpec
import dev.adamko.kntoolchain.model.ProvisionedKnToolchain
import dev.adamko.kntoolchain.operations.InstallKnToolchains
import java.nio.file.Path
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
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
  fun provisionInstallation(): Provider<ProvisionedKnToolchain> {
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

  /**
   * Returns a [Provider] for the `run_konan` script of the installed distribution.
   *
   * The location is configured by [KotlinNativePrebuiltDistributionSpec.relativePathToRunKonan].
   *
   * **Note**: The provider *must* be evaluated during Gradle's task execution phase.
   *
   * @see provisionInstallation
   */
  fun runKonan(): Provider<Path> =
    provisionInstallation().map { it.runKonan }

  companion object {
    private val logger: Logger = Logging.getLogger(KnToolchainProjectExtension::class.java)
    const val EXTENSION_NAME = "knToolchain"
  }
}
