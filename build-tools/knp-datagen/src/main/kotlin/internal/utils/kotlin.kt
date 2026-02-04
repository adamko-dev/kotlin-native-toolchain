package dev.adamko.kntoolchain.tools.internal.utils

import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

/**
 * Replace non-alphanumeric characters with underscores.
 */
internal fun KotlinToolingVersion.toEscapedString(): String =
  toString().map { if (it.isLetterOrDigit()) it else '_' }.joinToString("")
