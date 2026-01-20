package dev.adamko.kntoolchain.tools.datamodel

import dev.adamko.kntoolchain.tools.datamodel.internal.KotlinToolingVersionEnc
import java.nio.file.Path
import kotlin.io.path.name
import kotlinx.serialization.Serializable

@Serializable
data class KotlinNativePrebuiltData(
  val data: Map<KotlinToolingVersionEnc, Set<PrebuiltVariant>>,
) {

  @Serializable
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

    fun toEscapedString(): String =
      toString().map { if (it.isLetterOrDigit()) it else '_' }.joinToString("")

    override fun compareTo(other: PrebuiltVariant): Int =
      this.toString().compareTo(other.toString())
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
