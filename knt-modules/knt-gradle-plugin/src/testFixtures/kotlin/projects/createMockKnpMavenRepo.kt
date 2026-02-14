package dev.adamko.kntoolchain.test_utils.projects

import dev.adamko.kntoolchain.test_utils.createModuleTarGz
import dev.adamko.kntoolchain.test_utils.createModuleZip
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry

/**
 * Create a file-based Maven repository containing mock KNP distributions and dependencies.
 */
fun createMockKnpMavenRepo(dummyRepo: Path) {
  dummyRepo.createDirectories()

  dummyRepo.resolve("kotlin/native").apply {
    createDirectories()

    createModuleTarGz("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2")
    createModuleTarGz("arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1")
    createModuleTarGz("libffi-3.2.1-2-linux-x86-64")
    createModuleTarGz("libffi-3.2.1-3-darwin-macos")
    createModuleTarGz("libffi-3.3-1-macos-arm64")
    createModuleTarGz("lldb-4-linux")
    createModuleTarGz("lldb-4-macos")
    createModuleTarGz("msys2-mingw-w64-x86_64-2")
    createModuleTarGz("qemu-aarch64-static-5.1.0-linux-2")
    createModuleTarGz("qemu-arm-static-5.1.0-linux-2")
    createModuleTarGz("x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2")

    createModuleZip("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2")
    createModuleZip("aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2")
    createModuleZip("arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12")
    createModuleZip("arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4")
    createModuleZip("arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.12.1-kernel-4.9-1")
    createModuleZip("libffi-3.3-windows-x64-1")
    createModuleZip("lldb-2-windows")
    createModuleZip("msys2-mingw-w64-x86_64-2")
    createModuleZip("x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4")
    createModuleZip("x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2")

    createModuleTarGz("target-sysroot-1-android_ndk")
    createModuleTarGz("target-toolchain-2-linux-android_ndk")
    createModuleTarGz("target-toolchain-2-osx-android_ndk")

    createModuleZip("target-sysroot-1-android_ndk")
    createModuleZip("target-toolchain-2-windows-android_ndk")

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
