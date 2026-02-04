package dev.adamko.kntoolchain.tools.internal

import kotlinx.serialization.json.Json

internal val json: Json =
  Json {
    prettyPrint = true
    prettyPrintIndent = "  "
  }
