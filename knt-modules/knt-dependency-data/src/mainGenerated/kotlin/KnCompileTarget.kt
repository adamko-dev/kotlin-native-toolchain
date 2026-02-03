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

  enum class Architecture(
    val id: String,
  ) {
    X64("X64"),
    Arm32("ARM32"),
    Arm64("ARM64"),
    X86("X86"),
  }

  enum class OsFamily(
    val id: String,
  ) {
    Linux("LINUX"),
    Mingw("MINGW"),
    Android("ANDROID"),
    OsX("OSX"),
    IOs("IOS"),
    WatchOs("WATCHOS"),
    TvOs("TVOS"),
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
    buildString{
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
