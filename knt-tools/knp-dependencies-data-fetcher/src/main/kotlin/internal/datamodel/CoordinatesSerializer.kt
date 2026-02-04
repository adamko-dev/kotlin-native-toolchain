package dev.adamko.kntoolchain.tools.internal.datamodel

import kotlin.text.get
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object CoordinatesSerializer : KSerializer<KotlinVersionTargetDependencies.Coordinates> {

  override val descriptor: SerialDescriptor
    get() = String.serializer().descriptor

  private val regex: Regex =
    Regex("""(?<group>[^:]+):(?<module>[^:]+):(?<version>[^:@]+)(?::(?<classifier>[^@]+))?@(?<extension>[.a-z]+)""")

  override fun deserialize(decoder: Decoder): KotlinVersionTargetDependencies.Coordinates {
    val content = decoder.decodeString()
    val coords = content.substringBefore(ARTIFACT_SEPARATOR)
    val artifact = content.substringAfter(ARTIFACT_SEPARATOR, "").takeIf { it.isNotBlank() }

    val match = regex.matchEntire(coords)
      ?: error("Invalid dependency notation: $coords")

    val group = match.groups["group"]?.value
      ?: error("Missing 'group' in dependency notation: $coords")
    val module = match.groups["module"]?.value
      ?: error("Missing 'module' in dependency notation: $coords")
    val version = match.groups["version"]?.value
      ?: error("Missing 'version' in dependency notation: $coords")
    val classifier = match.groups["classifier"]?.value // Optional, can be null
    val extension = match.groups["extension"]?.value
      ?: error("Missing 'extension' in dependency notation: $coords")

    return KotlinVersionTargetDependencies.Coordinates(
      group = group,
      module = module,
      version = version,
      extension = extension,
      classifier = classifier,
      artifact = artifact,
    )
  }

  override fun serialize(encoder: Encoder, value: KotlinVersionTargetDependencies.Coordinates) {
    val coords = value.coords()

    encoder.encodeString(
      buildString {
        append(coords)
        if (value.artifact != null) {
          append(ARTIFACT_SEPARATOR)
          append(value.artifact)
        }
      }
    )
  }

  private const val ARTIFACT_SEPARATOR = " // "
}
