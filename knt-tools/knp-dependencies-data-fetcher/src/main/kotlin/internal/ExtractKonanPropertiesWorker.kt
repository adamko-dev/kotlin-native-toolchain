package dev.adamko.kntoolchain.tools.internal

import dev.adamko.kntoolchain.tools.datamodel.KotlinNativePrebuiltData.ArchiveType
import javax.inject.Inject
import kotlin.io.path.exists
import kotlin.io.path.name
import org.gradle.api.file.*
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Extract the `konan.properties` file from a kotlin-native-prebuilt distribution.
 *
 * Fails if no file was found.
 */
// NOTE: cannot be process isolated https://github.com/gradle/gradle/issues/33756
internal abstract class ExtractKonanPropertiesWorker
@Inject
internal constructor(
  private val fs: FileSystemOperations,
  private val archives: ArchiveOperations,
) : WorkAction<ExtractKonanPropertiesWorker.Parameters> {

  interface Parameters : WorkParameters {
    /** The extracted `konan.properties` file. */
    val outputDir: RegularFileProperty

    /** `kotlin-native-prebuilt` packaged dist. */
    val knpDist: RegularFileProperty
  }

  override fun execute() {
    val knpDist = parameters.knpDist.get().asFile.toPath()
    val outputDir = parameters.outputDir.get().asFile.toPath()
    val konanProperties = parameters.outputDir.get().asFile.toPath().resolve("konan.properties")

    val archiveType = ArchiveType.fromFile(knpDist)

    logger.lifecycle("[KonanDependenciesReportWorker] extracting ${knpDist.name} to $outputDir")
    val archiveExtractor: (Any) -> FileTree = when (archiveType) {
      ArchiveType.Zip   -> archives::zipTree
      ArchiveType.Jar   -> archives::zipTree
      ArchiveType.TarGz -> { f -> archives.tarTree(archives.gzip(f)) }
    }

    fs.sync { spec ->
      spec.into(outputDir)
      spec.from(archiveExtractor(knpDist))
      spec.include("**/konan/konan.properties")
      spec.eachFile {
        it.relativePath = RelativePath(true, it.sourceName)
      }
      spec.includeEmptyDirs = false
    }

    require(konanProperties.exists()) {
      "konan.properties not found after extraction of $knpDist to $outputDir"
    }
  }

  companion object {
    private val logger: Logger = Logging.getLogger(ExtractKonanPropertiesWorker::class.java)
  }
}
