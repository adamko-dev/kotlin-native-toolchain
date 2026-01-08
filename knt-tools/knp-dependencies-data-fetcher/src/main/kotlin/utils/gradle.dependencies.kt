package dev.adamko.kntoolchain.tools.utils

internal fun createDependencyNotation(
  group: String?,
  name: String,
  version: String?,
  classifier: String?,
  extension: String?,
): String =
  buildString {
    if (group != null) append(group)
    append(":")
    append(name)
    append(":")
    if (version != null) append(version)
    if (classifier != null) {
      append(":")
      append(classifier)
    }
    if (extension != null) {
      append("@")
      append(extension)
    }
  }
