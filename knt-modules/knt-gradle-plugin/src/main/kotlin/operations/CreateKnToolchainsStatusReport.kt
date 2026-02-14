package dev.adamko.kntoolchain.operations

import dev.adamko.kntoolchain.internal.utils.listInstallDirPathsMetadata
import dev.adamko.kntoolchain.model.DependencyInstallReport
import dev.adamko.kntoolchain.model.KotlinNativePrebuiltDistributionSpec
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.*
import kotlin.time.TimeSource
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.of

/**
 * Creates a status report of the installed kotlin-native-prebuilt.
 *
 * Used by [dev.adamko.kntoolchain.tasks.CheckKnToolchainIntegrityTask]
 * to verify the integrity of the installed toolchain.
 */
internal abstract class CreateKnToolchainsStatusReport
@Inject
internal constructor() : BaseKnToolchainsOperation<DependencyInstallReport>() {

  override fun obtain(): DependencyInstallReport {

    val installSpecs = getInstallSpecs()

    // TODO run in parallel...?
    val depInfos = installSpecs.map { spec ->
      DependencyInstallReport.DependencyInfo(
        archiveName = spec.archive.name,
        installDir = spec.installDir,
        status = determineInstallStatus(spec),
      )
    }

    return DependencyInstallReport(
      konanDataDir = this@CreateKnToolchainsStatusReport.baseInstallDir,
      dependencies = depInfos,
    )
  }


  /**
   * Check if installation is required.
   *
   * Installation is required if the checksum of the installation directory does not match the stored checksums.
   */
  private fun determineInstallStatus(
    spec: DependencyInstallSpec,
  ): DependencyInstallReport.DependencyStatus {
    val startMark = TimeSource.Monotonic.markNow()

    val installDirChecksumFile = installDirChecksumFile(spec)

    val installDirChanged =
      determineChecksumStatus(
        installDir = spec.installDir,
        installDirChecksumFile = installDirChecksumFile,
        installFileExcludes = spec.installFileExcludes,
      )

    return when (installDirChanged) {
      is DependencyInstallReport.DependencyStatus.Invalid -> {
        logger.info("[${spec.archive.name}] install checksum mismatch. (Checked in ${startMark.elapsedNow()}. Dest:${spec.installDir.invariantSeparatorsPathString})")
        installDirChanged
      }

      else                                                -> {
        DependencyInstallReport.DependencyStatus.Valid
      }
    }
  }


  private fun installDirChecksumFile(spec: DependencyInstallSpec): Path {
    val installDirChecksumFileName =
      spec.installDir
        .relativeTo(this@CreateKnToolchainsStatusReport.baseInstallDir)
        .joinToString("_") { it.name } + ".hash"
    return checksumsDir.resolve(installDirChecksumFileName)
  }


  private fun determineChecksumStatus(
    installDir: Path,
    installDirChecksumFile: Path,
    installFileExcludes: Set<String>,
  ): DependencyInstallReport.DependencyStatus {
    val dirChecksum = computeDirChecksum(
      destinationDir = installDir,
      excludes = installFileExcludes,
    )
    val previousChecksum = installDirChecksumFile
      .takeIf { it.exists() }
      ?.useLines { it.firstOrNull() ?: "" }
    val isChanged = dirChecksum != previousChecksum
    if (isChanged) {
      val diffReport = createInstallDirDiffReportFile(installDir, installFileExcludes, installDirChecksumFile)
      return DependencyInstallReport.DependencyStatus.Invalid(
        reason = "checksum mismatch for ${installDir.name}. Expected:${previousChecksum}, Actual:${dirChecksum}",
        details = diffReport.readText(),
      )
    }
    return DependencyInstallReport.DependencyStatus.Valid
  }


  companion object {
    private val logger: Logger = Logging.getLogger(CreateKnToolchainsStatusReport::class.java)

    internal fun ProviderFactory.createKnToolchainsStatusReport(
      installSpecs: List<KotlinNativePrebuiltDistributionSpec>,
      konanDataDir: Provider<Directory>,
      checksumsDir: Provider<Directory>,
    ): Provider<DependencyInstallReport> {
      return of(CreateKnToolchainsStatusReport::class) { spec ->
        spec.parameters.knpDistSpecs.addAll(installSpecs)
        spec.parameters.baseInstallDir.set(konanDataDir)
        spec.parameters.checksumsDir.set(checksumsDir)
      }
    }
  }
}


/**
 * Keep track of the files used to compute the checksum.
 * Used to debug when something is re-installed for unclear reasons.
 */
private fun createInstallDirDiffReportFile(
  installDir: Path,
  installFileExcludes: Set<String>,
  installDirChecksumFile: Path,
): Path {
  val diffReport = Files.createTempFile("kn-toolchains-install-dir-diff", ".txt")
  diffReport.writeText(installDir.invariantSeparatorsPathString)
  diffReport.appendText("\n")

  val prev = installDirChecksumFile
    .takeIf { it.exists() }
    ?.useLines { lines -> lines.drop(1).toList() }
    .orEmpty()
    .iterator()

  val current = listInstallDirPathsMetadata(installDir, installFileExcludes)
    .iterator()

  var maxLineWidth = 0
  val mapPrevToCurrentFileMetadata = buildList {
    while (prev.hasNext() || current.hasNext()) {
      val prevFile = if (prev.hasNext()) prev.next() else ""
      val currentFile = if (current.hasNext()) current.next() else ""
      maxLineWidth = maxOf(maxLineWidth, prevFile.length)
      add(prevFile to currentFile)
    }
  }
  diffReport.appendLines(
    mapPrevToCurrentFileMetadata.map { (prevFile, currentFile) ->
      val mark = if (prevFile == currentFile) "  " else "!!"
      "$mark - ${prevFile.padEnd(maxLineWidth)} $currentFile"
    }
  )

  return diffReport
}
