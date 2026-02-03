package dev.adamko.kntoolchain.tools.datamodel

import dev.adamko.kntoolchain.tools.datamodel.KonanTargetData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class KotlinVersionTargetDependencies(
  val version: String,
  /** The platform on which the compilation tools are executed. */
  val buildPlatform: Platform,
  /** The platform that the compiler will generate code for. */
  val targetPlatform: KonanTargetData,
  val dependencies: Set<Coordinates>,
) {
  @Serializable(with = CoordinatesSerializer::class)
  data class Coordinates(
    val group: String,
    val module: String,
    val version: String,
    val extension: String,
    val classifier: String?,
    val artifact: String? = null,
  ) {
    fun coords(): String {
      return buildString {
        append(group)
        append(":")
        append(module)
        append(":")
        append(version)
        if (classifier != null) {
          append(":")
          append(classifier)
        }
        append("@")
        append(extension)
      }
    }
  }
}


private object CoordinatesSerializer : KSerializer<KotlinVersionTargetDependencies.Coordinates> {

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
