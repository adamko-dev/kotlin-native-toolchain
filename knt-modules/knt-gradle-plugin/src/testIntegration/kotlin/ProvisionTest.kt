package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.test_utils.GradleTestContext
import dev.adamko.kntoolchain.test_utils.junit.KnpOsArchArgs
import dev.adamko.kntoolchain.test_utils.projects.setupProjectForKnpDownload
import dev.adamko.kntoolchain.test_utils.toTreeString
import dev.adamko.kntoolchain.tools.data.KnBuildPlatform
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class ProvisionTest {

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  fun `verify konan data dir provisioned successfully`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    setupProjectForKnpDownload(
      buildPlatform = buildPlatform,
    )

    buildGradleKts += """
        |val knToolchainUser by tasks.registering {
        |  val knToolchain = knToolchain.provisionInstallation()
        |
        |  doLast {
        |    val knToolchain = knToolchain.get()
        |    println("knToolchain dir: {" + knToolchain.invariantSeparatorsPathString + "}")
        |  }
        |}
        |""".trimMargin()

    val expectedDependencyCount = when (buildPlatform.family) {
      KnBuildPlatform.OsFamily.MacOs   -> when (buildPlatform.arch) {
        KnBuildPlatform.OsArch.X86_64  -> 10
        KnBuildPlatform.OsArch.AArch64 -> 10
      }

      KnBuildPlatform.OsFamily.Windows -> 10
      KnBuildPlatform.OsFamily.Linux   -> 12
    }

    runner
      .withArguments(
        "knToolchainUser",
      )
      .forwardOutput()
      .build()
      .apply {

        output shouldNotContain "partially complete installations detected"
        output shouldContain "Finished installing/checking Konan and $expectedDependencyCount dependencies in"
        output shouldContain "knToolchain dir: {${konanDataDir.invariantSeparatorsPathString}/kotlin-native-prebuilt-2.3.0-${buildPlatform.family.value}-${buildPlatform.arch.value}}"

        konanDataDir.toTreeString() shouldBe when (buildPlatform.family) {
          KnBuildPlatform.OsFamily.MacOs   -> when (buildPlatform.arch) {
            KnBuildPlatform.OsArch.AArch64 -> ExpectedDirectory.MacOS.AARCH_64
            KnBuildPlatform.OsArch.X86_64  -> ExpectedDirectory.MacOS.X86_64
          }

          KnBuildPlatform.OsFamily.Windows -> ExpectedDirectory.Windows.X86_64
          KnBuildPlatform.OsFamily.Linux   -> ExpectedDirectory.Linux.X86_64
        }.trimIndent()
      }
  }

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  @DisabledOnOs(WINDOWS) // can't execute `run_konan.sh` on Windows
  fun `verify run_konan source successfully`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    // can't execute `run_konan.bat` on Linux/MacOS
    assumeTrue(buildPlatform.family != KnBuildPlatform.OsFamily.Windows)

    setupProjectForKnpDownload(
      buildPlatform = buildPlatform,
    )

    registerRunKonanTask()

    runner
      .withArguments(
        "runKonanTask",
      )
      .forwardOutput()
      .build()
      .apply {
        val expectedRunKonanPath =
          konanDataDir.resolve("kotlin-native-prebuilt-2.3.0-${buildPlatform.family.value}-${buildPlatform.arch.value}/bin/run_konan")
        output shouldContain "run_konan path: {${expectedRunKonanPath.invariantSeparatorsPathString}}"
        output shouldContain "run_konan output: {run_konan :runKonanTask}"
      }
  }

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  fun `when dry-run used - expect no toolchains installed`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    setupProjectForKnpDownload(
      buildPlatform = buildPlatform,
    )

    registerRunKonanTask()

    runner
      .withArguments(
        "runKonanTask",
        "--dry-run",
      )
      .forwardOutput()
      .build()
      .apply {
        konanDataDir.shouldNotExist()
      }
  }

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  @DisabledOnOs(WINDOWS) // can't execute `run_konan.sh` on Windows
  fun `verify disable kn target`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

