package dev.adamko.kntoolchain.model

import java.io.Serializable
import javax.inject.Inject
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty

/**
 * Specification for installing a kotlin-native-prebuilt distribution.
 *
 * The distribution will be provisioned into a stable directory,
 * so it can be re-used across builds.
 */
abstract class KnToolchainSpec
@Inject
internal constructor() : Serializable {
//
//  /**
//   * Destination directory for the installed distribution.
//   */
//  abstract val konanDataDir: DirectoryProperty

  /**
   * The actual kotlin-native-prebuilt distribution archive.
   */
  abstract val sourceArchive: RegularFileProperty

  /**
   * Additional dependencies required by the kotlin-native-prebuilt distribution archive.
   */
  abstract val sourceDependencies: ConfigurableFileCollection

  /**
   * Glob exclude patterns of files to exclude when unpacking [sourceArchive],
   * or when performing checksum verification.
   */
  abstract val installFileExcludes: SetProperty<String>
//
//  /**
//   * Directory containing checksum files used for avoiding re-provisioning the distribution,
//   * and for verifying the integrity of the provisioned distribution.
//   */
//  abstract val checksumsDir: DirectoryProperty
}
