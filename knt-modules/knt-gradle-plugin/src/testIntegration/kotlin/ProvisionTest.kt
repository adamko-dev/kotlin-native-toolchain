package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.test_utils.GradleTestContext
import dev.adamko.kntoolchain.test_utils.kntGradlePluginProjectVersion
import dev.adamko.kntoolchain.test_utils.toTreeString
import dev.adamko.kntoolchain.tools.data.KnBuildPlatform
import dev.adamko.kntoolchain.tools.data.KnpVersion
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.*
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.support.ParameterDeclarations

class ProvisionTest {

  class KnpOsArchArgs : ArgumentsProvider {
    override fun provideArguments(
      parameters: ParameterDeclarations,
      context: ExtensionContext,
    ): Stream<out Arguments> =
      KnBuildPlatform.allPlatforms.map {
        Arguments.of(it)
      }.stream()
  }

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  fun `verify konan data dir provisioned successfully`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    setupProject(
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

    setupProject(
      buildPlatform = buildPlatform,
    )

    buildGradleKts = """
        |import java.io.ByteArrayOutputStream
        |import org.gradle.kotlin.dsl.support.serviceOf
        |
        |$buildGradleKts
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
  @DisabledOnOs(WINDOWS) // can't execute `run_konan.sh` on Windows
  fun `verify disable kn target`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

//    assumeFalse(knpOs == OsFamily.Windows) // can't execute `run_konan.bat` on Linux/MacOS

    setupProject(buildPlatform = buildPlatform)

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
        |  baseInstallDir = file("${konanDataDir.invariantSeparatorsPathString}")
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
            │   │   ├── libffi-3.3-windows-x64-1/
            │   │   │   └── demo-content.txt
            │   │   └── CACHEDIR.TAG
            │   ├── llvm-19-x86_64-windows-essentials-134/
            │   │   ├── llvm-19-x86_64-windows-essentials-134/
            │   │   │   └── demo-content.txt
            │   │   └── CACHEDIR.TAG
            │   ├── msys2-mingw-w64-x86_64-2/
            │   │   ├── msys2-mingw-w64-x86_64-2/
            │   │   │   └── demo-content.txt
            │   │   └── CACHEDIR.TAG
            │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
            │       ├── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
            │       │   └── demo-content.txt
            │       └── CACHEDIR.TAG
            └── kotlin-native-prebuilt-2.3.0-windows-x86_64/
                ├── kotlin-native-prebuilt-2.3.0-windows-x86_64/
                │   ├── bin/
                │   │   └── run_konan.bat
                │   └── demo-content.txt
                └── CACHEDIR.TAG
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
      │   │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/
      │   │   │   └── demo-content.txt
      │   │   └── CACHEDIR.TAG
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1/
      │   │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1/
      │   │   │   └── demo-content.txt
      │   │   └── CACHEDIR.TAG
      │   ├── libffi-3.3-windows-x64-1/
      │   │   ├── libffi-3.3-windows-x64-1/
      │   │   │   └── demo-content.txt
      │   │   └── CACHEDIR.TAG
      │   ├── lldb-2-windows/
      │   │   ├── lldb-2-windows/
      │   │   │   └── demo-content.txt
      │   │   └── CACHEDIR.TAG
      │   ├── llvm-19-x86_64-windows-essentials-134/
      │   │   ├── llvm-19-x86_64-windows-essentials-134/
      │   │   │   └── demo-content.txt
      │   │   └── CACHEDIR.TAG
      │   ├── msys2-mingw-w64-x86_64-2/
      │   │   ├── msys2-mingw-w64-x86_64-2/
      │   │   │   └── demo-content.txt
      │   │   └── CACHEDIR.TAG
      │   ├── target-sysroot-1-android_ndk/
      │   │   ├── target-sysroot-1-android_ndk/
      │   │   │   └── demo-content.txt
      │   │   └── CACHEDIR.TAG
      │   ├── target-toolchain-2-windows-android_ndk/
      │   │   ├── target-toolchain-2-windows-android_ndk/
      │   │   │   └── demo-content.txt
      │   │   └── CACHEDIR.TAG
      │   └── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
      │       ├── x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/
      │       │   └── demo-content.txt
      │       └── CACHEDIR.TAG
      └── kotlin-native-prebuilt-2.3.0-windows-x86_64/
          ├── kotlin-native-prebuilt-2.3.0-windows-x86_64/
          │   ├── bin/
          │   │   └── run_konan.bat
          │   └── demo-content.txt
          └── CACHEDIR.TAG
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

private val GradleTestContext.konanDataDir: Path
  get() = projectDir.resolve("konan-data")

private fun GradleTestContext.setupProject(
  knpVersion: KnpVersion = KnpVersion.V2_3_0,
  buildPlatform: KnBuildPlatform = KnBuildPlatform.MacOs.X86_64,
) {
  val dummyRepo = projectDir.resolve("dummy-repo")
  setupDummyRepo(dummyRepo)

  settingsGradleKts += """
        |plugins {
        |  id("dev.adamko.kotlin-native-toolchain") version "$kntGradlePluginProjectVersion"
        |}
        |
        |dependencyResolutionManagement.repositories
        |  .withType<IvyArtifactRepository>()
        |  .matching { it.name == "Kotlin Native Prebuilt Dependencies" }
        |  .configureEach { 
        |    setUrl("${dummyRepo.toUri()}")
        |  }
        |""".trimMargin()

  settingsGradleKts = settingsGradleKts
    .replace(
      "mavenCentral()",
      """
      mavenCentral {
        content {
          excludeModule("org.jetbrains.kotlin", "kotlin-native-prebuilt")
        }
      }
      maven(file("${dummyRepo.invariantSeparatorsPathString}")) {
        content { 
          includeModule("org.jetbrains.kotlin", "kotlin-native-prebuilt") 
        }
      }
      """.trimIndent()
    )

  buildGradleKts += """
        |import kotlin.io.path.*
        |import dev.adamko.kntoolchain.tools.data.*
        |
        |plugins {
        |  id("dev.adamko.kotlin-native-toolchain")
        |}
        |
        |knToolchain {
        |  baseInstallDir = file("${konanDataDir.invariantSeparatorsPathString}")
        |  kotlinNativePrebuiltDistribution {
        |    version = KnpVersion.${knpVersion.name}
        |    buildPlatform = KnBuildPlatform.${buildPlatform.family.name}.${buildPlatform.arch.name}
        |    compileTargets = KnCompileTarget.allTargets
        |  }
        |}
        |""".trimMargin()
}

private fun setupDummyRepo(dummyRepo: Path) {
  dummyRepo.createDirectories()

  dummyRepo.resolve("kotlin/native").apply {
    createDirectories()

    createModuleTarGz("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2")
    createModuleTarGz("arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1")
    createModuleZip("arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1")
    createModuleTarGz("libffi-3.2.1-2-linux-x86-64")
    createModuleTarGz("libffi-3.2.1-3-darwin-macos")
    createModuleTarGz("libffi-3.3-1-macos-arm64")
    createModuleTarGz("lldb-4-linux")
    createModuleTarGz("lldb-4-macos")
    createModuleTarGz("msys2-mingw-w64-x86_64-2")
    createModuleTarGz("qemu-aarch64-static-5.1.0-linux-2")
    createModuleTarGz("qemu-arm-static-5.1.0-linux-2")
    createModuleTarGz("target-sysroot-1-android_ndk")
    createModuleTarGz("target-toolchain-2-linux-android_ndk")

    createModuleTarGz("target-toolchain-2-osx-android_ndk")
    createModuleTarGz("x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2")

    createModuleZip("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2")
    createModuleZip("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2")
    createModuleZip("arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12")
    createModuleZip("arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4")
    createModuleZip("libffi-3.3-windows-x64-1")
    createModuleZip("lldb-2-windows")
    createModuleZip("msys2-mingw-w64-x86_64-2")
    createModuleZip("target-sysroot-1-android_ndk")
    createModuleZip("target-toolchain-2-windows-android_ndk")
    createModuleZip("x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4")
    createModuleZip("x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2")
    createModuleZip("target-sysroot-1-android_ndk")
    createModuleZip("target-toolchain-2-windows-android_ndk")
    createModuleZip("libffi-3.3-windows-x64-1")

    resolve("resources/llvm/19-aarch64-macos/").apply {
      createDirectories()
      createModuleTarGz("llvm-19-aarch64-macos-essentials-79")
    }
    resolve("resources/llvm/19-aarch64-macos/llvm-19-aarch64-macos-dev/").apply {
      createDirectories()
      createModuleTarGz("llvm-19-aarch64-macos-dev-79")
    }
    resolve("resources/llvm/19-x86_64-macos/").apply {
      createDirectories()
      createModuleTarGz("llvm-19-x86_64-macos-essentials-75")
    }
    resolve("resources/llvm/19-x86_64-macos/llvm-19-x86_64-macos-dev/").apply {
      createDirectories()
      createModuleTarGz("llvm-19-x86_64-macos-dev-75")
    }
    resolve("resources/llvm/19-x86_64-linux/llvm-19-x86_64-linux-essentials/").apply {
      createDirectories()
      createModuleTarGz("llvm-19-x86_64-linux-essentials-103")
    }
    resolve("resources/llvm/19-x86_64-windows/").apply {
      createDirectories()
      createModuleZip("llvm-19-x86_64-windows-dev-134")
      createModuleZip("llvm-19-x86_64-windows-essentials-134")
    }
  }

  dummyRepo.resolve("org/jetbrains/kotlin/kotlin-native-prebuilt/2.3.0").apply {
    createDirectories()

    resolve("kotlin-native-prebuilt-2.3.0.pom").writeText(
      /* language=XML */ """
      |<?xml version="1.0" encoding="UTF-8"?>
      |<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
      |    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      |  <modelVersion>4.0.0</modelVersion>
      |  <groupId>org.jetbrains.kotlin</groupId>
      |  <artifactId>kotlin-native-prebuilt</artifactId>
      |  <version>2.3.0</version>
      |  <packaging>pom</packaging>
      |  <name>Kotlin Native Prebuilt</name>
      |</project>
      |""".trimMargin()
    )

    listOf(
      "macos" to "aarch64",
      "macos" to "x86_64",
      "linux" to "x86_64",
    ).forEach { (os, arch) ->
      createModuleTarGz("kotlin-native-prebuilt-2.3.0-$os-$arch") { name, sink ->
        sink.putArchiveEntry(TarArchiveEntry("$name/bin/"))
        sink.closeArchiveEntry()

        val content = """
        |#!/usr/bin/env bash
        |
        |echo "run_konan $1"
        |""".trimMargin()
        val entry = TarArchiveEntry("$name/bin/run_konan").apply {
          size = content.length.toLong()
          mode = "755".toInt(8)
        }
        sink.putArchiveEntry(entry)
        sink.write(content.toByteArray())
        sink.closeArchiveEntry()
      }
    }

    listOf(
      "windows" to "x86_64",
    ).forEach { (os, arch) ->
      createModuleZip("kotlin-native-prebuilt-2.3.0-$os-$arch") { name, sink ->
        sink.putArchiveEntry(ZipArchiveEntry("$name/bin/"))
        sink.closeArchiveEntry()

        val content = """
        |@echo off
        |
        |echo run_konan %1
        |""".trimMargin()
        val entry = ZipArchiveEntry("$name/bin/run_konan.bat").apply {
          size = content.length.toLong()
          //mode = "755".toInt(8)
        }
        sink.putArchiveEntry(entry)
        sink.write(content.toByteArray())
        sink.closeArchiveEntry()
      }
    }
  }
}

private fun Path.createModuleTarGz(
  name: String,
  content: (name: String, sink: TarArchiveOutputStream) -> Unit = { _, _ -> },
) {
  require(this.isDirectory())

  val out: Path = resolve("$name.tar.gz")
  val entryName = "$name/demo-content.txt"
  val content = "dummy module $name\n".toByteArray(Charsets.UTF_8)

  out.outputStream()
    .let(::GzipCompressorOutputStream)
    .let(::TarArchiveOutputStream)
    .use { sink ->
      val entry = TarArchiveEntry(entryName).apply {
        size = content.size.toLong()
      }
      sink.putArchiveEntry(entry)
      sink.write(content)
      sink.closeArchiveEntry()

      content(name, sink)

      sink.finish()
    }
}

private fun Path.createModuleZip(
  name: String,
  content: (name: String, sink: ZipArchiveOutputStream) -> Unit = { _, _ -> },
) {
  require(this.isDirectory())

  val out: Path = resolve("$name.zip")
  val entryName = "$name/demo-content.txt"
  val content = "dummy module $name\n".toByteArray(Charsets.UTF_8)

  out.outputStream()
    .let(::ZipArchiveOutputStream)
    .use { sink ->

      // ensure root dir exists in zip
      sink.putArchiveEntry(ZipArchiveEntry("$name/"))
      sink.closeArchiveEntry()

      val entry = ZipArchiveEntry(entryName).apply {
        size = content.size.toLong()
      }
      sink.putArchiveEntry(entry)
      sink.write(content)
      sink.closeArchiveEntry()

      content(name, sink)

      sink.finish()
    }
}
