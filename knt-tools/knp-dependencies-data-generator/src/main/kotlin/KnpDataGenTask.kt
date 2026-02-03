package dev.adamko.knp

import dev.adamko.knp.utils.buildStringBlock
import dev.adamko.kntoolchain.tools.datamodel.KonanDependenciesReport
import dev.adamko.kntoolchain.tools.datamodel.KotlinVersionTargetDependencies
import dev.adamko.kntoolchain.tools.datamodel.Platform
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import utils.konanDependenciesReport

abstract class KnpDataGenTask
internal constructor() : DefaultTask() {

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @TaskAction
  protected fun action() {
    prepareOutputDir()
    generateKotlinData(konanDependenciesReport)
  }

  private fun prepareOutputDir() {
    outputDir.get().asFile.deleteRecursively()
    outputDir.get().asFile.mkdirs()
  }

  private fun generateKotlinData(report: KonanDependenciesReport) {
    val outputDir = outputDir.get().asFile.toPath()

    val ctx = Context(outputDir, report)

    context(ctx) {
      createKnpVersion()
      createKnBuildTarget()
      createKonanCompileTarget()
      createKnpDepData()
      createKnpDependency()
    }
  }
}

private class Context(
  val outputDir: Path,
  val report: KonanDependenciesReport,
) {

  val allVersions: SortedMap<String, String> =
    report.data.map { it.version }.distinct()
      .associateWith { version ->
        "V" + version.map { if (it.isLetterOrDigit()) it else '_' }.joinToString("")
      }
      .toSortedMap()

  val allKonanTargets =
    report.data.map { it.targetPlatform }
      .associateWith { target ->
        target.name
          .split(Regex("[^A-Za-z0-9]"))
          .joinToString("") { it.uppercaseFirstChar() }
          .replace("Watchos", "WatchOs")
          .replace("Tvos", "TvOs")
          .replace("Ios", "IOs")
          .replace("Macos", "MacOs")
      }

  val allBuildPlatforms: SortedSet<Platform> =
    report.data.map { it.buildPlatform }.toSortedSet()

  val allOsFamilies: SortedMap<String, String> =
    allBuildPlatforms.map { it.osFamily }.distinct()
      .associateWith { family ->
        when (family) {
          "linux" -> "Linux"
          "macos" -> "MacOs"
          "windows" -> "Windows"
          else -> error("Unknown OS family: $family")
        }
      }
      .toSortedMap()

  val allOsArches: SortedMap<String, String> =
    allBuildPlatforms.map { it.osArch }.distinct()
      .associateWith { arch ->
        when (arch) {
          "aarch64" -> "AArch64"
          "x86_64"  -> "X86_64"
          else      -> error("Unknown OS arch: $arch")
        }
      }
      .toSortedMap()
}

context(ctx: Context)
private fun createKnpVersion() {
  ctx.outputDir.resolve("KnpVersion.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()

      line("/**")
      line(" * kotlin-native-prebuilt version.")
      line(" */")
      block("enum class KnpVersion(", ") {") {
        line("val value: String,")
      }
      block("", "}") {
        ctx.allVersions.forEach { (version, name) ->
          line()
          line("/** Version `$version`. */")
          line("$name(\"$version\"),")
        }
      }
    }
  )

}

context(ctx: Context)
private fun createKnBuildTarget() {

  val groupedPlatforms = ctx.allBuildPlatforms.groupBy { it.osFamily }

  ctx.outputDir.resolve("KnBuildPlatform.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()

      line("/**")
      line(" * A platform that can build Kotlin/Native code.")
      line(" */")
      block("class KnBuildPlatform\nprivate constructor(", "): java.io.Serializable {") {
        line("val family: OsFamily,")
        line("val arch: OsArch,")
      }
      line()
      block("", "}") {

        block("enum class OsFamily(", ") {") {
          line("val value: String,")
        }
        block("", "}") {
          ctx.allOsFamilies.forEach { (family, prettyName) ->
            line("$prettyName(\"$family\"),")
          }
        }
        line()

        block("enum class OsArch(", ") {") {
          line("val value: String,")
        }
        block("", "}") {
          ctx.allOsArches.forEach { (arch, prettyName) ->
            line("$prettyName(\"$arch\"),")
          }
        }
        line()

        block("override fun equals(other: Any?): Boolean {", "}") {
          line("if (this === other) return true")
          line("if (other !is KnBuildPlatform) return false")
          line("if (family != other.family) return false")
          line("if (arch != other.arch) return false")
          line("return true")
        }
        line()

        block("override fun hashCode(): Int {", "}") {
          line("var result = family.hashCode()")
          line("result = 31 * result + arch.hashCode()")
          line("return result")
        }
        line()

        block("override fun toString(): String =", "") {
          block("buildString{", "}") {
            line("append(\"KnBuildPlatform(\")")
            line("append(family)")
            line("append(\"-\")")
            line("append(arch)")
            line("append(\")\")")
          }
        }
        line()

        groupedPlatforms.forEach { (family, platforms) ->
          val osName = ctx.allOsFamilies[family] ?: error("Unknown OS family: $family")

          line()
          block("object $osName {", "}") {
            platforms.forEach { platform ->
              val arch = ctx.allOsArches[platform.osArch] ?: error("Unknown OS arch: ${platform.osArch}")
              line()
              block("val ${arch}: KnBuildPlatform = ", "") {
                block("KnBuildPlatform(", ")") {
                  line("family = OsFamily.$osName,")
                  line("arch = OsArch.$arch,")
                }
              }
            }
          }
        }

        block("companion object {", "}") {
          block("val allPlatforms: Set<KnBuildPlatform> by lazy {", "}") {
            block("setOf(", ")") {
              groupedPlatforms.forEach { (family, platforms) ->
                val osName = ctx.allOsFamilies[family]
                platforms.forEach { platform ->
                  val arch = ctx.allOsArches[platform.osArch]
                  line("$osName.${arch},")
                }
              }
            }
          }
        }
      }
    }
  )
}

