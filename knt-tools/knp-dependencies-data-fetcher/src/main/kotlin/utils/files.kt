package dev.adamko.kntoolchain.tools.utils

import java.nio.file.Path
import kotlin.io.path.exists

internal fun Path.takeIfExists(): Path? =
  takeIf { it.exists() }
