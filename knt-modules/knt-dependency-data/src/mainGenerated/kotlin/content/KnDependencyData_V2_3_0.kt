package dev.adamko.kntoolchain.tools.data.content

import dev.adamko.kntoolchain.tools.data.*

@Suppress("ClassName")
sealed class KnDependencyData_V2_3_0: KnDependencyDataSpec() {

  override val version: KnpVersion = KnpVersion.V2_3_0

  sealed class Linux_X86_64: KnDependencyData_V2_3_0() {

    override val buildPlatform: KnBuildPlatform = KnBuildPlatform.Linux.X86_64

    data object LinuxX64 : Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native:lldb:4:linux@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-linux:llvm-19-x86_64-linux-essentials:103@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Linux_X86_64.LinuxX64"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxX64
    }

    data object LinuxArm32Hfp : Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm32Hfp

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9:1@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-linux:llvm-19-x86_64-linux-essentials:103@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
          KnpDependency("kotlin.native:qemu-arm-static-5.1.0-linux:2@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Linux_X86_64.LinuxArm32Hfp"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxArm32Hfp
    }

    data object LinuxArm64 : Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-linux:llvm-19-x86_64-linux-essentials:103@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
          KnpDependency("kotlin.native:qemu-aarch64-static-5.1.0-linux:2@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Linux_X86_64.LinuxArm64"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxArm64
    }

    data object MingwX64 : Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MingwX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-linux:llvm-19-x86_64-linux-essentials:103@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Linux_X86_64.MingwX64"

