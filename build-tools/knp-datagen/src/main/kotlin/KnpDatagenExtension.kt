package dev.adamko.kntoolchain.tools

import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

abstract class KnpDatagenExtension
@Inject
internal constructor(
) {

  abstract val dataDir: DirectoryProperty//  =
//    layout.projectDirectory.dir("knt-data")

  internal val knpVariantsDataFile: Provider<RegularFile>
    get() = dataDir.file("KotlinNativePrebuiltVariants.json")

  internal val konanDependenciesReportFile: Provider<RegularFile>
    get() = dataDir.file("KonanDependenciesReport.json")

  abstract val generatedKnpDependenciesDataDir: DirectoryProperty

  abstract val regenerateData: Property<Boolean>
}
