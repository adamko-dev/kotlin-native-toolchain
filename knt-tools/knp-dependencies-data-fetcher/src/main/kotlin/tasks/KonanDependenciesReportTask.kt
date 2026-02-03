package dev.adamko.kntoolchain.tools.tasks

import dev.adamko.kntoolchain.tools.datamodel.KonanDependenciesReport
import dev.adamko.kntoolchain.tools.datamodel.KotlinNativePrebuiltData.ArchiveType
import dev.adamko.kntoolchain.tools.datamodel.Platform
import dev.adamko.kntoolchain.tools.internal.ExtractKonanPropertiesWorker
import dev.adamko.kntoolchain.tools.internal.KonanDependenciesWorker
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
  @get:OutputFile
  abstract val reportFile: RegularFileProperty

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

//  @LocalState
//  protected val checksumsDir: Path =
//    temporaryDir.resolve("checksums").toPath()

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
    val archive: Path,
    val konanProperties: Path,
    val dependenciesData: Path,
  ) {
    private val archiveType: ArchiveType = ArchiveType.fromFile(archive)
    /** Archive file name, without the extension. */
    private val name: String = archive.name.removeSuffix(archiveType.fileExtension)

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

  @TaskAction
  protected fun action() {
    val knpDists = createKnpDists()

    unpackKonanProperties(knpDists)

    computeKonanDependencies(knpDists)

    val allReports: List<KonanDependenciesReport> =
      knpDists.map { knpDist ->
        json.decodeFromString(
          KonanDependenciesReport.serializer(),
          knpDist.dependenciesData.readText(),
        )
      }

    val allData = allReports.flatMap { it.data }

    // aggregate dependenciesData for each dist into single file
    val report = KonanDependenciesReport(allData)

    saveToJson(report)
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

  /**
   * Create a [KnpDist] for each archive file.
   */
  private fun createKnpDists(): List<KnpDist> {
    logger.lifecycle("[$path] Creating ${konanDistributions.count()} knp distributions")

    return konanDistributions.map { knpDistFile ->
      val knpDist = knpDistFile.toPath()
      val archiveType: ArchiveType = ArchiveType.fromFile(knpDist)
      val distName: String = knpDist.name.removeSuffix(archiveType.fileExtension)
      val distDataDir = dataDir.resolve(distName)
      KnpDist(
        archive = knpDist,
        konanProperties = distDataDir.resolve("konan/konan.properties"),
        dependenciesData = distDataDir.resolve("dependencies.json"),
      )
    }
  }

  /**
   * Extract the `konan.properties` file from each [KnpDist].
   */
  private fun unpackKonanProperties(knpDists: List<KnpDist>) {
    val unpackDistWorkQueue = workers.noIsolation()
    knpDists.forEach { knpDist ->
      unpackDistWorkQueue.submit(ExtractKonanPropertiesWorker::class) {
        it.knpDist.set(knpDist.archive.toFile())
        it.outputDir.set(knpDist.konanProperties.parent.toFile())
      }
    }
    unpackDistWorkQueue.await()

    logger.lifecycle("[$path] Unpacked ${knpDists.size} konan.properties files")
  }

  private fun computeKonanDependencies(knpDists: List<KnpDist>) {
    knpDists.groupBy { it.buildPlatform }
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
            it.targetDependenciesReportFile.set(knpDist.dependenciesData.toFile())
            it.konanPropertiesFile.set(knpDist.konanProperties.toFile())
            it.distVersion.set(knpDist.version.toString())
            it.buildPlatform.set(platform)
          }
        }

        konanDependenciesWorkQueue
      }
      .forEach { it.await() }


    logger.lifecycle("[$path] Computed ${knpDists.size} konan dependencies")
  }

  companion object {
    private val json = Json {
      prettyPrint = true
      prettyPrintIndent = "  "
    }
  }
}
