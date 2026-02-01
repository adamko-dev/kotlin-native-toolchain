package dev.adamko.kntoolchain.internal

import java.io.OutputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.zip.GZIPInputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.*
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.InputStreamStatistics

internal fun Path.nameWithoutArchiveExtension(): String {
  return when {
    name.endsWith(".zip")    -> name.removeSuffix(".zip")
    name.endsWith(".tar")    -> name.removeSuffix(".tar")
    name.endsWith(".tar.gz") -> name.removeSuffix(".tar.gz")
    else                     -> {
      error("unsupported archive format (file:${name})")
    }
  }
}

/**
 * Extract an archive, respecting symlinks.
 */
internal fun extractArchive(
  archive: Path,
  destinationDir: Path,
  excludes: Set<String>,
  fileTime: FileTime = FileTime.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)),
) {
  require(archive.isRegularFile()) { "archive must be a regular file, but was ${archive.describeType()}" }
  require(!destinationDir.exists() || destinationDir.isDirectory()) { "destinationDir must be a directory, but was ${archive.describeType()}" }
  require(destinationDir.isAbsolute) { "destinationDir must be absolute" }

  val excludeMatchers: Set<PathMatcher> = excludes.mapTo(mutableSetOf()) {
    FileSystems.getDefault().getPathMatcher("glob:$it")
  }

  when {
    archive.name.endsWith(".zip")    ->
      extractZip(
        archive = archive,
        destinationDir = destinationDir,
        excludes = excludeMatchers,
        fileTime = fileTime,
      )

    archive.name.endsWith(".tar.gz") ->
      extractTarGz(
        archive = archive,
        destinationDir = destinationDir,
        excludes = excludeMatchers,
        fileTime = fileTime,
      )

    else                             ->
      error("Unsupported format for unzipping $archive")
  }
}

/**
 * Unzip [archive] as a `.zip` file into [destinationDir].
 *
 * Protects against zip bombs and zip slip attacks.
 */
private fun extractZip(
  archive: Path,
  destinationDir: Path,
  excludes: Set<PathMatcher>,
  fileTime: FileTime,
) {
  val tempDest = createTempDirectory()

  ZipFile.builder()
    .setPath(archive)
    .get()
    .use { zipFile: ZipFile ->

      // STEP 1: validate all entries
      validateZipArchive(
        archive = archive,
        zipFile = zipFile,
        destinationDir = tempDest,
      )

      // STEP 2: find all matching entries
      val zipEntries =
        zipFile.entries.asSequence()
          .filter { entry: ZipArchiveEntry ->
            excludes.none { it.matches(Path(entry.name)) }
          }

      // STEP 3: copy the contents to the temporary destination
      zipEntries.forEach { entry: ZipArchiveEntry ->
        val entryDestination = entry.resolveIn(tempDest)
        when {
          entry.isDirectory   -> entryDestination.createDirectories()
          entry.isUnixSymlink -> {
            entryDestination.createSymbolicLinkPointingTo(Path(entry.name))
          }

          else                -> {
            if (entryDestination.exists()) {
              error("Destination file already exists: $entryDestination")
            }
            entryDestination.outputStream().use { sink ->
              zipFile.getInputStream(entry).use { entrySource ->
                entrySource.transferTo(sink)
              }
            }
            entryDestination.fileAttributesView<BasicFileAttributeView>().setTimes(
              fileTime,
              fileTime,
              fileTime,
            )
          }
        }
      }
    }

  // STEP 4: copy temporary destination to the actual dest
  val rootOutputDir = tempDest.listDirectoryEntries().singleOrNull()
    ?.takeIf { it.isDirectory() }

  if (rootOutputDir != null) {
    rootOutputDir.moveTo(destinationDir, overwrite = true)
  } else {
    tempDest.moveTo(destinationDir, overwrite = true)
  }
}

private fun extractTarGz(
  archive: Path,
  destinationDir: Path,
  excludes: Set<PathMatcher>,
  fileTime: FileTime,
) {
  val tempDest = createTempDirectory()
  val hardLinks = mutableMapOf<Path, Path>()

  GZIPInputStream(archive.inputStream()).use { gzipInputStream ->
    TarArchiveInputStream(gzipInputStream).use { tarInputStream ->

      // STEP 1: validate all entries
      val allEntries =
        tarInputStream.iterator().asIterator().asSequence()
          .onEach { entry: TarArchiveEntry ->
            entry.resolveIn(tempDest)
          }

      // STEP 2: find all matching entries
      val tarEntries =
        allEntries.filter { entry: TarArchiveEntry ->
          excludes.none { it.matches(Path(entry.name)) }
        }

      // STEP 3: copy the contents to the destination
      tarEntries.forEach { entry: TarArchiveEntry ->
        val entryDestination = entry.resolveIn(tempDest)
        when {
          entry.isDirectory    ->
            entryDestination.createDirectories()

          entry.isSymbolicLink -> {
            val target = Path(entry.linkName)
            entryDestination.createSymbolicLinkPointingTo(target)
          }

          entry.isLink         -> {
            val target = tempDest.resolve(entry.linkName)
            hardLinks[entryDestination] = target
          }

          else                 -> {
            entryDestination.parent.createDirectories()
            entryDestination.outputStream().use { sink ->
              tarInputStream.transferTo(sink)
            }

            if (entryDestination.fileAttributesViewOrNull<PosixFileAttributeView>() != null) {
              entryDestination.setPosixFilePermissions(entry.getPosixFilePermissions())
            } else {
              // Not supported on this filesystem (e.g. on Windows)
            }

            entryDestination.fileAttributesView<BasicFileAttributeView>().setTimes(
              fileTime,
              fileTime,
              fileTime,
            )
          }
        }
      }
    }
  }

  hardLinks.forEach { (src, target) ->
    src.createLinkPointingTo(target)
  }

  // STEP 4: copy temporary destination to the actual dest
  val rootOutputDir = tempDest.listDirectoryEntries().singleOrNull()
    ?.takeIf { it.isDirectory() }

  if (rootOutputDir != null) {
    rootOutputDir.moveTo(destinationDir, overwrite = true)
  } else {
    tempDest.moveTo(destinationDir, overwrite = true)
  }
}

