package dev.adamko.kntoolchain.tools.tasks

import dev.adamko.kntoolchain.tools.datamodel.KonanDependenciesReport
import dev.adamko.kntoolchain.tools.datamodel.KotlinNativePrebuiltData.ArchiveType
import dev.adamko.kntoolchain.tools.datamodel.Platform
import dev.adamko.kntoolchain.tools.internal.ExtractKonanPropertiesWorker
import dev.adamko.kntoolchain.tools.internal.KonanDependenciesWorker
import dev.adamko.kntoolchain.tools.utils.sha512Checksum
import dev.adamko.kntoolchain.tools.utils.takeIfExists
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

/**
 * Produces a [KonanDependenciesReport] file
 * containing all required Konan dependencies.
 *
 * The dependencies must be installed into the `.konan` data dir.
 */
@CacheableTask
abstract class KonanDependenciesReportTask
@Inject
internal constructor(
  private val workers: WorkerExecutor,
) : DefaultTask() {

  //  /**
//   * JSON data of Konan dependencies.
//   *
//   * This file should be committed to VCS to avoid unnecessary task execution.
//   */
////  @get:OutputDirectory
////  abstract val reportsDir: DirectoryProperty
  @get:OutputFile
  abstract val reportFile: RegularFileProperty
//  /**
//   * Output data
//   */
//  @get:OutputDirectory
//  abstract val outputDir: DirectoryProperty

  /**
   * Packaged kotlin-native-prebuilt distributions.
   *
   * The available versions are discovered by
   * [dev.adamko.kntoolchain.tools.internal.KotlinNativePrebuiltVariantsSource].
   */
  @get:InputFiles
  @get:PathSensitive(NONE)
  abstract val konanDistributions: ConfigurableFileCollection

  /**
   * Individual results, per knp dist.
   *
   * Individual files produced by [KonanDependenciesWorker].
   * Will be aggregated into a single file [reportFile].
   */
  @LocalState
  protected val dataDir: Path =
    temporaryDir.resolve("data").toPath()

  @LocalState
  protected val checksumsDir: Path =
    temporaryDir.resolve("checksums").toPath()

  /**
   * Needs to contain
   *
   * - `org.jetbrains.kotlin:kotlin-native-utils`
   * - `org.jetbrains.kotlin:kotlin-compiler-embeddable`
   */
  @get:Classpath
  abstract val workerClasspath: ConfigurableFileCollection

  /**
   * Data for a specific kotlin-native-prebuilt distribution archive.
   */
  private data class KnpDist(
    val archive: FileWithChecksum,
    val konanProperties: FileWithChecksum,
    val dependenciesData: FileWithChecksum,
  ) {
    private val archiveType: ArchiveType = ArchiveType.fromFile(archive.file)
    /** Archive file name, without the extension. */
    private val name: String = archive.file.name.removeSuffix(archiveType.fileExtension)

    val version: KotlinToolingVersion
    val buildPlatform: Platform

    init {
      // knp archives are named `kotlin-native-prebuilt-$version-$host-$arch.$ext`
      val nameElements: List<String> = name
        .removePrefix("kotlin-native-prebuilt-")
        .split("-")

      version = KotlinToolingVersion(nameElements.dropLast(2).joinToString("-"))
      val osFamily = nameElements.takeLast(2).first()
      val osArch = nameElements.last()
      buildPlatform = Platform(
        osFamily = osFamily,
        osArch = osArch,
      )
    }
  }

  private data class FileWithChecksum(
    val file: Path,
    val storedChecksumFile: Path,
  ) {
    private fun storedChecksum(): String? = storedChecksumFile.takeIfExists()?.readText()
    fun currentChecksum(): String? = file.takeIfExists()?.sha512Checksum()

    fun hasChanged(): Boolean =
      storedChecksum() == null || storedChecksum() != currentChecksum()
  }

  private data class DirWithChecksum(
    val dir: Path,
    val storedChecksumFile: Path,
  ) {
    private fun storedChecksum(): String? = storedChecksumFile.takeIfExists()?.readText()
    fun currentChecksum(): String? = dir.takeIfExists()?.sha512Checksum()

    fun hasChanged(): Boolean =
      storedChecksum() == null || storedChecksum() != currentChecksum()
  }

  @TaskAction
  protected fun action() {
    val knpDists = createKnpDists()

    unpackKonanProperties(knpDists)

    computeKonanDependencies(knpDists)

    updateChecksums(knpDists)

    // aggregate dependenciesData for each dist into single file
//    val allReports: List<KonanDependenciesReport> =
//      knpDists.flatMap { knpDist ->
//        knpDist.dependenciesData.dir.listDirectoryEntries("dependencies-*.json").map { dataFile ->
//          json.decodeFromString(
//            KonanDependenciesReport.serializer(),
//            dataFile.readText(),
//          )
//        }
//      }
    val allReports: List<KonanDependenciesReport> =
      knpDists.map { knpDist ->
        json.decodeFromString(
          KonanDependenciesReport.serializer(),
          knpDist.dependenciesData.file.readText(),
        )
      }

//    logger.lifecycle("[$path] Aggregated ${allData.size} reports")
//    logger.debug("[$path] Aggregated ${allData.size} reports : $allData")

    val allData = allReports.flatMap { it.data }

    val report = KonanDependenciesReport(allData)

    saveToJson(report)
//    generateKotlinData(report)
  }

  private fun saveToJson(report: KonanDependenciesReport) {

    val reportFile = reportFile.get().asFile.toPath()

    if (reportFile.exists()) {
      val existingReport = try {
        reportFile.inputStream().use { source ->
          json.decodeFromStream(KonanDependenciesReport.serializer(), source)
        }
      } catch (ex: SerializationException) {
        logger.debug("[$path] failed to decode KonanDependenciesReport from $reportFile: $ex")
        null
      }

      if (existingReport != null && report != existingReport) {
        logger.warn("[$path] Existing report $reportFile differs from computed one")
      }
    }

    reportFile.outputStream().use { sink ->
      json.encodeToStream(KonanDependenciesReport.serializer(), report, sink)
    }
  }

//  private fun saveToSeparateJsons(
//    knpDists: List<KnpDist>,
//  ) {
//    val reportFile = reportFile.get().asFile.toPath()
//
//    // aggregate dependenciesData for each version into separate files
//    val allDataByVersion: Map<String, List<KotlinVersionTargetDependencies>> =
//      knpDists.flatMap { knpDist ->
//        knpDist.dependenciesData.dir.listDirectoryEntries("dependencies-*.json")
//          .map { dataFile ->
//            json.decodeFromString(
//              KotlinVersionTargetDependencies.serializer(),
//              dataFile.readText(),
//            )
//          }
//      }
//        .groupBy { it.version }
//    logger.lifecycle("[$path] Aggregated ${allDataByVersion.values.sumOf { it.size }} reports")
//
//
//    allDataByVersion.forEach { (version, data) ->
//      val report = KonanDependenciesReport(data)
//
//      if (reportFile.exists()) {
//        val existingReport = try {
//          reportFile.inputStream().use { source ->
//            json.decodeFromStream(KonanDependenciesReport.serializer(), source)
//          }
//        } catch (ex: SerializationException) {
//          logger.debug("[$path] failed to decode KonanDependenciesReport from $reportFile: $ex")
//          null
//        }
//
//        if (existingReport != null && report != existingReport) {
//          logger.warn("[$path] Existing report $reportFile differs from computed one")
//        }
//      }
//
//      reportFile.outputStream().use { sink ->
//        json.encodeToStream(KonanDependenciesReport.serializer(), report, sink)
//      }
//    }
//  }

  /**
   * Create a [KnpDist] for each archive file.
   */
  private fun createKnpDists(): List<KnpDist> {
    logger.lifecycle("[$path] Creating ${konanDistributions.count()} knp distributions")

    return konanDistributions.map { knpDistFile ->
      val knpDist = knpDistFile.toPath()
      val archiveType: ArchiveType = ArchiveType.fromFile(knpDist)
      val distName: String = knpDist.name.removeSuffix(archiveType.fileExtension)
      val distChecksumDir = checksumsDir.resolve(distName)
      val distDataDir = dataDir.resolve(distName)
      KnpDist(
        archive = FileWithChecksum(
          file = knpDist,
          storedChecksumFile = distChecksumDir.resolve("${knpDist.name}.sha512"),
        ),
        konanProperties = FileWithChecksum(
          file = distDataDir.resolve("konan/konan.properties"),
          storedChecksumFile = distChecksumDir.resolve("konan.properties.sha512"),
        ),
        dependenciesData = FileWithChecksum(
          file = distDataDir.resolve("dependencies.json"),
          storedChecksumFile = distChecksumDir.resolve("dependencies.json.sha512"),
        ),
//        dependenciesData = DirWithChecksum(
//          dir = distDataDir.resolve("dependencies"),
//          storedChecksumFile = distChecksumDir.resolve("dependencies.sha512"),
//        ),
      )
    }
  }

  /**
   * Extract the `konan.properties` file from each [KnpDist].
   */
  private fun unpackKonanProperties(knpDists: List<KnpDist>) {
    val knpDistsToUnpack = knpDists
      .filter { knpDist -> knpDist.archive.hasChanged() || knpDist.konanProperties.hasChanged() }

    val unpackDistWorkQueue = workers.noIsolation()
    knpDistsToUnpack.forEach { knpDist ->
      unpackDistWorkQueue.submit(ExtractKonanPropertiesWorker::class) {
        it.knpDist.set(knpDist.archive.file.toFile())
        it.outputDir.set(knpDist.konanProperties.file.parent.toFile())
      }
    }
    unpackDistWorkQueue.await()

    logger.lifecycle("[$path] Unpacked ${knpDistsToUnpack.size} konan.properties files")
  }

  private fun computeKonanDependencies(knpDists: List<KnpDist>) {
    val knpDistsToCompute =
      knpDists.filter { knpDist ->
        knpDist.konanProperties.hasChanged() || knpDist.dependenciesData.hasChanged()
        // TODO re-enable, disabled for easier testing
        true
      }


    knpDistsToCompute.groupBy { it.buildPlatform }
      .map { (platform, knpDists) ->

        val konanDependenciesWorkQueue = workers.processIsolation { spec ->
          spec.classpath.from(workerClasspath)
          spec.forkOptions {
            it.systemProperty("os.name", platform.osNameForJavaSystemProperty)
            it.systemProperty("os.arch", platform.osArch)
          }
        }
        knpDists.forEach { knpDist ->

          konanDependenciesWorkQueue.submit(KonanDependenciesWorker::class) {
            it.targetDependenciesReportFile.set(knpDist.dependenciesData.file.toFile())
            it.konanPropertiesFile.set(knpDist.konanProperties.file.toFile())
            it.distVersion.set(knpDist.version.toString())
            it.buildPlatform.set(platform)
          }
        }

        konanDependenciesWorkQueue
      }
      .forEach { it.await() }


    logger.lifecycle("[$path] Computed ${knpDistsToCompute.size} konan dependencies")
  }

  private fun updateChecksums(knpDists: List<KnpDist>) {
    knpDists.forEach { knpDist ->
      knpDist.archive.storedChecksumFile.apply {
        parent.createDirectories()
        writeText(knpDist.archive.currentChecksum() ?: error("could not compute checksum for $this"))
      }
      knpDist.konanProperties.storedChecksumFile.apply {
        parent.createDirectories()
        writeText(knpDist.konanProperties.currentChecksum() ?: error("could not compute checksum for $this"))
      }
      knpDist.dependenciesData.storedChecksumFile.apply {
        parent.createDirectories()
        writeText(knpDist.dependenciesData.currentChecksum() ?: error("could not compute checksum for $this"))
      }
    }
  }

  companion object {
    private val json = Json {
      prettyPrint = true
      prettyPrintIndent = "  "
    }
  }
}
