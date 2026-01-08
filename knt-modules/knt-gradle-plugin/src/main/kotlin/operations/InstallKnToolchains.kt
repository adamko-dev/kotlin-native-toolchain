package dev.adamko.kntoolchain.operations

import dev.adamko.kntoolchain.internal.*
import dev.adamko.kntoolchain.internal.BuildChecksumContext.Companion.checksum
import dev.adamko.kntoolchain.internal.BuildChecksumContext.Companion.checksumFile
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import javax.inject.Inject
import kotlin.io.path.*
import kotlin.time.TimeSource
import kotlin.time.measureTimedValue
import kotlinx.coroutines.*
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Installs a Kotlin Native Prebuilt distribution.
 *
 * Must only be evaluated during the task execution phase.
 *
 * If a tool is missing, it will be downloaded and installed.
 */
internal abstract class InstallKnToolchains
@Inject
internal constructor() : BaseKnToolchainsOperation<Path>() {

  /**
   * Download and provision a Kotlin Native Prebuilt distribution.
   *
   * Only one installation should run at a time.
   * [synchronized] on [Companion] to avoid multiple parallel installations
   *
   * ([synchronized] will not strictly prevent concurrent installations.
   * For example, if multiple Gradle builds run simultaneously,
   * or with non-uniform buildscript class loaders.
   * Engineering a solution to prevent concurrent installations is complex and beyond the current scope).
   */
  override fun obtain(): Path = synchronized(Companion) {
    val installSpecs = getInstallSpecs()

    val installSpecStatuses = installSpecs.associateWith { spec -> spec.installStatus() }
    val pendingInstalls = installSpecStatuses.filterValues { it == null }.keys
    val partialInstalls = installSpecStatuses
      .mapNotNull { (spec, status) -> if (status == null) null else spec to status }
      .toMap()


    if (partialInstalls.isNotEmpty()) {
      logger.warn {
        val partialInstallsList =
          partialInstalls.entries.joinToString("\n") { (spec, status) ->
            val relativePath = spec.installDir.relativeTo(super.baseInstallDir).invariantSeparatorsPathString
            " - ${spec.archive.name} $status $relativePath"
          }

        """
        |${partialInstalls.size} partially complete installations detected:
        |$partialInstallsList
        |Possibly two builds are installing at the same time?
        |Try re-running, or delete the installs and try again.
        """.trimMargin()
      }
    }

    val requiredInstalls = installSpecs
      .filter { it.isInstallationRequired() }

    if (requiredInstalls.isNotEmpty()) {
      logger.lifecycle("Installing kotlin-native-prebuilt specs:${pendingInstalls.size}, partial installs:${partialInstalls.size}")

      val startMark = TimeSource.Monotonic.markNow()
      runBlocking {
        requiredInstalls.map { installSpec ->
          async(installDispatcher) {
            installArchive(spec = installSpec)
          }
        }.awaitAll()
      }
      logger.lifecycle("Finished installing/checking Konan and ${installSpecs.size} dependencies in ${startMark.elapsedNow()}")
    } else {
      logger.lifecycle("Skipping kotlin-native-prebuilt install. No pending installs (partial installs:${partialInstalls.size})")
    }

    installSpecs.forEach { spec ->
      updateLastAccessedTime(spec)
    }

    return this@InstallKnToolchains.baseInstallDir
  }

  /**
   * Check if an installation already exists.
   *
   * Installation is required if the incoming archive has changed, or the installation directory is empty.
   */
  private fun DependencyInstallSpec.isInstallationRequired(): Boolean {
    val startMark = TimeSource.Monotonic.markNow()

    // check if the incoming archive has changed...
    val shouldReinstallArchive = isArchiveChanged()

    val installDirPresent = isInstallDirPresent()

    // if the archive hasn't changed, we can skip installing
    if (!shouldReinstallArchive && installDirPresent) {
      logger.info("[${archive.name}] Skip install - up-to-date. (Checked in ${startMark.elapsedNow()}. Dest:${installDir.invariantSeparatorsPathString})")
      return false
    } else {
      logger.lifecycle("[${archive.name}] Installing - shouldReinstallArchive:$shouldReinstallArchive, installDirPresent:$installDirPresent. (Checked in ${startMark.elapsedNow()}). Dest:${installDir.invariantSeparatorsPathString})")
      return true
    }
  }

  private fun DependencyInstallSpec.installStatus(): List<String>? {
    if (!isInstallDirPresent()) return null
    return buildList {
      if (!installDirCacheTagFile.exists()) {
        add("installDirCacheTagFile does not exist")
      }
      if (!archiveChecksumFile.exists()) {
        add("archiveChecksumFile does not exist")
      }
      if (!installDirChecksumFile.exists()) {
        add("installDirChecksumFile does not exist")
      }
    }.takeIf { it.isNotEmpty() }
  }

  /**
   * Installs the archive to the destination directory.
   *
   * Deletes the destination directory if it exists.
   * Must run [isInstallationRequired] beforehand, to check if the installation is needed.
   */
  private fun installArchive(
    spec: DependencyInstallSpec,
  ) {
    logger.lifecycle("[${spec.archive.name}] installing")

    // delete the installDir...
    spec.prepareInstallDirForInstallation()

    // actually unzip!
    extractArchive(
      archive = spec.archive,
      destinationDir = spec.installDir,
      excludes = spec.installFileExcludes,
    )

    spec.createCacheDirTag()

    // update the hash files
    updateChecksums(spec)
  }

  private fun DependencyInstallSpec.createCacheDirTag() {
    installDirCacheTagFile.writeText(
      """
      |Signature: 8a477f597d28d172789f06886806bc55
      |# This file is a cache directory tag created by dev.adamko.kntoolchain
      |# For information about cache directory tags, see:
      |# https://www.brynosaurus.com/cachedir/
      |""".trimMargin()
    )
  }

  private fun DependencyInstallSpec.prepareInstallDirForInstallation() {
    if (
      installDir.exists()
      &&
      installDir.normalize().startsWith(installDir.normalize())
    ) {
      installDir.deleteRecursively()
    }
    installDir.createDirectories()
  }

  private fun updateChecksums(spec: DependencyInstallSpec) {
    val archiveChecksum = computeInstallSpecChecksum(spec)
    val archiveChecksumFile: Path = spec.archiveChecksumFile
    if (!archiveChecksumFile.exists()) {
      archiveChecksumFile.parent.createDirectories()
      archiveChecksumFile.createFile()
    }
    archiveChecksumFile.writeText(archiveChecksum)

    val installDirChecksum = computeDirChecksum(
      destinationDir = spec.installDir,
      excludes = spec.installFileExcludes,
    ) ?: error("Failed to compute checksum for ${spec.installDir}")

    val installDirChecksumFile: Path = spec.installDirChecksumFile
    if (!installDirChecksumFile.exists()) {
      installDirChecksumFile.parent.createDirectories()
      installDirChecksumFile.createFile()
    }
    installDirChecksumFile.appendText(installDirChecksum)
    installDirChecksumFile.appendText("\n")
    installDirChecksumFile.appendLines(
      listInstallDirPathsMetadata(
        installDir = spec.installDir,
        excludes = spec.installFileExcludes,
      )
        .sorted()
    )
  }

  /**
   * Check if the incoming archive has changed.
   */
  private fun DependencyInstallSpec.isArchiveChanged(): Boolean {
    if (!archiveChecksumFile.exists()) {
      return true
    }
    val archiveChecksum = computeInstallSpecChecksum(this).trim()
    val previousChecksum = archiveChecksumFile.readText().trim()
    return archiveChecksum != previousChecksum
  }

  /**
   * Quick and basic check that [DependencyInstallSpec.installDir] exists and has some files.
   * Avoid doing a thorough check, because it's slow to checksum all files all the time.
   *
   * Instead, check integrity in a 'test' task,
   * [dev.adamko.kntoolchain.tasks.CheckKnToolchainIntegrityTask],
   * which is run during the `check` lifecycle task.
   */
  private fun DependencyInstallSpec.isInstallDirPresent(): Boolean =
    installDir.exists()
        && installDir.walk().any { it.isRegularFile() }

  private fun updateLastAccessedTime(spec: DependencyInstallSpec) {
    if (spec.installDirCacheTagFile.exists()) {
      spec.installDirCacheTagFile.setLastModifiedTime(FileTime.from(Instant.now()))
    }
  }

  /**
   * Create a checksum to determine if the incoming archive has changed.
   */
  private fun computeInstallSpecChecksum(
    spec: DependencyInstallSpec,
  ): String {
    val (hash, time) = measureTimedValue {

      val excludeMatchers = spec.installFileExcludes.map { FileSystems.getDefault().getPathMatcher("glob:$it") }

      val includedArchiveEntries =
        useArchiveEntries(spec.archive) { entries ->
          entries
            .filter { !it.isDirectory }
            .filter { entry ->
              excludeMatchers.none { matcher ->
                matcher.matches(Path(entry.name))
              }
            }
            .map { it.name }
            .toSortedSet()
        }

      buildChecksum {
        checksumFile(spec.archive)
        includedArchiveEntries.forEach { entry ->
          checksum(entry)
        }
      }
    }

    logger.info("[${spec.archive.name}] computeInstallSpecChecksum in $time - $hash")

    return hash
  }

  companion object {
    private val logger: Logger = Logging.getLogger(InstallKnToolchains::class.java)

    private val installDispatcher: CoroutineDispatcher =
      Dispatchers.IO.limitedParallelism(10)
  }
}
