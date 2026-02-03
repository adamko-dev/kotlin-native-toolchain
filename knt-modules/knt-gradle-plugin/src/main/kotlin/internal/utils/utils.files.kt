package dev.adamko.kntoolchain.internal.utils

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink


/**
 * https://bford.info/cachedir/
 */
internal const val CACHE_DIR_TAG_FILENAME = "CACHEDIR.TAG"

/**
 * Returns the type of file (file, directory, symlink, etc.).
 */
internal fun Path.describeType(): String =
  buildString {
    append(
      when {
        !exists()       -> "<non-existent>"
        isRegularFile() -> "file"
        isDirectory()   -> "directory"
        else            -> "<unknown>"
      }
    )

    if (isSymbolicLink()) {
      append(" (symlink)")
    }
  }
