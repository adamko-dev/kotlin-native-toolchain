package dev.adamko.kntoolchain.tools.data

/**
 * A platform that can build Kotlin/Native code.
 */
class KnBuildPlatform
private constructor(
  val family: OsFamily,
  val arch: OsArch,
) : java.io.Serializable {

  sealed class OsFamily(
    val name: String,
  ) : java.io.Serializable {
    companion object {
      @JvmStatic
      val Linux: OsFamily get() = LinuxInternal

      @JvmStatic
      val MacOs: OsFamily get() = MacOsInternal

      @JvmStatic
      val Windows: OsFamily get() = WindowsInternal

      @JvmStatic
      val entries: Set<OsFamily> get() = setOf(Linux, MacOs, Windows)
    }

    internal data object LinuxInternal : OsFamily("linux") {
      override fun toString(): String = "OsFamily.Linux"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxInternal
    }

    internal data object MacOsInternal : OsFamily("macos") {
      override fun toString(): String = "OsFamily.MacOs"

      @Suppress("unused")
      private fun readResolve(): Any = MacOsInternal
    }

    internal data object WindowsInternal : OsFamily("windows") {
      override fun toString(): String = "OsFamily.Windows"

      @Suppress("unused")
      private fun readResolve(): Any = WindowsInternal
    }
  }

  sealed class OsArch(
    val name: String,
  ) : java.io.Serializable {
    companion object {
      @JvmStatic
      val AArch64: OsArch get() = AArch64Internal

      @JvmStatic
      val X86_64: OsArch get() = X8664Internal

      @JvmStatic
      val entries: Set<OsArch> get() = setOf(AArch64, X86_64)
    }

    internal data object AArch64Internal : OsArch("aarch64") {
      override fun toString(): String = "OsArch.AArch64"

      @Suppress("unused")
      private fun readResolve(): Any = AArch64Internal
    }

    internal data object X8664Internal : OsArch("x86_64") {
      override fun toString(): String = "OsArch.X86_64"

      @Suppress("unused")
      private fun readResolve(): Any = X8664Internal
    }
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
    buildString {
      append("KnBuildPlatform(")
      append(family.name)
      append("-")
      append(arch.name)
      append(")")
    }

  object Linux {
    @JvmStatic
    val X86_64: KnBuildPlatform =
      KnBuildPlatform(
        family = OsFamily.Linux,
        arch = OsArch.X86_64,
      )
  }

  object MacOs {
    @JvmStatic
    val AArch64: KnBuildPlatform =
      KnBuildPlatform(
        family = OsFamily.MacOs,
        arch = OsArch.AArch64,
      )

    @JvmStatic
    val X86_64: KnBuildPlatform =
      KnBuildPlatform(
        family = OsFamily.MacOs,
        arch = OsArch.X86_64,
      )
  }

  object Windows {
    @JvmStatic
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
