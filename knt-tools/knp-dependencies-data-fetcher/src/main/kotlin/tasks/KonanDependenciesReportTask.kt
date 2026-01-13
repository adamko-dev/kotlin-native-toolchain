package dev.adamko.kntoolchain.tools.tasks

import dev.adamko.kntoolchain.tools.datamodel.KonanDependenciesReport
import dev.adamko.kntoolchain.tools.datamodel.KotlinNativePrebuiltData.ArchiveType
import dev.adamko.kntoolchain.tools.datamodel.KotlinVersionTargetDependencies
import dev.adamko.kntoolchain.tools.internal.ExtractKonanPropertiesWorker
import dev.adamko.kntoolchain.tools.internal.KonanDependenciesWorker
import dev.adamko.kntoolchain.tools.utils.sha512Checksum
import dev.adamko.kntoolchain.tools.utils.takeIfExists
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

/**
 * Produces a [dev.adamko.kntoolchain.tools.datamodel.KonanDependenciesReport] file
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

  /**
   * JSON data of Konan dependencies.
   *
   * This file should be committed to VCS to avoid unnecessary task execution.
   */
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
   * All available Kotlin versions.
   *
   * @see dev.adamko.kntoolchain.tools.internal.KotlinVersionsDataSource
   */
  @get:Input
  abstract val kotlinVersions: SetProperty<KotlinToolingVersion>

  /**
   * Individual results, per knp dist.
   *
   * Individual files produced by [dev.adamko.kntoolchain.tools.internal.KonanDependenciesWorker].
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

  private data class KnpDist(
    val archive: FileWithChecksum,
    val konanProperties: FileWithChecksum,
    val dependenciesData: FileWithChecksum,
  ) {
    private val archiveType: ArchiveType = ArchiveType.fromFile(archive.file)
    private val name: String = archive.file.name.removeSuffix(archiveType.fileExtension)

    val version: KotlinToolingVersion
    val hostFamily: String
    val hostArch: String

    init {
      // knp archives are named `kotlin-native-prebuilt-$version-$host-$arch.$ext`
      val nameElements: List<String> = name
        .removePrefix("kotlin-native-prebuilt-")
        .split("-")

      version = KotlinToolingVersion(nameElements.dropLast(2).joinToString("-"))
      hostFamily = nameElements.takeLast(2).first()
      hostArch = nameElements.last()
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

  @TaskAction
  protected fun action() {
    val knpDists = createKnpDists()

    unpackKonanProperties(knpDists)

    computeKonanDependencies(knpDists)

    updateChecksums(knpDists)

    // aggregate dependenciesData for each dist into single file
    val allData: List<KotlinVersionTargetDependencies> =
      knpDists.map { knpDist ->
        json.decodeFromString(
          KotlinVersionTargetDependencies.serializer(),
          knpDist.dependenciesData.file.readText(),
        )
      }

    logger.lifecycle("[$path] Aggregated ${allData.size} reports")
    logger.debug("[$path] Aggregated ${allData.size} reports : $allData")

    val allDataEncoded = json.encodeToString(
      KonanDependenciesReport.serializer(),
      KonanDependenciesReport(allData),
    )
    reportFile.get().asFile.writeText(allDataEncoded)
    println("allDataEncoded: $allDataEncoded")
  }

  /**
   * Create a [KnpDist] for each archive file.
   */
  private fun createKnpDists(): List<KnpDist> {
    logger.lifecycle("[$path] Creating ${konanDistributions.count()} knp distributions")

    return konanDistributions.map { knpDist ->
      val archiveType: ArchiveType = ArchiveType.fromFile(knpDist.toPath())
      val distName: String = knpDist.toPath().name.removeSuffix(archiveType.fileExtension)
      val distChecksumDir = checksumsDir.resolve(distName)
      val distDataDir = dataDir.resolve(distName)
      KnpDist(
        archive = FileWithChecksum(
          file = knpDist.toPath(),
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
      }

    val konanDependenciesWorkQueue = workers.processIsolation {
      it.classpath.from(workerClasspath)
      it.forkOptions { }
    }
    knpDistsToCompute.forEach { knpDist ->
      konanDependenciesWorkQueue.submit(KonanDependenciesWorker::class) {
        it.targetDependenciesReportFile.set(knpDist.dependenciesData.file.toFile())
        it.konanPropertiesFile.set(knpDist.konanProperties.file.toFile())
        it.distVersion.set(knpDist.version.toString())
        it.hostFamily.set(knpDist.hostFamily)
        it.hostArch.set(knpDist.hostArch)
      }
    }

    konanDependenciesWorkQueue.await()

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
