package dev.adamko.kntoolchain.internal.utils

import java.nio.file.Path
import kotlin.io.path.*
import kotlin.text.get

/**
 * Get all files within a directory.
 *
 * Only returns regular files.
 *
 * ### Filtering content
 *
 * - If there are no inclusions or exclusions, everything is included.
 * - If any inclusions are specified only matching files are included.
 * - Any exclusion pattern overrides any inclusions.
 *   If a file or directory matches at least one exclusion pattern,
 *   it won’t be included, regardless of the inclusion patterns.
 *
 * #### Glob patterns
 *
 * The files can be filtered using glob patterns.
 *
 * The following patterns are supported:
 *
 * - `**` - matches anything
 * - `*` - matches a substring in a path element.
 * - `?` - a single character in a path element.
 *
 * If the glob starts with `/`, it anchors the pattern to the root.
 */
internal fun Path.walkFiltered(
  includes: Set<String> = emptySet(),
  excludes: Set<String> = emptySet(),
): Sequence<Path> =
  walkFilteredImpl(
    baseDir = this,
    includes = includes,
    excludes = excludes,
  )


@OptIn(ExperimentalPathApi::class)
private fun walkFilteredImpl(
  baseDir: Path,
  includes: Set<String>,
  excludes: Set<String>,
): Sequence<Path> {
  require(baseDir.isDirectory()) {
    "Cannot walk Path $baseDir. It must be a directory, but was ${baseDir.describeType()}"
  }

  /** Make the path relative to [baseDir]. */
  fun Path.relativeToBasePath(anchored: Boolean = false): String {
    val path = relativeToOrNull(baseDir)?.invariantSeparatorsPathString
      ?: error("Cannot create checksum, file '$this' is outside of root dir '$baseDir'")

    return if (anchored) "/$path" else path
  }

  val includeMatchers: List<Regex> = includes.map { globToRegex(it) }
  val excludeMatchers: List<Regex> = excludes.map { globToRegex(it) }

//  if (includeMatchers.isNotEmpty()) {
//    println("includeMatchers: $includeMatchers")
//  }
//  if (excludeMatchers.isNotEmpty()) {
//    println("excludeMatchers: $excludeMatchers")
//  }

  val content = baseDir.walk()
    .filter { it.isRegularFile() }
    .distinct()
    .filter { file ->
      val matchesInclusion = includeMatchers.all { matcher ->
        matcher.matches(file.relativeToBasePath(anchored = true))
      }
      val isNotExcluded = excludeMatchers.none { exclusion ->
        exclusion.matches(file.relativeToBasePath(anchored = true))
      }
      //println("   ${file.relativeToBasePath()}: include:$matchesInclusion, not-exclude:$isNotExcluded")
      matchesInclusion && isNotExcluded
    }

  return content
}


/**
 * Converts a glob pattern string to a [Regex].
 *
 * The following patterns are supported:
 *
 * - `**` - matches anything
 * - `*` - matches a substring in a path element.
 * - `?` - a single character in a path element.
 *
 * If the glob starts with `/`, it anchors the pattern to the root.
 */
private fun globToRegex(
  glob: String,
): Regex {
  val pathSeparator = "/".escapeRegexChars()

  val isAnchored = glob.startsWith(pathSeparator)

  val regex = glob
    .escapeRegexChars()
    .replace(Regex("""(?<doubleStar>\\\*\\\*)|(?<singleStar>\\\*)|(?<singleChar>\\\?)""")) {
      when {
        it.groups["doubleStar"] != null -> ".*?"
        it.groups["singleStar"] != null -> "[^${pathSeparator}]*?"
        it.groups["singleChar"] != null -> "[^${pathSeparator}]?"
        else                            -> error("could not convert '$glob' to regex, unknown match group. $it")
      }
    }
//    .toRegex(Regex Option.IGNORE_CASE)

  // If anchored, prepend '^' to force matching from the root of the input
  val finalPattern = if (isAnchored) regex else "(.*/)?$regex"

  return finalPattern.toRegex(RegexOption.IGNORE_CASE)

}

private fun String.escapeRegexChars(): String =
  replace(Regex("""\W"""), """\\$0""")
