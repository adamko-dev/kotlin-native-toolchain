package dev.adamko.kntoolchain.internal.utils

import java.nio.file.Path
import kotlin.io.path.*

/**
 *
 */
internal fun listInstallDirPathsMetadata(
  installDir: Path,
  excludes: Set<String>,
): Sequence<String> {
  return installDir
    .walkFiltered(excludes = excludes)
    .map { file ->
      buildString {
        append(file.relativeTo(installDir).invariantSeparatorsPathString)
        append(" size:${file.fileSize()}")
        append(" type:")
        append(
          when {
            file.isRegularFile() -> 1
            file.isDirectory()   -> 2
            else                 -> 3
          }
        )
        append(" sl:")
        append(if (file.isSymbolicLink()) 1 else 0)

        append(" lm:")
        append(file.getLastModifiedTime().toMillis())
      }
    }
    .sorted()
}