//    assumeFalse(knpOs == OsFamily.Windows) // can't execute `run_konan.bat` on Linux/MacOS

    setupProjectForKnpDownload(
      buildPlatform = buildPlatform
    )

    buildGradleKts += """
        |val knToolchainUser by tasks.registering {
        |  val knToolchain = knToolchain.provisionInstallation()
        |
        |  doLast {
        |    val knToolchain = knToolchain.get()
        |    println("knToolchain dir: {" + knToolchain.invariantSeparatorsPathString + "}")
        |  }
        |}
        |""".trimMargin()

    buildGradleKts += """
        |knToolchain {
        |  kotlinNativePrebuiltDistribution {
        |    compileTargets = setOf(KnCompileTarget.LinuxX64)
        |  }
        |}
        """.trimMargin()

    runner
      .withArguments(
        "knToolchainUser",
      )
      .forwardOutput()
      .build()
      .apply {

        output shouldNotContain "partially complete installations detected"
        output shouldContain "knToolchain dir: {${konanDataDir.invariantSeparatorsPathString}/kotlin-native-prebuilt-2.3.0-${buildPlatform.family.value}-${buildPlatform.arch.value}}"

        konanDataDir.toTreeString() shouldBe when (buildPlatform.family) {
          KnBuildPlatform.OsFamily.MacOs   -> when (buildPlatform.arch) {
            KnBuildPlatform.OsArch.AArch64 ->
              """
              konan-data/
              ├── checksums/
              │   ├── dependencies_libffi-3.3-1-macos-arm64.hash
              │   ├── dependencies_llvm-19-aarch64-macos-essentials-79.hash
              │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
              │   ├── kotlin-native-prebuilt-2.3.0-macos-aarch64.hash
              │   ├── kotlin-native-prebuilt-2.3.0-macos-aarch64.tar.gz.hash
              │   ├── libffi-3.3-1-macos-arm64.tar.gz.hash
              │   ├── llvm-19-aarch64-macos-essentials-79.tar.gz.hash
              │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.tar.gz.hash
              ├── dependencies/
              │   ├── libffi-3.3-1-macos-arm64/
              │   │   ├── CACHEDIR.TAG
              │   │   └── demo-content.txt
              │   ├── llvm-19-aarch64-macos-essentials-79/
              │   │   ├── CACHEDIR.TAG
              │   │   └── demo-content.txt
              │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
              │       ├── CACHEDIR.TAG
              │       └── demo-content.txt
              └── kotlin-native-prebuilt-2.3.0-macos-aarch64/
                  ├── bin/
                  │   └── run_konan
                  ├── CACHEDIR.TAG
                  └── demo-content.txt
              """.trimIndent()

            KnBuildPlatform.OsArch.X86_64  ->
              """
              konan-data/
              ├── checksums/
              │   ├── dependencies_libffi-3.2.1-3-darwin-macos.hash
              │   ├── dependencies_llvm-19-x86_64-macos-essentials-75.hash
              │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
              │   ├── kotlin-native-prebuilt-2.3.0-macos-x86_64.hash
              │   ├── kotlin-native-prebuilt-2.3.0-macos-x86_64.tar.gz.hash
              │   ├── libffi-3.2.1-3-darwin-macos.tar.gz.hash
              │   ├── llvm-19-x86_64-macos-essentials-75.tar.gz.hash
              │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.tar.gz.hash
              ├── dependencies/
              │   ├── libffi-3.2.1-3-darwin-macos/
              │   │   ├── CACHEDIR.TAG
              │   │   └── demo-content.txt
              │   ├── llvm-19-x86_64-macos-essentials-75/
              │   │   ├── CACHEDIR.TAG
              │   │   └── demo-content.txt
              │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
              │       ├── CACHEDIR.TAG
              │       └── demo-content.txt
              └── kotlin-native-prebuilt-2.3.0-macos-x86_64/
                  ├── bin/
                  │   └── run_konan
                  ├── CACHEDIR.TAG
                  └── demo-content.txt
              """.trimIndent()
          }

          KnBuildPlatform.OsFamily.Windows ->
            """
            konan-data/
            ├── checksums/
            │   ├── dependencies_libffi-3.3-windows-x64-1.hash
            │   ├── dependencies_llvm-19-x86_64-windows-essentials-134.hash
            │   ├── dependencies_msys2-mingw-w64-x86_64-2.hash
            │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
            │   ├── kotlin-native-prebuilt-2.3.0-windows-x86_64.hash
            │   ├── kotlin-native-prebuilt-2.3.0-windows-x86_64.zip.hash
            │   ├── libffi-3.3-windows-x64-1.zip.hash
            │   ├── llvm-19-x86_64-windows-essentials-134.zip.hash
            │   ├── msys2-mingw-w64-x86_64-2.zip.hash
            │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.zip.hash
            ├── dependencies/
            │   ├── libffi-3.3-windows-x64-1/
            │   │   ├── CACHEDIR.TAG
            │   │   └── demo-content.txt
            │   ├── llvm-19-x86_64-windows-essentials-134/
            │   │   ├── CACHEDIR.TAG
            │   │   └── demo-content.txt
            │   ├── msys2-mingw-w64-x86_64-2/
            │   │   ├── CACHEDIR.TAG
            │   │   └── demo-content.txt
            │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
            │       ├── CACHEDIR.TAG
            │       └── demo-content.txt
            └── kotlin-native-prebuilt-2.3.0-windows-x86_64/
                ├── bin/
                │   └── run_konan.bat
                ├── CACHEDIR.TAG
                └── demo-content.txt
            """.trimIndent()

          KnBuildPlatform.OsFamily.Linux   ->
            """
            konan-data/
            ├── checksums/
            │   ├── dependencies_libffi-3.2.1-2-linux-x86-64.hash
            │   ├── dependencies_lldb-4-linux.hash
            │   ├── dependencies_llvm-19-x86_64-linux-essentials-103.hash
            │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
            │   ├── kotlin-native-prebuilt-2.3.0-linux-x86_64.hash
            │   ├── kotlin-native-prebuilt-2.3.0-linux-x86_64.tar.gz.hash
            │   ├── libffi-3.2.1-2-linux-x86-64.tar.gz.hash
            │   ├── lldb-4-linux.tar.gz.hash
            │   ├── llvm-19-x86_64-linux-essentials-103.tar.gz.hash
            │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.tar.gz.hash
            ├── dependencies/
            │   ├── libffi-3.2.1-2-linux-x86-64/
            │   │   ├── CACHEDIR.TAG
            │   │   └── demo-content.txt
            │   ├── lldb-4-linux/
            │   │   ├── CACHEDIR.TAG
            │   │   └── demo-content.txt
            │   ├── llvm-19-x86_64-linux-essentials-103/
            │   │   ├── CACHEDIR.TAG
            │   │   └── demo-content.txt
            │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
            │       ├── CACHEDIR.TAG
            │       └── demo-content.txt
            └── kotlin-native-prebuilt-2.3.0-linux-x86_64/
                ├── bin/
                │   └── run_konan
                ├── CACHEDIR.TAG
                └── demo-content.txt
            """.trimIndent()
        }.trimIndent()
      }
  }
}

