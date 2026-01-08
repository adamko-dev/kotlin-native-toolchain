package dev.adamko.kntoolchain

import java.nio.file.Path
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class KnToolchainSettingsExtension
@Inject
internal constructor() {

  /**
   * Installation directory for all Kotlin/Native Toolchains.
   */
//  abstract val knToolchainsDir: DirectoryProperty
  abstract val knToolchainsDir: Property<Path>

  /**
   * Directory containing checksum files used for avoiding re-provisioning toolchains
   * and for verifying the integrity of the provisioned distribution.
   */
//  abstract val checksumsDir: DirectoryProperty
  abstract val checksumsDir: Property<Path>

  companion object {
    const val EXTENSION_NAME = "knToolchains"
  }
}
