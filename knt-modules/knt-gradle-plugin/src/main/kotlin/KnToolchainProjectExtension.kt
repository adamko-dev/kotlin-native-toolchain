package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.internal.adding
import dev.adamko.kntoolchain.model.KnToolchainArchitecture
import dev.adamko.kntoolchain.model.KnToolchainOsFamily
import dev.adamko.kntoolchain.model.KnToolchainSpec
import dev.adamko.kntoolchain.operations.InstallKnToolchains
import java.nio.file.Path
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property
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
  abstract val knToolchainsDir: DirectoryProperty

  /**
   * Directory containing checksum files used for avoiding re-provisioning toolchains
   * and for verifying the integrity of the provisioned distribution.
   */
  abstract val checksumsDir: DirectoryProperty

//  internal abstract val knToolchainsDirFromSettings: DirectoryProperty
  internal abstract val knToolchainsDirFromSettings:  Property<Path>
//  internal abstract val checksumsDirFromSettings: DirectoryProperty
  internal abstract val checksumsDirFromSettings:  Property<Path>

  /**
   * The operating system family of the current machine.
   *
   * Used to determine which Kotlin/Native toolchain variant to download.
   *
   * @see org.jetbrains.kotlin.konan.target.HostManager.Companion.simpleOsName
   */
  abstract val hostOsFamily: Property<KnToolchainOsFamily>

  /**
   * The architecture of the current machine.
   *
   * Used to determine which Kotlin/Native toolchain variant to download.
   */
  abstract val hostArchitecture: Property<KnToolchainArchitecture>

  /**
   * The project's current Kotlin version.
   *
   * Used to determine which Kotlin/Native toolchain variant to download.
   */
  abstract val currentKotlinVersion: Property<String>

  /**
   * The specification of the Kotlin/Native toolchain distribution to install.
   *
   * The properties can be configured to specify the desired Kotlin/Native toolchain
   * distribution to install.
   *
   * Use [provisionInstallation] to obtain a [Provider] for the installed distribution.
   */
  val knToolchain: KnToolchainSpec =
    extensions.adding(
      "knToolchain",
      objects.newInstance()
    )

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
      vs.parameters.installSpecs.add(knToolchain)
      vs.parameters.baseInstallDir.set(knToolchainsDir)
      vs.parameters.checksumsDir.set(checksumsDir)
    }
  }

  private val extensions: ExtensionContainer
    get() = (this as ExtensionAware).extensions

  companion object {
    const val EXTENSION_NAME = "knToolchain"
  }
}
