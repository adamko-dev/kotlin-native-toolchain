package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.test_utils.GradleTestContext
import dev.adamko.kntoolchain.test_utils.kntGradlePluginProjectVersion
import dev.adamko.kntoolchain.test_utils.toTreeString
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path
import kotlin.io.path.*
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ProvisionTest {

  @Test
  fun `verify konan data dir provisioned successfully`(
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    setupProject()

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
        output shouldContain "knToolchain dir: {${konanDataDir.invariantSeparatorsPathString}/kotlin-native-prebuilt-2.3.0-macos-aarch64}"

        konanDataDir.toTreeString() shouldBe """
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
        """.trimIndent()
      }
  }

  @Test
  fun `verify run_konan source successfully`(
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    setupProject()

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
        val expectedRunKonanPath = konanDataDir.resolve("kotlin-native-prebuilt-2.3.0-macos-aarch64/bin/run_konan")
        output shouldContain "run_konan path: {${expectedRunKonanPath.invariantSeparatorsPathString}}"
        output shouldContain "run_konan output: {run_konan :runKonanTask}"
      }
  }
}

private val GradleTestContext.konanDataDir: Path
  get() = projectDir.resolve("konan-data")

private fun GradleTestContext.setupProject() {
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
        |
        |plugins {
        |  id("dev.adamko.kotlin-native-toolchain")
        |}
        |
        |knToolchain {
        |  baseInstallDir = file("${konanDataDir.invariantSeparatorsPathString}")
        |  kotlinNativePrebuiltDistribution {
        |    version = "2.3.0"
        |    osFamily = dev.adamko.kntoolchain.model.OsFamily.MacOs
        |    architecture = dev.adamko.kntoolchain.model.Architecture.AArch64
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

    createModuleTarGz("kotlin-native-prebuilt-2.3.0-macos-aarch64") { name, sink ->
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