context(ctx: Context)
private fun createKonanCompileTarget() {

  val osFamilies = ctx.allKonanTargets.keys.map { it.family }
    .associateWith { fam ->
      fam.lowercase(Locale.ROOT)
        .replaceFirstChar { it.uppercase(Locale.ROOT) }
        .replace("Osx", "OsX")
        .replace("Tvos", "TvOs")
        .replace("Ios", "IOs")
        .replace("Watchos", "WatchOs")
    }

  val architectures = ctx.allKonanTargets.keys.map { it.architecture }
    .associateWith { fam ->
      fam.lowercase(Locale.ROOT)
        .replaceFirstChar { it.uppercase(Locale.ROOT) }
    }

  ctx.outputDir.resolve("KnCompileTarget.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()

      line("/**")
      line(" * A target platform for Kotlin/Native code.")
      line(" */")
      block("class KnCompileTarget\nprivate constructor(", "): java.io.Serializable {") {
        line("/** [org.jetbrains.kotlin.konan.target.KonanTarget.name] */")
        line("val name: String,")
        line("/** [org.jetbrains.kotlin.konan.target.KonanTarget.family] */")
        line("val os: OsFamily,")
        line("/** [org.jetbrains.kotlin.konan.target.KonanTarget.architecture] */")
        line("val arch: Architecture,")
      }
      block("", "}") {
        line()

        block("enum class Architecture(", ") {") {
          line("val id: String,")
        }
        block("", "}") {
          architectures.forEach { (arch, prettyName) ->
            line("$prettyName(\"$arch\"),")
          }
        }
        line()

        block("enum class OsFamily(", ") {") {
          line("val id: String,")
        }
        block("", "}") {
          osFamilies.forEach { (family, prettyName) ->
            line("$prettyName(\"$family\"),")
          }
        }
        line()

        block("override fun equals(other: Any?): Boolean {", "}") {
          line("if (this === other) return true")
          line("if (other !is KnCompileTarget) return false")
          line("if (name != other.name) return false")
          line("if (os != other.os) return false")
          line("if (arch != other.arch) return false")
          line("return true")
        }
        line()

        block("override fun hashCode(): Int {", "}") {
          line("var result = name.hashCode()")
          line("result = 31 * result + os.hashCode()")
          line("result = 31 * result + arch.hashCode()")
          line("return result")
        }
        line()

        block("override fun toString(): String =", "") {
          block("buildString{", "}") {
            line("append(\"KnCompileTarget(\")")
            line("append(name)")
            line("append(\")\")")
          }
        }
        line()

        block("companion object {", "}") {
          block("val allTargets: Set<KnCompileTarget> by lazy {", "}") {
            block("setOf(", ")") {
              ctx.allKonanTargets.forEach { (_, prettyName) ->
                line("$prettyName,")
              }
            }
          }

          ctx.allKonanTargets.forEach { (target, prettyName) ->
            line()
            block("val $prettyName: KnCompileTarget = ", "") {
              block("KnCompileTarget(", ")") {
                line("name = \"${target.name}\",")
                line("os = OsFamily.${osFamilies[target.family]},")
                line("arch = Architecture.${architectures[target.architecture]},")
              }
            }
          }
        }
      }
    }
  )
}


