package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.test_utils.GradleTestContext
import dev.adamko.kntoolchain.test_utils.GradleTestContext.Companion.devMavenRepo
import dev.adamko.kntoolchain.test_utils.systemProperty
import io.kotest.matchers.collections.shouldContainAll
import java.nio.file.Path
import kotlin.io.path.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ExamplesTest {

  @Test
  fun `c-compile-example`(
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir, projectName = "c-compile-example")) {
    copyProject()

    val runDebugExecutableTask = runDebugExecutableTaskName()

    runner
      .withArguments(runDebugExecutableTask)
      .forwardOutput()
      .build()
      .apply {
        output.lines().shouldContainAll(
          "Hello, world!",
          "isEven(0) true",
          "isEven(1) false",
          "isEven(2) true",
          "isEven(3) false",
          "isEven(4) true",
        )
      }
  }

  private fun GradleTestContext.copyProject() {
    examplesDir.resolve("c-compile-example").also { srcDir ->
      srcDir.walk()
        .filter { it.isRegularFile() }
        .filter { it.name != "gradle.properties" }
        .forEach { f ->
          val dest = projectDir.resolve(f.relativeTo(srcDir))
          dest.parent.createDirectories()
          f.copyTo(dest, overwrite = true)
        }
    }

    updateGradleProperties()
    updateRepositories()
    updateKntBaseInstallDir()
    updateKntPluginVersion()
  }

  private fun GradleTestContext.updateGradleProperties() {
    projectDir.resolve("gradle.properties").apply {
      writeText(
        buildString {
          appendLine(readText())
          appendLine("kotlin.mpp.enableCInteropCommonization=true")
        }
      )
    }
  }

  private fun GradleTestContext.updateRepositories() {
    projectDir.walk()
      .filter { it.name == "settings.gradle.kts" }
      .forEach { file ->
        file.writeText(
          file.useLines { lines ->
            lines.joinToString("\n") { line ->
              if (line.trim() == "repositories {") {
                val currentIndent = line.length - line.trimStart().length
                buildString {
                  appendLine(line)
                  appendLine(
                    """
                    |val devRepo =
                    |  maven(file("${devMavenRepo.invariantSeparatorsPathString}")) {
                    |    name = "DevRepo"
                    |  }
                    |exclusiveContent {
                    |  forRepositories(devRepo)
                    |    filter {
                    |      includeGroupAndSubgroups("dev.adamko")
                    |  }
                    |}
                    """.trimMargin().prependIndent("  ".repeat(currentIndent + 1))
                  )
                }
              } else {
                line
              }
            }
          }
        )
      }
  }

  private fun GradleTestContext.updateKntBaseInstallDir() {
    projectDir.walk()
      .filter { it.name == "build.gradle.kts" }
      .filter { it.readText().contains("""id("dev.adamko.kotlin-native-toolchain")""") }
      .forEach { file ->
        file.writeText(
          buildString {
            appendLine(file.readText())
            appendLine(
              """
              |project.extensions.configure<dev.adamko.kntoolchain.KnToolchainProjectExtension> {
              |  baseInstallDir = file("${konanDataDir.invariantSeparatorsPathString}")
              |}
              """.trimMargin()
            )
          }
        )
      }
  }

  private fun GradleTestContext.updateKntPluginVersion() {
    val kntPluginId = """id("dev.adamko.kotlin-native-toolchain")"""
    projectDir.walk()
      .filter { it.name == "build.gradle.kts" }
      .filter { it.readText().contains("""$kntPluginId version""") }
      .forEach { file ->
        file.writeText(
          file.readLines()
            .joinToString("\n") { line ->
              if (line.trim().startsWith("""$kntPluginId version""")) {
                """$kntPluginId version "$kntGradlePluginProjectVersion""""
              } else {
                line
              }
            }
        )
      }
  }

  private fun runDebugExecutableTaskName(): String {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")

    return when {
      osName.startsWith("Mac")     ->
        when (osArch) {
          "aarch64" -> "runDebugExecutableMacosArm64"
          "x64"     -> "runDebugExecutableMacosX64"
          else      -> error("Unknown arch: $osArch")
        }

      osName.startsWith("Linux")   -> "runDebugExecutableLinuxX64"

      osName.startsWith("Windows") -> "runDebugExecutableMingwX64"

      else                         -> error("Unknown OS: $osName")
    }
  }


  companion object {
    private val examplesDir: Path by systemProperty(::Path)
    private val kntGradlePluginProjectVersion: String by systemProperty()
  }
}
