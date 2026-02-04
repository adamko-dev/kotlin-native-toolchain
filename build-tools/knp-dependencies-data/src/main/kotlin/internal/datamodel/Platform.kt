package dev.adamko.kntoolchain.tools.internal.datamodel

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Platform.Serializer::class)
internal data class Platform(
  val osFamily: String,
  val osArch: String,
) : java.io.Serializable, Comparable<Platform> {

  // must match the values expected by Konan's HostManager
  val osNameForJavaSystemProperty: String =
    when (osFamily) {
      "windows" -> "Windows"
      "macos"   -> "Mac OS X"
      "linux"   -> "Linux"
      else      -> error("Unsupported OS family: $osFamily")
    }

  override fun compareTo(other: Platform): Int =
    compareValuesBy(this, other, Platform::osFamily, Platform::osArch)

  override fun toString(): String {
    return buildString {
      append("Platform(")
      append(osFamily)
      append("-")
      append(osArch)
      append(")")
    }
  }

  object Serializer : KSerializer<Platform> {
    override val descriptor: SerialDescriptor
      get() = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Platform {
      val value = decoder.decodeString()
      val (osFamily, osArch) = value.split(SEPARATOR, limit = 2)
      return Platform(
        osFamily = osFamily,
        osArch = osArch,
      )
    }

    override fun serialize(encoder: Encoder, value: Platform) {
      encoder.encodeString("${value.osFamily}${SEPARATOR}${value.osArch}")
    }

    private const val SEPARATOR = ":"
  }
}
