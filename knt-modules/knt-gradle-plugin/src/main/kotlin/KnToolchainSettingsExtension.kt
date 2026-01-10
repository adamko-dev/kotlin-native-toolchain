package dev.adamko.kntoolchain

import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty

abstract class KnToolchainSettingsExtension
@Inject
internal constructor() {

  /**
   * Installation directory for all Kotlin/Native Toolchains.
   */
  abstract val baseInstallDir: DirectoryProperty

  /**
   * Directory containing checksum files used for avoiding re-provisioning toolchains
   * and for verifying the integrity of the provisioned distribution.
   */
  abstract val checksumsDir: DirectoryProperty

  companion object {
    const val EXTENSION_NAME = "knToolchains"
  }
}
