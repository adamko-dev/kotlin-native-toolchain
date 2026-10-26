package dev.adamko.kntoolchain.tools.data

/**
 * A target platform for Kotlin/Native code.
 */
class KnCompileTarget
private constructor(
  /** [org.jetbrains.kotlin.konan.target.KonanTarget.name] */
  val name: String,
  /** [org.jetbrains.kotlin.konan.target.KonanTarget.family] */
  val os: OsFamily,
  /** [org.jetbrains.kotlin.konan.target.KonanTarget.architecture] */
  val arch: Architecture,
): java.io.Serializable {

  sealed class Architecture(
    val id: String,
  ) : java.io.Serializable {
    companion object {
      @JvmStatic
      val X64: Architecture get() = X64Internal

      @JvmStatic
      val Arm32: Architecture get() = Arm32Internal

      @JvmStatic
      val Arm64: Architecture get() = Arm64Internal

      @JvmStatic
      val X86: Architecture get() = X86Internal

      @JvmStatic
      val entries: Set<Architecture> get() = setOf(X64, Arm32, Arm64, X86)
    }

    internal data object X64Internal : Architecture("X64") {
      override fun toString(): String = "Architecture.X64"

      @Suppress("unused")
      private fun readResolve(): Any = X64Internal
    }

    internal data object Arm32Internal : Architecture("ARM32") {
      override fun toString(): String = "Architecture.Arm32"

      @Suppress("unused")
      private fun readResolve(): Any = Arm32Internal
    }

    internal data object Arm64Internal : Architecture("ARM64") {
      override fun toString(): String = "Architecture.Arm64"

      @Suppress("unused")
      private fun readResolve(): Any = Arm64Internal
    }

    internal data object X86Internal : Architecture("X86") {
      override fun toString(): String = "Architecture.X86"

      @Suppress("unused")
      private fun readResolve(): Any = X86Internal
    }
  }

  sealed class OsFamily(
    val id: String,
  ) : java.io.Serializable {
    companion object {
      @JvmStatic
      val Linux: OsFamily get() = LinuxInternal

      @JvmStatic
      val Mingw: OsFamily get() = MingwInternal

      @JvmStatic
      val Android: OsFamily get() = AndroidInternal

      @JvmStatic
      val OsX: OsFamily get() = OsXInternal

      @JvmStatic
      val IOs: OsFamily get() = IOsInternal

      @JvmStatic
      val WatchOs: OsFamily get() = WatchOsInternal

      @JvmStatic
      val TvOs: OsFamily get() = TvOsInternal

      @JvmStatic
      val entries: Set<OsFamily> get() = setOf(Linux, Mingw, Android, OsX, IOs, WatchOs, TvOs)
    }

    internal data object LinuxInternal : OsFamily("LINUX") {
      override fun toString(): String = "OsFamily.Linux"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxInternal
    }

    internal data object MingwInternal : OsFamily("MINGW") {
      override fun toString(): String = "OsFamily.Mingw"

      @Suppress("unused")
      private fun readResolve(): Any = MingwInternal
    }

    internal data object AndroidInternal : OsFamily("ANDROID") {
      override fun toString(): String = "OsFamily.Android"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidInternal
    }

    internal data object OsXInternal : OsFamily("OSX") {
      override fun toString(): String = "OsFamily.OsX"

      @Suppress("unused")
      private fun readResolve(): Any = OsXInternal
    }

    internal data object IOsInternal : OsFamily("IOS") {
      override fun toString(): String = "OsFamily.IOs"

      @Suppress("unused")
      private fun readResolve(): Any = IOsInternal
    }

    internal data object WatchOsInternal : OsFamily("WATCHOS") {
      override fun toString(): String = "OsFamily.WatchOs"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsInternal
    }

    internal data object TvOsInternal : OsFamily("TVOS") {
      override fun toString(): String = "OsFamily.TvOs"

      @Suppress("unused")
      private fun readResolve(): Any = TvOsInternal
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is KnCompileTarget) return false
    if (name != other.name) return false
    if (os != other.os) return false
    if (arch != other.arch) return false
    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + os.hashCode()
    result = 31 * result + arch.hashCode()
    return result
  }

  override fun toString(): String =
    buildString {
      append("KnCompileTarget(")
      append(name)
      append(")")
    }

  companion object {
    val allTargets: Set<KnCompileTarget> by lazy {
      setOf(
        LinuxX64,
        LinuxArm32Hfp,
        LinuxArm64,
        MingwX64,
        AndroidX86,
        AndroidX64,
        AndroidArm32,
        AndroidArm64,
        MacOsX64,
        MacOsArm64,
        IOsArm64,
        IOsX64,
        IOsSimulatorArm64,
        WatchOsArm32,
        WatchOsArm64,
        WatchOsX64,
        WatchOsSimulatorArm64,
        WatchOsDeviceArm64,
        TvOsArm64,
        TvOsX64,
        TvOsSimulatorArm64,
      )
    }

    val LinuxX64: KnCompileTarget =
      KnCompileTarget(
        name = "linux_x64",
        os = OsFamily.Linux,
        arch = Architecture.X64,
      )

    val LinuxArm32Hfp: KnCompileTarget =
      KnCompileTarget(
        name = "linux_arm32_hfp",
        os = OsFamily.Linux,
        arch = Architecture.Arm32,
      )

    val LinuxArm64: KnCompileTarget =
      KnCompileTarget(
        name = "linux_arm64",
        os = OsFamily.Linux,
        arch = Architecture.Arm64,
      )

    val MingwX64: KnCompileTarget =
      KnCompileTarget(
        name = "mingw_x64",
        os = OsFamily.Mingw,
        arch = Architecture.X64,
      )

    val AndroidX86: KnCompileTarget =
      KnCompileTarget(
        name = "android_x86",
        os = OsFamily.Android,
        arch = Architecture.X86,
      )

    val AndroidX64: KnCompileTarget =
      KnCompileTarget(
        name = "android_x64",
        os = OsFamily.Android,
        arch = Architecture.X64,
      )

    val AndroidArm32: KnCompileTarget =
      KnCompileTarget(
        name = "android_arm32",
        os = OsFamily.Android,
        arch = Architecture.Arm32,
      )

    val AndroidArm64: KnCompileTarget =
      KnCompileTarget(
        name = "android_arm64",
        os = OsFamily.Android,
        arch = Architecture.Arm64,
      )

    val MacOsX64: KnCompileTarget =
      KnCompileTarget(
        name = "macos_x64",
        os = OsFamily.OsX,
        arch = Architecture.X64,
      )

    val MacOsArm64: KnCompileTarget =
      KnCompileTarget(
        name = "macos_arm64",
        os = OsFamily.OsX,
        arch = Architecture.Arm64,
      )

    val IOsArm64: KnCompileTarget =
      KnCompileTarget(
        name = "ios_arm64",
        os = OsFamily.IOs,
        arch = Architecture.Arm64,
      )

    val IOsX64: KnCompileTarget =
      KnCompileTarget(
        name = "ios_x64",
        os = OsFamily.IOs,
        arch = Architecture.X64,
      )

    val IOsSimulatorArm64: KnCompileTarget =
      KnCompileTarget(
        name = "ios_simulator_arm64",
        os = OsFamily.IOs,
        arch = Architecture.Arm64,
      )

    val WatchOsArm32: KnCompileTarget =
      KnCompileTarget(
        name = "watchos_arm32",
        os = OsFamily.WatchOs,
        arch = Architecture.Arm32,
      )

    val WatchOsArm64: KnCompileTarget =
      KnCompileTarget(
        name = "watchos_arm64",
        os = OsFamily.WatchOs,
        arch = Architecture.Arm64,
      )

    val WatchOsX64: KnCompileTarget =
      KnCompileTarget(
        name = "watchos_x64",
        os = OsFamily.WatchOs,
        arch = Architecture.X64,
      )

    val WatchOsSimulatorArm64: KnCompileTarget =
      KnCompileTarget(
        name = "watchos_simulator_arm64",
        os = OsFamily.WatchOs,
        arch = Architecture.Arm64,
      )

    val WatchOsDeviceArm64: KnCompileTarget =
      KnCompileTarget(
        name = "watchos_device_arm64",
        os = OsFamily.WatchOs,
        arch = Architecture.Arm64,
      )

    val TvOsArm64: KnCompileTarget =
      KnCompileTarget(
        name = "tvos_arm64",
        os = OsFamily.TvOs,
        arch = Architecture.Arm64,
      )

    val TvOsX64: KnCompileTarget =
      KnCompileTarget(
        name = "tvos_x64",
        os = OsFamily.TvOs,
        arch = Architecture.X64,
      )

    val TvOsSimulatorArm64: KnCompileTarget =
      KnCompileTarget(
        name = "tvos_simulator_arm64",
        os = OsFamily.TvOs,
        arch = Architecture.Arm64,
      )
  }
}
