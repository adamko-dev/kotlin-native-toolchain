package dev.adamko.kntoolchain.tools.data.content

import dev.adamko.kntoolchain.tools.data.*

@Suppress("ClassName")
sealed class KnDependencyData_V2_0_0: KnDependencyDataSpec() {

  override val version: KnpVersion = KnpVersion.V2_0_0

  @Suppress("ClassName")
  sealed class Linux_X86_64: KnDependencyData_V2_0_0() {

    override val buildPlatform: KnBuildPlatform = KnBuildPlatform.Linux.X86_64

    object LinuxX64: Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native:lldb:4:linux@tar.gz"),
          KnpDependency("kotlin.native:llvm:11.1.0:linux-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
    }

    object LinuxArm32Hfp: Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm32Hfp

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9:1@tar.gz"),
          KnpDependency("kotlin.native:llvm:11.1.0:linux-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
          KnpDependency("kotlin.native:qemu-arm-static-5.1.0-linux:2@tar.gz"),
        )
    }

    object LinuxArm64: Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native:llvm:11.1.0:linux-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
          KnpDependency("kotlin.native:qemu-aarch64-static-5.1.0-linux:2@tar.gz"),
        )
    }

    object MingwX64: Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MingwX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@tar.gz"),
          KnpDependency("kotlin.native:llvm:11.1.0:linux-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
    }

    object AndroidX86: Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX86

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:linux-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:llvm:11.1.0:linux-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
    }

    object AndroidX64: Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:linux-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:llvm:11.1.0:linux-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
    }

    object AndroidArm32: Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:linux-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:llvm:11.1.0:linux-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
    }

    object AndroidArm64: Linux_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:linux-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:llvm:11.1.0:linux-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1-2-linux-x86:64@tar.gz"),
        )
    }
  }

  @Suppress("ClassName")
  sealed class MacOs_AArch64: KnDependencyData_V2_0_0() {

    override val buildPlatform: KnBuildPlatform = KnBuildPlatform.MacOs.AArch64

    object LinuxX64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object LinuxArm32Hfp: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm32Hfp

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9:1@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object LinuxArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object MingwX64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MingwX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object AndroidX86: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX86

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object AndroidX64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object AndroidArm32: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object AndroidArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object MacOsX64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MacOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object MacOsArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MacOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:lldb:4:macos@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object IOsArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object IOsX64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object IOsSimulatorArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object WatchOsArm32: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object WatchOsArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object WatchOsX64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object WatchOsSimulatorArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object WatchOsDeviceArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsDeviceArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object TvOsArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object TvOsX64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }

    object TvOsSimulatorArm64: MacOs_AArch64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-aarch64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.3:1:macos-arm64@tar.gz"),
        )
    }
  }

  @Suppress("ClassName")
  sealed class MacOs_X86_64: KnDependencyData_V2_0_0() {

    override val buildPlatform: KnBuildPlatform = KnBuildPlatform.MacOs.X86_64

    object LinuxX64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object LinuxArm32Hfp: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm32Hfp

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9:1@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object LinuxArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9:2@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object MingwX64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MingwX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object AndroidX86: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX86

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object AndroidX64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object AndroidArm32: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object AndroidArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@tar.gz"),
          KnpDependency("kotlin.native:target-toolchain:2:osx-android_ndk@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object MacOsX64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MacOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:lldb:4:macos@tar.gz"),
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object MacOsArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MacOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object IOsArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object IOsX64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object IOsSimulatorArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.IOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object WatchOsArm32: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object WatchOsArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object WatchOsX64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object WatchOsSimulatorArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object WatchOsDeviceArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.WatchOsDeviceArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object TvOsArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object TvOsX64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }

    object TvOsSimulatorArm64: MacOs_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.TvOsSimulatorArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:apple-llvm:20200714:macos-x64-essentials@tar.gz"),
          KnpDependency("kotlin.native:libffi-3.2.1:3:darwin-macos@tar.gz"),
        )
    }
  }

  @Suppress("ClassName")
  sealed class Windows_X86_64: KnDependencyData_V2_0_0() {

    override val buildPlatform: KnBuildPlatform = KnBuildPlatform.Windows.X86_64

    object LinuxX64: Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9:2@zip"),
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@zip"),
          KnpDependency("kotlin.native:llvm:11.1.0:windows-x64-essentials@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
    }

    object LinuxArm32Hfp: Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm32Hfp

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9:1@zip"),
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@zip"),
          KnpDependency("kotlin.native:llvm:11.1.0:windows-x64-essentials@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
    }

    object LinuxArm64: Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.LinuxArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9:2@zip"),
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@zip"),
          KnpDependency("kotlin.native:llvm:11.1.0:windows-x64-essentials@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
    }

    object MingwX64: Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.MingwX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:lldb:2:windows@zip"),
          KnpDependency("kotlin.native:lld:12.0.1:windows-x64@zip"),
          KnpDependency("kotlin.native:msys2-mingw-w64-x86_64:2@zip"),
          KnpDependency("kotlin.native:llvm:11.1.0:windows-x64-essentials@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
    }

    object AndroidX86: Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX86

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@zip"),
          KnpDependency("kotlin.native:target-toolchain:2:windows-android_ndk@zip"),
          KnpDependency("kotlin.native:llvm:11.1.0:windows-x64-essentials@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
    }

    object AndroidX64: Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidX64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@zip"),
          KnpDependency("kotlin.native:target-toolchain:2:windows-android_ndk@zip"),
          KnpDependency("kotlin.native:llvm:11.1.0:windows-x64-essentials@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
    }

    object AndroidArm32: Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm32

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@zip"),
          KnpDependency("kotlin.native:target-toolchain:2:windows-android_ndk@zip"),
          KnpDependency("kotlin.native:llvm:11.1.0:windows-x64-essentials@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
    }

    object AndroidArm64: Windows_X86_64() {

      override val compileTarget: KnCompileTarget = KnCompileTarget.AndroidArm64

      override val dependencies: Set<KnpDependency> =
        setOf(
          KnpDependency("kotlin.native:target-sysroot:1:android_ndk@zip"),
          KnpDependency("kotlin.native:target-toolchain:2:windows-android_ndk@zip"),
          KnpDependency("kotlin.native:llvm:11.1.0:windows-x64-essentials@zip"),
          KnpDependency("kotlin.native:libffi-3.3-windows-x64:1@zip"),
        )
    }
  }
}