context(ctx: Context)
private fun createKnpDependency() {

  ctx.outputDir.resolve("KnpDependency.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()

      block("data class KnpDependency internal constructor(", ") {") {
        line("val coord: String,")
        line("val artifact: String? = null,")
      }
      block("", "}") {

        line()
        line("val group: String")
        line("val module: String")
        line("val version: String")
        line("val classifier: String?")
        line("val extension: String")

        line()
        block("init {", "}") {
          line("val match = coordRegex.matchEntire(coord)")
          line($$"  ?: error(\"Invalid dependency notation: $coord\")")
          line("""group = match.groups["group"]?.value""")
          line($$"""  ?: error("Missing 'group' in dependency notation: $coord")""")
          line("""module = match.groups["module"]?.value""")
          line($$"""  ?: error("Missing 'module' in dependency notation: $coord")""")
          line("""version = match.groups["version"]?.value""")
          line($$"""  ?: error("Missing 'version' in dependency notation: $coord")""")
          line("""classifier = match.groups["classifier"]?.value // Optional, can be null""")
          line("""extension = match.groups["extension"]?.value""")
          line($$"""  ?: error("Missing 'extension' in dependency notation: $coord")""")
        }
        line()
        block("companion object {", "}") {
          line("private val coordRegex: Regex =")
          line(buildString {
            append("  ")
            append("Regex(\"\"\"")
            append("(?<group>[^:]+):(?<module>[^:]+):(?<version>[^:@]+)(?::(?<classifier>[^@]+))?@(?<extension>[.a-z]+)")
            append("\"\"\")")
          })
        }
      }
    }
  )
}

context(ctx: Context)
private fun createKnpDepData() {

  val dataDir = ctx.outputDir.resolve("data").createDirectories()

  dataDir.resolve("KnDependencyDataSpec.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data.content")
      line()
      line("import dev.adamko.kntoolchain.tools.data.*")
      line()

      line("/**")
      line(" * Data for a K/N compile target.")
      line(" */")
      block("sealed class KnDependencyDataSpec {", "}") {
        line()
        line("abstract val version: KnpVersion")
        line()
        line("abstract val buildPlatform: KnBuildPlatform")
        line()
        line("abstract val compileTarget: KnCompileTarget")
        line()
        line("abstract val dependencies: Set<KnpDependency>")
        line()
        line("companion object")
      }
    }
  )

  val allKnDependencyDataSpecs = mutableSetOf<String>()

  val dataGroupedByVersion: Map<String, List<KotlinVersionTargetDependencies>> =
    ctx.report.data.groupBy { it.version }

  dataGroupedByVersion.forEach { (version, reports) ->
    val versionPretty = ctx.allVersions[version] ?: error("Unknown version: $version")

    val outputFile = dataDir.resolve("KnDependencyData_${versionPretty}.kt")

    outputFile.writeText(
      buildStringBlock {
        line("package dev.adamko.kntoolchain.tools.data.content")
        line()
        line("import dev.adamko.kntoolchain.tools.data.*")
        line()

        val clsNameVersion = "KnDependencyData_${versionPretty}"
        line("""@Suppress("ClassName")""")
        block("sealed class ${clsNameVersion}: KnDependencyDataSpec() {", "}") {

          line()
          line("override val version: KnpVersion = KnpVersion.$versionPretty")

          val dataGroupedByBuildPlatform = reports.groupBy { it.buildPlatform }

          dataGroupedByBuildPlatform.forEach { (buildPlatform, targetDependencies) ->
            val buildOs = ctx.allOsFamilies[buildPlatform.osFamily]
              ?: error("Unknown OS family: ${buildPlatform.osFamily}")
            val buildArch = ctx.allOsArches[buildPlatform.osArch]
              ?: error("Unknown OS arch: ${buildPlatform.osArch}")

            val baseClsName = "${buildOs}_${buildArch}"

            line()
            line("""@Suppress("ClassName")""")
            block("sealed class ${baseClsName}: ${clsNameVersion}() {", "}") {

              line()
              line("override val buildPlatform: KnBuildPlatform = KnBuildPlatform.${buildOs}.${buildArch}")

              targetDependencies.forEach { dependencies ->

                val targetName = ctx.allKonanTargets[dependencies.targetPlatform]
                  ?: error("Unknown target: ${dependencies.targetPlatform}")

                allKnDependencyDataSpecs += "${clsNameVersion}.${baseClsName}.${targetName}"

                line()
                block("object ${targetName}: ${baseClsName}() {", "}") {
                  line()
                  line("override val compileTarget: KnCompileTarget = KnCompileTarget.${targetName}")
                  line()
                  block("override val dependencies: Set<KnpDependency> =", "") {
                    block("setOf(", ")") {
                      dependencies.dependencies.forEach { dep ->
                        if (dep.artifact != null) {
                          line("KnpDependency(\"${dep.coords()}\", \"${dep.artifact}\"),")
                        } else {
                          line("KnpDependency(\"${dep.coords()}\"),")
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    )
  }

  ctx.outputDir.resolve("knDependencyData.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()
      line("import dev.adamko.kntoolchain.tools.data.content.*")
      line()
      block("val knDependencyData: Set<KnDependencyDataSpec> =", "") {
        block("setOf(", ")") {
          allKnDependencyDataSpecs.forEach { spec ->
            line("$spec,")
          }
        }
      }
    }
  )
}
