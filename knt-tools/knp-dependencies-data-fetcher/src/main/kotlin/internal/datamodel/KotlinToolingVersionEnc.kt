package dev.adamko.kntoolchain.tools.internal.datamodel

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

internal typealias KotlinToolingVersionEnc =
    @Serializable(with = KotlinToolingVersionSerializer::class)
    KotlinToolingVersion

internal object KotlinToolingVersionSerializer : KSerializer<KotlinToolingVersion> {
  override val descriptor: SerialDescriptor
    get() = String.serializer().descriptor

  override fun deserialize(decoder: Decoder): KotlinToolingVersion {
    return KotlinToolingVersion(decoder.decodeString())
  }

  override fun serialize(encoder: Encoder, value: KotlinToolingVersion) {
    encoder.encodeString(value.toString())
  }
}
