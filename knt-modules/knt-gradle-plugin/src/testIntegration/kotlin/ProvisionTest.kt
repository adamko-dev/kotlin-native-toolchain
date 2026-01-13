package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.model.Architecture
import dev.adamko.kntoolchain.model.OsFamily
import dev.adamko.kntoolchain.test_utils.GradleTestContext
import dev.adamko.kntoolchain.test_utils.kntGradlePluginProjectVersion
import dev.adamko.kntoolchain.test_utils.toTreeString
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
import org.junit.jupiter.api.Assumptions.assumeFalse
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
    ): Stream<out Arguments> = Stream.of(
      Arguments.of(OsFamily.MacOs, Architecture.AArch64),
      Arguments.of(OsFamily.MacOs, Architecture.X86_64),
      Arguments.of(OsFamily.Windows, Architecture.X86_64),
      Arguments.of(OsFamily.Linux, Architecture.X86_64),
    )
  }

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  fun `verify konan data dir provisioned successfully`(
    knpOs: OsFamily,
    knpArch: Architecture,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    setupProject(
      knpOs = knpOs.name,
      knpArch = knpArch.name,
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

    runner
      .withArguments(
        "knToolchainUser",
      )
      .forwardOutput()
      .build()
      .apply {

        output shouldNotContain "partially complete installations detected"
        output shouldContain "Finished installing/checking Konan and 10 dependencies in"
        output shouldContain "knToolchain dir: {${konanDataDir.invariantSeparatorsPathString}/kotlin-native-prebuilt-2.3.0-${knpOs.id}-${knpArch.id}}"

        konanDataDir.toTreeString() shouldBe when (knpOs) {
          OsFamily.MacOs   -> when (knpArch) {
            Architecture.AArch64 -> ExpectedDirectory.MacOS.AARCH_64
            Architecture.X86_64  -> ExpectedDirectory.MacOS.X86_64
          }

          OsFamily.Windows -> ExpectedDirectory.Windows.X86_64
          OsFamily.Linux   -> ExpectedDirectory.Linux.X86_64
        }.trimIndent()
      }
  }

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  @DisabledOnOs(WINDOWS) // can't execute `run_konan.sh` on Windows
  fun `verify run_konan source successfully`(
    knpOs: OsFamily,
    knpArch: Architecture,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    assumeFalse(knpOs == OsFamily.Windows) // can't execute `run_konan.bat` on Linux/MacOS

    setupProject(
      knpOs = knpOs.name,
      knpArch = knpArch.name,
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
          konanDataDir.resolve("kotlin-native-prebuilt-2.3.0-${knpOs.id}-${knpArch.id}/bin/run_konan")
        output shouldContain "run_konan path: {${expectedRunKonanPath.invariantSeparatorsPathString}}"
        output shouldContain "run_konan output: {run_konan :runKonanTask}"
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
      │   ├── dependencies_libffi-3.3-1-macos-arm64.hash
      │   ├── dependencies_lldb-4-macos.hash
      │   ├── dependencies_llvm-19-aarch64-macos-dev-79.hash
      │   ├── dependencies_msys2-mingw-w64-x86_64-2.hash
      │   ├── dependencies_target-sysroot-1-android_ndk.hash
      │   ├── dependencies_target-toolchain-2-osx-android_ndk.hash
      │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
      │   ├── kotlin-native-prebuilt-2.3.0-macos-x86_64.hash
      │   ├── kotlin-native-prebuilt-2.3.0-macos-x86_64.tar.gz.hash
      │   ├── libffi-3.3-1-macos-arm64.tar.gz.hash
      │   ├── lldb-4-macos.tar.gz.hash
      │   ├── llvm-19-aarch64-macos-dev-79.tar.gz.hash
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
      │   ├── llvm-19-aarch64-macos-dev-79/
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
      │   ├── aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.tar.gz.hash
      │   ├── arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.tar.gz.hash
      │   ├── dependencies_aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2.hash
      │   ├── dependencies_arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1.hash
      │   ├── dependencies_libffi-3.3-1-macos-arm64.hash
      │   ├── dependencies_lldb-4-macos.hash
      │   ├── dependencies_llvm-19-aarch64-macos-dev-79.hash
      │   ├── dependencies_msys2-mingw-w64-x86_64-2.hash
      │   ├── dependencies_target-sysroot-1-android_ndk.hash
      │   ├── dependencies_target-toolchain-2-osx-android_ndk.hash
      │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
      │   ├── kotlin-native-prebuilt-2.3.0-windows-x86_64.hash
      │   ├── kotlin-native-prebuilt-2.3.0-windows-x86_64.zip.hash
      │   ├── libffi-3.3-1-macos-arm64.tar.gz.hash
      │   ├── lldb-4-macos.tar.gz.hash
      │   ├── llvm-19-aarch64-macos-dev-79.tar.gz.hash
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
      │   ├── llvm-19-aarch64-macos-dev-79/
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
      │   ├── dependencies_libffi-3.3-1-macos-arm64.hash
      │   ├── dependencies_lldb-4-macos.hash
      │   ├── dependencies_llvm-19-aarch64-macos-dev-79.hash
      │   ├── dependencies_msys2-mingw-w64-x86_64-2.hash
      │   ├── dependencies_target-sysroot-1-android_ndk.hash
      │   ├── dependencies_target-toolchain-2-osx-android_ndk.hash
      │   ├── dependencies_x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2.hash
      │   ├── kotlin-native-prebuilt-2.3.0-linux-x86_64.hash
      │   ├── kotlin-native-prebuilt-2.3.0-linux-x86_64.tar.gz.hash
      │   ├── libffi-3.3-1-macos-arm64.tar.gz.hash
      │   ├── lldb-4-macos.tar.gz.hash
      │   ├── llvm-19-aarch64-macos-dev-79.tar.gz.hash
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
      │   ├── llvm-19-aarch64-macos-dev-79/
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
  knpVersion: String = "2.3.0",
  knpOs: String = "MacOs",
  knpArch: String = "AArch64",
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
        |import dev.adamko.kntoolchain.model.OsFamily.*
        |import dev.adamko.kntoolchain.model.Architecture.*
        |
        |plugins {
        |  id("dev.adamko.kotlin-native-toolchain")
        |}
        |
        |knToolchain {
        |  baseInstallDir = file("${konanDataDir.invariantSeparatorsPathString}")
        |  kotlinNativePrebuiltDistribution {
        |    version = "$knpVersion"
        |    osFamily = $knpOs
        |    architecture = $knpArch
        |  }
        |}
        |""".trimMargin()
}

private fun setupDummyRepo(dummyRepo: Path) {
  dummyRepo.createDirectories()

  dummyRepo.resolve("kotlin/native").apply {
    createDirectories()

    createModuleTarGz("target-sysroot-1-android_ndk")
    createModuleTarGz("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2")
    createModuleTarGz("arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1")
    createModuleTarGz("libffi-3.3-1-macos-arm64")
    createModuleTarGz("lldb-4-macos")
    createModuleTarGz("msys2-mingw-w64-x86_64-2")
    createModuleTarGz("target-sysroot-1-android_ndk")
    createModuleTarGz("target-toolchain-2-osx-android_ndk")
    createModuleTarGz("x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2")
    resolve("resources/llvm/19-aarch64-macos/").apply {
      createDirectories()
      createModuleTarGz("llvm-19-aarch64-macos-essentials-79")
    }
    resolve("resources/llvm/19-aarch64-macos/llvm-19-aarch64-macos-dev/").apply {
      createDirectories()
      createModuleTarGz("llvm-19-aarch64-macos-dev-79")
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
