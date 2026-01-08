package dev.adamko.kntoolchain.test_utils

import java.nio.file.Path
import kotlin.io.path.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import org.gradle.testkit.runner.GradleRunner

class GradleTestContext(
  tmpDir: Path,
  projectName: String = "demo-project",
) : AutoCloseable {

  val projectDir: Path = tmpDir.resolve(projectName)

  val settingsGradleKtsFile: Path = projectDir.resolve("settings.gradle.kts")
  val buildGradleKtsFile: Path = projectDir.resolve("build.gradle.kts")
  val gradlePropertiesFile: Path = projectDir.resolve("gradle.properties")

  var settingsGradleKts: String by settingsGradleKtsFile.text()
  var buildGradleKts: String by buildGradleKtsFile.text()
  var gradleProperties: String by gradlePropertiesFile.text()

  init {
    projectDir.createDirectories()

    settingsGradleKts = """
      |import org.gradle.api.initialization.resolve.RepositoriesMode.PREFER_SETTINGS
      |
      |rootProject.name = "$projectName"
      |
      |pluginManagement {
      |  repositories {
      |    exclusiveContent { 
      |      forRepository {
      |        maven(file("${devMavenRepo.invariantSeparatorsPathString}"))
      |      }
      |      filter { includeGroupAndSubgroups("dev.adamko.kotlin-native-toolchain") }
      |    }
      |    mavenCentral()
      |    gradlePluginPortal()
      |  }
      |}
      |
      |@Suppress("UnstableApiUsage")
      |dependencyResolutionManagement {
      |  repositoriesMode.set(PREFER_SETTINGS)
      |
      |  repositories {
      |    exclusiveContent { 
      |      forRepository {
      |        maven(file("${devMavenRepo.invariantSeparatorsPathString}"))
      |      }
      |      filter { includeGroupAndSubgroups("dev.adamko.kotlin-native-toolchain") }
      |    }
      |    mavenCentral()
      |  }
      |}
      |""".trimMargin()

    buildGradleKts =
      """
      |""".trimMargin()

    gradleProperties = """
      |org.gradle.jvmargs=-Dfile.encoding=UTF-8 -Xmx4g -XX:MaxMetaspaceSize=2g
      |org.gradle.caching=true
      |org.gradle.configuration-cache=true
      |org.gradle.logging.stacktrace=all
      |org.gradle.parallel=true
      |org.gradle.welcome=never
      |org.gradle.priority=low
      |org.gradle.daemon.idletimeout=60000
      |""".trimMargin()
  }


  val runner: GradleRunner = GradleRunner.create()
    .withProjectDir(projectDir.toFile())

  override fun close() {}

  companion object {
    private val devMavenRepo: Path by systemProperty { Path(it) }
  }
}


private fun Path.text(): ReadWriteProperty<Any?, String> =
  object : ReadWriteProperty<Any?, String> {
    val file: Path = this@text
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
      return file.readText()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
      file.writeText(value)
    }
  }
