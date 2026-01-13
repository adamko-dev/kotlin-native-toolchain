package dev.adamko.kntoolchain.model

import java.io.Serializable
import javax.inject.Inject
import org.gradle.api.Describable
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * Specification of a kotlin-native-prebuilt distribution.
 * Used to download and install a specific distribution.
 *
 * The distribution will be provisioned into a stable directory,
 * so it can be re-used across builds.
 */
abstract class KotlinNativePrebuiltDistributionSpec
@Inject
internal constructor() : Serializable, Describable {

  /**
   * The operating system family of the current machine.
   *
   * Used to determine which Kotlin/Native toolchain variant to download.
   *
   * @see org.jetbrains.kotlin.konan.target.HostManager.Companion.simpleOsName
   */
  abstract val osFamily: Property<OsFamily>

  /**
   * The architecture of the kotlin-native-prebuilt distribution.
   *
   * Used to determine which Kotlin/Native toolchain variant to download.
   *
   * @see org.jetbrains.kotlin.konan.target.HostManager.Companion.hostArch
   */
  abstract val architecture: Property<Architecture>

  /**
   * The GAV Maven coordinates of the kotlin-native-prebuilt distribution.
   * Derived from [osFamily], [architecture], and [version].
   *
   * This property should only be overridden if modifying
   * the other properties isn't enough to correctly describe the distribution
   * (for example, in the future the archive extension may change).
   *
   * ### Format
   *
   * The string must use Gradle's dependency notation format,
   * [described here](https://docs.gradle.org/current/userguide/declaring_dependencies_basics.html#sec:module-dependencies).
   *
   * ```
   * group:name:version:classifier@ext
   * ```
   * `classifier` and `ext` are optional.
   */
  abstract val coordinates: Property<String>

  /**
   * The project's current Kotlin version.
   *
   * Used to determine which Kotlin/Native toolchain variant to download.
   */
  abstract val version: Property<String>

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


  override fun getDisplayName(): String = buildString {
    append("KotlinNativePrebuiltDistributionSpec")
    append("(")
    append(coordinates.orNull)
    append(")")
  }

  internal fun debugString(): String = buildString {
    append("kotlin-native-prebuilt distribution ")
    append("coordinates:")
    append(coordinates.orNull)
    append(", ")
    append("sourceArchive:")
    append(sourceArchive.orNull?.asFile?.invariantSeparatorsPath)
    append(", ")
    append("sourceDependencies:")
    append(sourceDependencies.files.map { it.invariantSeparatorsPath }.toString())
    append(", ")
    append("installFileExcludes:")
    append(installFileExcludes.orNull)
  }
}
