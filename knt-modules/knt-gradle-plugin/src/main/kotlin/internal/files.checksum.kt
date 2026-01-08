package dev.adamko.kntoolchain.internal

import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

internal fun Path.checksumFile(
  baseDir: Path? = null,
  additionalMetadata: Collection<String> = emptyList(),
): String {
  require(exists()) {
    "Cannot checksum non-existing file: ${this.toAbsolutePath()}"
  }
  require(isRegularFile()) {
    "Cannot checksum Path because it is not a file: ${this.toAbsolutePath()}"
  }

  return sequenceOf(this)
    .checksumFiles(
      baseDir = baseDir,
      additionalMetadata = additionalMetadata,
    )
}

/**
 * Create a checksum of all files in the directory.
 *
 * @param[excludes] Glob patterns - see [walkFiltered]
 * @param[includes] Glob patterns - see [walkFiltered]
 */
internal fun Path.checksumDirectory(
  includes: Set<String> = emptySet(),
  excludes: Set<String> = emptySet(),
): String {
  require(exists()) {
    "Cannot checksum non-existing directory: ${this.toAbsolutePath()}"
  }
  require(isDirectory()) {
    "Cannot checksum Path because it is not a directory: ${this.toAbsolutePath()}"
  }

  val content = walkFiltered(
    includes = includes,
    excludes = excludes,
  )

  return content.checksumFiles(this)
}


/**
 * Create checksum of file content and paths, relative to [baseDir].
 *
 * Additionally encode some [additionalMetadata] (for example, a version, or include/exclude patterns).
 */
internal fun Collection<Path>.checksumFiles(
  baseDir: Path?,
  additionalMetadata: Collection<String> = emptyList(),
): String =
  asSequence()
    .checksumFiles(
      baseDir = baseDir,
      additionalMetadata = additionalMetadata,
    )


/**
 * Create checksum of file content and paths, relative to [baseDir].
 *
 * Additionally encode some [additionalMetadata] (for example, a version, or include/exclude patterns).
 */
internal fun Sequence<Path>.checksumFiles(
  baseDir: Path?,
  additionalMetadata: Collection<String> = emptyList(),
): String =
  checksumFilesImpl(
    files = this.toSortedSet(),
    baseDir = baseDir,
    additionalMetadata = additionalMetadata,
  ) { file ->
    // hash relative path
    val relativePath = file.relativeToBasePath()
    if (relativePath != null) {
      checksum(relativePath.toByteArray())
    }

    // hash file content
    checksumContent(file)
  }


internal fun Collection<Path>.checksumFilesMetadata(
  baseDir: Path?,
  additionalMetadata: Collection<String> = emptyList(),
): String =
  asSequence()
    .checksumFiles(
      baseDir = baseDir,
      additionalMetadata = additionalMetadata,
    )


internal fun Sequence<Path>.checksumFilesMetadata(
  baseDir: Path?,
  additionalMetadata: Collection<String> = emptyList(),
): String =
  checksumFilesImpl(
    files = this.toSortedSet(),
    baseDir = baseDir,
    additionalMetadata = additionalMetadata,
  ) { file ->

    val relativePath = file.relativeToBasePath()
    if (relativePath != null) {
      checksum(relativePath.toByteArray())
    }

    checksum(file.fileSize().toByteArray())

    checksum(
      when {
        file.isRegularFile() -> 1
        file.isDirectory()   -> 2
        else                 -> 3
      }
    )

    checksum(
      if (file.isSymbolicLink()) 1 else 0
    )

    checksum(file.getLastModifiedTime().toMillis().toByteArray())

//    file.readAttributes<BasicFileAttributes>(LinkOption.NOFOLLOW_LINKS).apply {
//      checksum(creationTime().toMillis().toByteArray())
//    }

//    val permissions = file.getPosixFilePermissions(LinkOption.NOFOLLOW_LINKS)
//    PosixFilePermission.entries.forEach { permission ->
//      checksum(
//        if (permission in permissions) 1 else 0
//      )
//    }
  }


/**
 * Convert this [Long] to a [ByteArray].
 *
 * The bytes are stored in big-endian format.
 */
internal fun Long.toByteArray(): ByteArray {
  // Note: JVM stores bytes in big-endian, so start at the left-most bytes

  // initial:        A B C D E F G H
  // rotate-left:    B C D E F G H A
  // take last byte:               ^
  // rotate-left:    C D E F G H A B
  // take last byte:               ^
  // rotate-left:    D E F G H A B C
  // take last byte:               ^
  // etc..

  fun Long.lastByte(): Byte = (this and 0xFF).toByte()

  var acc = this
  return ByteArray(Long.SIZE_BYTES) { _ ->
    acc = acc.rotateLeft(Byte.SIZE_BITS)
    acc.lastByte()
  }
}


private fun checksumFilesImpl(
  files: SortedSet<Path>,
  baseDir: Path?,
  additionalMetadata: Collection<String>,
  checksummer: ChecksumFilesContext.(Path) -> Unit,
): String {
  if (baseDir != null) {
    require(baseDir.isDirectory()) {
      "baseDir $baseDir must be a directory, but was ${baseDir.describeType()}."
    }
  }

  require(files.isNotEmpty()) {
    "Cannot create checksum: no files found."
  }

  return buildChecksum {
    val ctx = ChecksumFilesContext(this, baseDir)
    files.forEach { file ->
      ctx.checksummer(file)
    }
    additionalMetadata.forEach { metadata ->
      checksum(metadata.toByteArray())
    }
  }
}


private class ChecksumFilesContext(
  private val context: BuildChecksumContext,
  private val baseDir: Path? = null,
) {
  fun checksum(value: Int) {
    context.checksum(value)
  }

  fun checksum(bytes: ByteArray) {
    context.checksum(bytes)
  }

  fun checksumContent(file: Path) {
    file.inputStream().use { source ->
      context.checksum(source)
    }
  }

  /** Make the path relative to [baseDir]. */
  fun Path.relativeToBasePath(): String? {
    if (baseDir == null) return null
    return relativeToOrNull(baseDir)?.invariantSeparatorsPathString
      ?: error("Cannot create checksum, file '$this' is outside of root dir '$baseDir'")
  }
}