private object ExpectedDirectory {
  object MacOS {
    const val AARCH_64 = """
      konan-data/
      ├── checksums/
      │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.tar.gz.hash
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.tar.gz.hash
      │   ├── dependencies_aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.hash
      │   ├── dependencies_arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.hash
      │   ├── dependencies_libffi-3.3-1-macos-arm64.hash
      │   ├── dependencies_lldb-4-macos.hash
      │   ├── dependencies_llvm-19-aarch64-macos-essentials-79.hash
      │   ├── dependencies_msys2-mingw-w64-x86_64-2.hash
      │   ├── dependencies_target-sysroot-1-android_ndk.hash
      │   ├── dependencies_target-toolchain-2-osx-android_ndk.hash
      │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
      │   ├── kotlin-native-prebuilt-2.3.0-macos-aarch64.hash
      │   ├── kotlin-native-prebuilt-2.3.0-macos-aarch64.tar.gz.hash
      │   ├── libffi-3.3-1-macos-arm64.tar.gz.hash
      │   ├── lldb-4-macos.tar.gz.hash
      │   ├── llvm-19-aarch64-macos-essentials-79.tar.gz.hash
      │   ├── msys2-mingw-w64-x86_64-2.tar.gz.hash
      │   ├── target-sysroot-1-android_ndk.tar.gz.hash
      │   ├── target-toolchain-2-osx-android_ndk.tar.gz.hash
      │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.tar.gz.hash
      ├── dependencies/
      │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── libffi-3.3-1-macos-arm64/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── lldb-4-macos/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── llvm-19-aarch64-macos-essentials-79/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── msys2-mingw-w64-x86_64-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── target-sysroot-1-android_ndk/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── target-toolchain-2-osx-android_ndk/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
      │       ├── CACHEDIR.TAG
      │       └── demo-content.txt
      └── kotlin-native-prebuilt-2.3.0-macos-aarch64/
          ├── bin/
          │   └── run_konan
          ├── CACHEDIR.TAG
          └── demo-content.txt
      """
    const val X86_64 = """
      konan-data/
      ├── checksums/
      │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.tar.gz.hash
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.tar.gz.hash
      │   ├── dependencies_aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.hash
      │   ├── dependencies_arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.hash
      │   ├── dependencies_libffi-3.2.1-3-darwin-macos.hash
      │   ├── dependencies_lldb-4-macos.hash
      │   ├── dependencies_llvm-19-x86_64-macos-essentials-75.hash
      │   ├── dependencies_msys2-mingw-w64-x86_64-2.hash
      │   ├── dependencies_target-sysroot-1-android_ndk.hash
      │   ├── dependencies_target-toolchain-2-osx-android_ndk.hash
      │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
      │   ├── kotlin-native-prebuilt-2.3.0-macos-x86_64.hash
      │   ├── kotlin-native-prebuilt-2.3.0-macos-x86_64.tar.gz.hash
      │   ├── libffi-3.2.1-3-darwin-macos.tar.gz.hash
      │   ├── lldb-4-macos.tar.gz.hash
      │   ├── llvm-19-x86_64-macos-essentials-75.tar.gz.hash
      │   ├── msys2-mingw-w64-x86_64-2.tar.gz.hash
      │   ├── target-sysroot-1-android_ndk.tar.gz.hash
      │   ├── target-toolchain-2-osx-android_ndk.tar.gz.hash
      │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.tar.gz.hash
      ├── dependencies/
      │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── libffi-3.2.1-3-darwin-macos/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── lldb-4-macos/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── llvm-19-x86_64-macos-essentials-75/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── msys2-mingw-w64-x86_64-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── target-sysroot-1-android_ndk/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── target-toolchain-2-osx-android_ndk/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
      │       ├── CACHEDIR.TAG
      │       └── demo-content.txt
      └── kotlin-native-prebuilt-2.3.0-macos-x86_64/
          ├── bin/
          │   └── run_konan
          ├── CACHEDIR.TAG
          └── demo-content.txt
    """
  }

