package dev.adamko.kntoolchain.tools.data

/**
 * A platform that can build Kotlin/Native code.
 */
class KnBuildPlatform
private constructor(
  val family: OsFamily,
  val arch: OsArch,
): java.io.Serializable {

  enum class OsFamily(
    val value: String,
  ) {
    Linux("linux"),
    MacOs("macos"),
    Windows("windows"),
  }

  enum class OsArch(
    val value: String,
  ) {
    AArch64("aarch64"),
    X86_64("x86_64"),
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is KnBuildPlatform) return false
    if (family != other.family) return false
    if (arch != other.arch) return false
    return true
  }

  override fun hashCode(): Int {
    var result = family.hashCode()
    result = 31 * result + arch.hashCode()
    return result
  }

  override fun toString(): String =
    buildString{
      append("KnBuildPlatform(")
      append(family)
      append("-")
      append(arch)
      append(")")
    }


  object Linux {

    val X86_64: KnBuildPlatform =
      KnBuildPlatform(
        family = OsFamily.Linux,
        arch = OsArch.X86_64,
      )
  }

  object MacOs {

    val AArch64: KnBuildPlatform =
      KnBuildPlatform(
        family = OsFamily.MacOs,
        arch = OsArch.AArch64,
      )

    val X86_64: KnBuildPlatform =
      KnBuildPlatform(
        family = OsFamily.MacOs,
        arch = OsArch.X86_64,
      )
  }

  object Windows {

    val X86_64: KnBuildPlatform =
      KnBuildPlatform(
        family = OsFamily.Windows,
        arch = OsArch.X86_64,
      )
  }
  companion object {
    val allPlatforms: Set<KnBuildPlatform> by lazy {
      setOf(
        Linux.X86_64,
        MacOs.AArch64,
        MacOs.X86_64,
        Windows.X86_64,
      )
    }
  }
}
