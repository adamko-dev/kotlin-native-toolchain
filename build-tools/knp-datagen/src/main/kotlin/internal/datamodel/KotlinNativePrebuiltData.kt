package dev.adamko.kntoolchain.tools.internal.datamodel

import java.nio.file.Path
import kotlin.io.path.name
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
internal data class KotlinNativePrebuiltData(
  val data: Map<KotlinToolingVersionEnc, Set<PrebuiltVariant>>,
) {

  @Serializable(with = PrebuiltVariant.Serializer::class)
  data class PrebuiltVariant(
    /**
     * `$os-$arch`, e.g. `linux-x86_64` or `macos-aarch64`.
     *
     * Will be used as the classifier when creating a Gradle dependency.
     */
    val classifier: String,
    val archiveType: ArchiveType,
  ) : Comparable<PrebuiltVariant> {

    override fun toString(): String =
      "$classifier-${archiveType.dependencyExtension}"

    override fun compareTo(other: PrebuiltVariant): Int =
      this.toString().compareTo(other.toString())

    object Serializer : KSerializer<PrebuiltVariant> {
      override val descriptor: SerialDescriptor = String.serializer().descriptor
      private const val SEPARATOR = "@"

      override fun serialize(encoder: Encoder, value: PrebuiltVariant) {
        encoder.encodeString(value.classifier + SEPARATOR + value.archiveType.name)
      }

      override fun deserialize(decoder: Decoder): PrebuiltVariant {
        val value = decoder.decodeString()
        val (classifier, archiveType) = value
          .split(SEPARATOR, limit = 2)
        return PrebuiltVariant(
          classifier = classifier,
          archiveType = ArchiveType.valueOf(archiveType)
        )
      }
    }

  }

  @Serializable
  enum class ArchiveType(
    val fileExtension: String,
    val dependencyExtension: String = fileExtension.removePrefix(".")
  ) {
    Zip(".zip"),
    Jar(".jar"),
    TarGz(".tar.gz"),
    ;

    companion object {
      fun fromFile(file: Path): ArchiveType =
        fromFileOrNull(file) ?: error("Unsupported archive type: $file")

      fun fromFileOrNull(file: Path): ArchiveType? =
        fromFileNameOrNull(file.name)

      fun fromFileNameOrNull(fileName: String): ArchiveType? =
        enumValues<ArchiveType>().firstOrNull { fileName.endsWith(it.fileExtension) }
    }
  }
}