  object Windows {
    const val X86_64 = """
      konan-data/
      ├── checksums/
      │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.zip.hash
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.zip.hash
      │   ├── dependencies_aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.hash
      │   ├── dependencies_arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.hash
      │   ├── dependencies_libffi-3.3-windows-x64-1.hash
      │   ├── dependencies_lldb-2-windows.hash
      │   ├── dependencies_llvm-19-x86_64-windows-essentials-134.hash
      │   ├── dependencies_msys2-mingw-w64-x86_64-2.hash
      │   ├── dependencies_target-sysroot-1-android_ndk.hash
      │   ├── dependencies_target-toolchain-2-windows-android_ndk.hash
      │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
      │   ├── kotlin-native-prebuilt-2.3.0-windows-x86_64.hash
      │   ├── kotlin-native-prebuilt-2.3.0-windows-x86_64.zip.hash
      │   ├── libffi-3.3-windows-x64-1.zip.hash
      │   ├── lldb-2-windows.zip.hash
      │   ├── llvm-19-x86_64-windows-essentials-134.zip.hash
      │   ├── msys2-mingw-w64-x86_64-2.zip.hash
      │   ├── target-sysroot-1-android_ndk.zip.hash
      │   ├── target-toolchain-2-windows-android_ndk.zip.hash
      │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.zip.hash
      ├── dependencies/
      │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── libffi-3.3-windows-x64-1/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── lldb-2-windows/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── llvm-19-x86_64-windows-essentials-134/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── msys2-mingw-w64-x86_64-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── target-sysroot-1-android_ndk/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── target-toolchain-2-windows-android_ndk/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
      │       ├── CACHEDIR.TAG
      │       └── demo-content.txt
      └── kotlin-native-prebuilt-2.3.0-windows-x86_64/
          ├── bin/
          │   └── run_konan.bat
          ├── CACHEDIR.TAG
          └── demo-content.txt
    """
  }

