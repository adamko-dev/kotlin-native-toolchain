@file:OptIn(ExperimentalSerializationApi::class)

package dev.adamko.kntoolchain.tools.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

internal fun <T> Json.decodeFromFile(
  deserializer: DeserializationStrategy<T>,
  file: Path,
): T {
  file.inputStream().use { src ->
    return decodeFromStream(deserializer, src)
  }
}

internal fun <T> Json.encodeToFile(
  serializer: SerializationStrategy<T>,
  value: T,
  file: Path,
) {
  file.outputStream().use { src ->
    encodeToStream(serializer, value, src)
  }
}


internal inline fun <reified T> Json.encodeToFile(
  value: T,
  file: Path,
) {
  file.outputStream().use { src ->
    encodeToStream(value, src)
  }
}
