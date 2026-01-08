package dev.adamko.kntoolchain.tools.utils

import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

internal fun KotlinToolingVersion.toEscapedString(): String =
  toString().map { if (it.isLetterOrDigit()) it else '_' }.joinToString("")
