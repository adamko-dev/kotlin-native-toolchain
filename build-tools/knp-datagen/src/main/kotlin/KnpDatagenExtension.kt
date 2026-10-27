package dev.adamko.kntoolchain.tools

import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

abstract class KnpDatagenExtension
@Inject
internal constructor() {

  abstract val dataDir: DirectoryProperty

  internal val knpVariantsDataFile: Provider<RegularFile>
    get() = dataDir.file("KotlinNativePrebuiltVariants.json")

  internal val konanDependenciesReportFile: Provider<RegularFile>
    get() = dataDir.file("KonanDependenciesReport.json")

  abstract val generatedKnpDependenciesDataDir: DirectoryProperty

  abstract val regenerateData: Property<Boolean>

  /**
   * Allow updating [generatedKnpDependenciesDataDir] if it differs from the generated code.
   * Otherwise, the build fails.
   *
   * Defaults to the Gradle property `knpDataAllowUpdate`.
   * Otherwise, `true` if [regenerateData] is enabled or when not running on CI.
   */
  abstract val allowUpdate: Property<Boolean>
}
