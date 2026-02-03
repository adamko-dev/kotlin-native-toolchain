package dev.adamko.kntoolchain.internal.utils

import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.relativeToOrNull

/**
 * Recursively walks through a directory tree.
 *
 * @param[maxDepth] The maximum depth to traverse in the directory tree.
 * @param[enterDirectories] Only enter directories that match this predicate.
 * @return A sequence of [Path]s encountered during the traversal.
 */
internal fun Path.walk(
  maxDepth: Int,
  enterDirectories: ((dir: Path) -> Boolean) = { true },
): Sequence<Path> {
  require(isDirectory()) { "Expected a directory, but was: $this" }

  val allPaths = ArrayDeque<Path>()
  allPaths.addAll(listDirectoryEntries())

  return sequence {
    while (allPaths.isNotEmpty()) {
      val path = allPaths.removeFirst()
      val depth = path.relativeToOrNull(this@walk)?.nameCount ?: 0

      if (path.isDirectory() && depth < maxDepth && enterDirectories(path)) {
        allPaths.addAll(path.listDirectoryEntries())
      }

      yield(path)
    }
  }
}