private fun TarArchiveEntry.getPosixFilePermissions(): Set<PosixFilePermission> {
  val permissions: MutableSet<PosixFilePermission> = mutableSetOf()

  fun add(permissionBitMask: Int, permission: PosixFilePermission) {
    if ((mode and permissionBitMask) > 0) {
      permissions.add(permission)
    }
  }

  // owner permissions
  add(0b100_000_000, PosixFilePermission.OWNER_READ)
  add(0b010_000_000, PosixFilePermission.OWNER_WRITE)
  add(0b001_000_000, PosixFilePermission.OWNER_EXECUTE)

  // group permissions
  add(0b000_100_000, PosixFilePermission.GROUP_READ)
  add(0b000_010_000, PosixFilePermission.GROUP_WRITE)
  add(0b000_001_000, PosixFilePermission.GROUP_EXECUTE)

  // other permissions
  add(0b000_000_100, PosixFilePermission.OTHERS_READ)
  add(0b000_000_010, PosixFilePermission.OTHERS_WRITE)
  add(0b000_000_001, PosixFilePermission.OTHERS_EXECUTE)

  return permissions
}


/**
 * Use all entries in an archive.
 *
 * **Important**: Do not use this to extract archives.
 * This function has no protection against maliciously constructed archives.
 * Use [extractArchive] instead.
 */
@OptIn(ExperimentalContracts::class)
internal fun <T> useArchiveEntries(
  archive: Path,
  block: (entries: Sequence<ArchiveEntry>) -> T,
): T {
  contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

  require(archive.isRegularFile()) { "archive must be a regular file, but was ${archive.describeType()}" }

  return when {
    archive.name.endsWith(".zip")    ->
      useZipEntries(archive) { entries -> block(entries) }

    archive.name.endsWith(".tar.gz") ->
      useTarGzEntries(archive) { entries -> block(entries) }

    else                             ->
      error("Unsupported format for unzipping $archive")
  }
}


@OptIn(ExperimentalContracts::class)
private fun <T> useZipEntries(
  archive: Path,
  block: (entries: Sequence<ZipArchiveEntry>) -> T,
): T {
  contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

  ZipFile.builder()
    .setPath(archive)
    .get()
    .use { zipFile: ZipFile ->
      validateZipArchive(
        archive = archive,
        zipFile = zipFile,
        destinationDir = createTempDirectory(),
      )

      val entries = zipFile.entries.asSequence()
      return block(entries)
    }
}

@OptIn(ExperimentalContracts::class)
private fun <T> useTarGzEntries(
  archive: Path,
  block: (entries: Sequence<TarArchiveEntry>) -> T
): T {
  contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

  GZIPInputStream(archive.inputStream()).use { gzipInputStream ->
    TarArchiveInputStream(gzipInputStream).use { tarInputStream ->
      val entries = tarInputStream.iterator().asIterator().asSequence()
      return block(entries)
    }
  }
}


private fun validateZipArchive(
  archive: Path,
  zipFile: ZipFile,
  destinationDir: Path,
  /** Allowable maximum compression ratio. */
  maxCompressionRatio: Double = 100.0,
) {
  require(maxCompressionRatio > 1.0) { "maxCompressionRatio must be >= 1.0, but was $maxCompressionRatio" }

  /** ZipBomb validation on a specific entry. */
  fun InputStreamStatistics.validateCompression() {
    if (compressedCount < 1024 || uncompressedCount < 1024) {
      // not enough data to check yet, and avoid divide-by-zero
      return
    }
    val currentRatio = uncompressedCount.toDouble() / compressedCount.toDouble()
    require(currentRatio < maxCompressionRatio) {
      "Compression ratio on entry exceeded maximum ratio $maxCompressionRatio. ${archive.invariantSeparatorsPathString}"
    }
  }

  val zipEntries = zipFile.entries.asSequence()

  zipEntries.forEach { entry: ZipArchiveEntry ->
    // ArchiveEntry#resolveIn validates against ZipSlip
    entry.resolveIn(destinationDir)

    if (!entry.isDirectory) {
      zipFile.getInputStream(entry).use { entrySource ->
        require(entrySource is InputStreamStatistics) {
          "entrySource must implement InputStreamStatistics, as per ZipFile.getInputStream() Javadoc."
        }
        entrySource.transferTo(OutputStream.nullOutputStream())
        entrySource.validateCompression()
      }
    }
  }
}