  object Linux {
    const val X86_64 = """
      konan-data/
      ├── checksums/
      │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.tar.gz.hash
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.tar.gz.hash
      │   ├── dependencies_aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.hash
      │   ├── dependencies_arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.hash
      │   ├── dependencies_libffi-3.2.1-2-linux-x86-64.hash
      │   ├── dependencies_lldb-4-linux.hash
      │   ├── dependencies_llvm-19-x86_64-linux-essentials-103.hash
      │   ├── dependencies_msys2-mingw-w64-x86_64-2.hash
      │   ├── dependencies_qemu-aarch64-static-5.1.0-linux-2.hash
      │   ├── dependencies_qemu-arm-static-5.1.0-linux-2.hash
      │   ├── dependencies_target-sysroot-1-android_ndk.hash
      │   ├── dependencies_target-toolchain-2-linux-android_ndk.hash
      │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
      │   ├── kotlin-native-prebuilt-2.3.0-linux-x86_64.hash
      │   ├── kotlin-native-prebuilt-2.3.0-linux-x86_64.tar.gz.hash
      │   ├── libffi-3.2.1-2-linux-x86-64.tar.gz.hash
      │   ├── lldb-4-linux.tar.gz.hash
      │   ├── llvm-19-x86_64-linux-essentials-103.tar.gz.hash
      │   ├── msys2-mingw-w64-x86_64-2.tar.gz.hash
      │   ├── qemu-aarch64-static-5.1.0-linux-2.tar.gz.hash
      │   ├── qemu-arm-static-5.1.0-linux-2.tar.gz.hash
      │   ├── target-sysroot-1-android_ndk.tar.gz.hash
      │   ├── target-toolchain-2-linux-android_ndk.tar.gz.hash
      │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.tar.gz.hash
      ├── dependencies/
      │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── libffi-3.2.1-2-linux-x86-64/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── lldb-4-linux/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── llvm-19-x86_64-linux-essentials-103/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── msys2-mingw-w64-x86_64-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── qemu-aarch64-static-5.1.0-linux-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── qemu-arm-static-5.1.0-linux-2/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── target-sysroot-1-android_ndk/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   ├── target-toolchain-2-linux-android_ndk/
      │   │   ├── CACHEDIR.TAG
      │   │   └── demo-content.txt
      │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
      │       ├── CACHEDIR.TAG
      │       └── demo-content.txt
      └── kotlin-native-prebuilt-2.3.0-linux-x86_64/
          ├── bin/
          │   └── run_konan
          ├── CACHEDIR.TAG
          └── demo-content.txt
    """
  }
}


/**
 * Register a Gradle task `runKonanTask` that executes the dummy `run_konan` shell script.
 */
private fun GradleTestContext.registerRunKonanTask() {
  buildGradleKts += """
        |import java.io.ByteArrayOutputStream
        |import org.gradle.kotlin.dsl.support.serviceOf
        |
        |val runKonanTask by tasks.registering {
        |  val exec = serviceOf<ExecOperations>()
        |
        |  val runKonan = knToolchain.runKonan()
        |
        |  doLast {
        |    val runKonan = runKonan.get()
        |    val stdout = ByteArrayOutputStream()
        |    println("run_konan path: {" + runKonan.invariantSeparatorsPathString + "}")
        |    exec.exec {
        |      executable(runKonan)
        |      args(path)
        |      standardOutput = stdout
        |    }
        |    println("run_konan output: {" + stdout.toString().trim() + "}")
        |  }
        |}
        |""".trimMargin()
}