      @Suppress("unused")
      private fun readResolve(): Any = MingwX64
    }

    data object AndroidX86 : Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX86

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:linux-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-linux:llvm-19-x86_64-linux-essentials:103@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Linux_X86_64.AndroidX86"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidX86
    }

    data object AndroidX64 : Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:linux-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-linux:llvm-19-x86_64-linux-essentials:103@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Linux_X86_64.AndroidX64"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidX64
    }

    data object AndroidArm32 : Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:linux-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-linux:llvm-19-x86_64-linux-essentials:103@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Linux_X86_64.AndroidArm32"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidArm32
    }

    data object AndroidArm64 : Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:linux-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-linux:llvm-19-x86_64-linux-essentials:103@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Linux_X86_64.AndroidArm64"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidArm64
    }
  }

  sealed class MacOs_AArch64: KnDependencyData_V2_3_0() {

    override val buildPlatform: KnBuildPlatform = KnBuildPlatform.MacOs.AArch64

    data object LinuxX64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.LinuxX64"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxX64
    }

    data object LinuxArm32Hfp : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm32Hfp

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9:1@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.LinuxArm32Hfp"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxArm32Hfp
    }

    data object LinuxArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.LinuxArm64"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxArm64
    }

    data object MingwX64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MingwX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.MingwX64"

      @Suppress("unused")
      private fun readResolve(): Any = MingwX64
    }

    data object AndroidX86 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX86

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.AndroidX86"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidX86
    }

    data object AndroidX64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.AndroidX64"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidX64
    }

    data object AndroidArm32 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.AndroidArm32"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidArm32
    }

    data object AndroidArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.AndroidArm64"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidArm64
    }

    data object MacOsX64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MacOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.MacOsX64"

      @Suppress("unused")
      private fun readResolve(): Any = MacOsX64
    }

    data object MacOsArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MacOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:lldb:4:macos@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.MacOsArm64"

      @Suppress("unused")
      private fun readResolve(): Any = MacOsArm64
    }

    data object IOsArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.IOsArm64"

      @Suppress("unused")
      private fun readResolve(): Any = IOsArm64
    }

    data object IOsX64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.IOsX64"

      @Suppress("unused")
      private fun readResolve(): Any = IOsX64
    }

    data object IOsSimulatorArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.IOsSimulatorArm64"

      @Suppress("unused")
      private fun readResolve(): Any = IOsSimulatorArm64
    }

    data object WatchOsArm32 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.WatchOsArm32"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsArm32
    }

    data object WatchOsArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.WatchOsArm64"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsArm64
    }

    data object WatchOsX64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.WatchOsX64"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsX64
    }

    data object WatchOsSimulatorArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.WatchOsSimulatorArm64"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsSimulatorArm64
    }

    data object WatchOsDeviceArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsDeviceArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.WatchOsDeviceArm64"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsDeviceArm64
    }

    data object TvOsArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.TvOsArm64"

      @Suppress("unused")
      private fun readResolve(): Any = TvOsArm64
    }

    data object TvOsX64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.TvOsX64"

      @Suppress("unused")
      private fun readResolve(): Any = TvOsX64
    }

    data object TvOsSimulatorArm64 : MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-aarch64-macos:llvm-19-aarch64-macos-essentials:79@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_AArch64.TvOsSimulatorArm64"

      @Suppress("unused")
      private fun readResolve(): Any = TvOsSimulatorArm64
    }
  }

  sealed class MacOs_X86_64: KnDependencyData_V2_3_0() {

    override val buildPlatform: KnBuildPlatform = KnBuildPlatform.MacOs.X86_64

    data object LinuxX64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.LinuxX64"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxX64
    }

    data object LinuxArm32Hfp : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm32Hfp

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9:1@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.LinuxArm32Hfp"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxArm32Hfp
    }

    data object LinuxArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.LinuxArm64"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxArm64
    }

    data object MingwX64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MingwX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.MingwX64"

      @Suppress("unused")
      private fun readResolve(): Any = MingwX64
    }

    data object AndroidX86 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX86

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.AndroidX86"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidX86
    }

    data object AndroidX64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.AndroidX64"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidX64
    }

    data object AndroidArm32 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.AndroidArm32"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidArm32
    }

    data object AndroidArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.AndroidArm64"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidArm64
    }

    data object MacOsX64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MacOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:lldb:4:macos@tar.gz"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.MacOsX64"

      @Suppress("unused")
      private fun readResolve(): Any = MacOsX64
    }

    data object MacOsArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MacOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.MacOsArm64"

      @Suppress("unused")
      private fun readResolve(): Any = MacOsArm64
    }

    data object IOsArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.IOsArm64"

      @Suppress("unused")
      private fun readResolve(): Any = IOsArm64
    }

    data object IOsX64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.IOsX64"

      @Suppress("unused")
      private fun readResolve(): Any = IOsX64
    }

    data object IOsSimulatorArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.IOsSimulatorArm64"

      @Suppress("unused")
      private fun readResolve(): Any = IOsSimulatorArm64
    }

    data object WatchOsArm32 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.WatchOsArm32"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsArm32
    }

    data object WatchOsArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.WatchOsArm64"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsArm64
    }

    data object WatchOsX64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.WatchOsX64"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsX64
    }

    data object WatchOsSimulatorArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.WatchOsSimulatorArm64"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsSimulatorArm64
    }

    data object WatchOsDeviceArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsDeviceArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.WatchOsDeviceArm64"

      @Suppress("unused")
      private fun readResolve(): Any = WatchOsDeviceArm64
    }

    data object TvOsArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.TvOsArm64"

      @Suppress("unused")
      private fun readResolve(): Any = TvOsArm64
    }

    data object TvOsX64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.TvOsX64"

      @Suppress("unused")
      private fun readResolve(): Any = TvOsX64
    }

    data object TvOsSimulatorArm64 : MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-macos:llvm-19-x86_64-macos-essentials:75@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.MacOs_X86_64.TvOsSimulatorArm64"

      @Suppress("unused")
      private fun readResolve(): Any = TvOsSimulatorArm64
    }
  }

  sealed class Windows_X86_64: KnDependencyData_V2_3_0() {

    override val buildPlatform: KnBuildPlatform = KnBuildPlatform.Windows.X86_64

    data object LinuxX64 : Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9:2@zip"),
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@zip"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-windows:llvm-19-x86_64-windows-essentials:134@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Windows_X86_64.LinuxX64"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxX64
    }

    data object LinuxArm32Hfp : Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm32Hfp

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9:1@zip"),
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@zip"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-windows:llvm-19-x86_64-windows-essentials:134@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Windows_X86_64.LinuxArm32Hfp"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxArm32Hfp
    }

    data object LinuxArm64 : Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9:2@zip"),
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@zip"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-windows:llvm-19-x86_64-windows-essentials:134@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Windows_X86_64.LinuxArm64"

      @Suppress("unused")
      private fun readResolve(): Any = LinuxArm64
    }

    data object MingwX64 : Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MingwX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:lldb:2:windows@zip"),
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@zip"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-windows:llvm-19-x86_64-windows-essentials:134@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Windows_X86_64.MingwX64"

      @Suppress("unused")
      private fun readResolve(): Any = MingwX64
    }

    data object AndroidX86 : Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX86

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@zip"),
          KnpDependency("kotlin.native:target-toolchain:2:windows-android_ndk@zip"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-windows:llvm-19-x86_64-windows-essentials:134@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Windows_X86_64.AndroidX86"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidX86
    }

    data object AndroidX64 : Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@zip"),
          KnpDependency("kotlin.native:target-toolchain:2:windows-android_ndk@zip"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-windows:llvm-19-x86_64-windows-essentials:134@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Windows_X86_64.AndroidX64"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidX64
    }

    data object AndroidArm32 : Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@zip"),
          KnpDependency("kotlin.native:target-toolchain:2:windows-android_ndk@zip"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-windows:llvm-19-x86_64-windows-essentials:134@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Windows_X86_64.AndroidArm32"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidArm32
    }

    data object AndroidArm64 : Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@zip"),
          KnpDependency("kotlin.native:target-toolchain:2:windows-android_ndk@zip"),
          KnpDependency("kotlin.native.resources.llvm.19-x86_64-windows:llvm-19-x86_64-windows-essentials:134@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
      override fun toString(): String = "KnDependencyData_V2_3_0.Windows_X86_64.AndroidArm64"

      @Suppress("unused")
      private fun readResolve(): Any = AndroidArm64
    }
  }
}
