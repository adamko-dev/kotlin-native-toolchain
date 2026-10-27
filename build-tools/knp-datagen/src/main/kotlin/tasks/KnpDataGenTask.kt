package dev.adamko.kntoolchain.tools.tasks

import dev.adamko.kntoolchain.tools.internal.datamodel.KonanDependenciesReport
import dev.adamko.kntoolchain.tools.internal.datamodel.KotlinVersionTargetDependencies
import dev.adamko.kntoolchain.tools.internal.datamodel.Platform
import dev.adamko.kntoolchain.tools.internal.utils.StringBlockBuilder
import dev.adamko.kntoolchain.tools.internal.utils.buildStringBlock
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.NAME_ONLY
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.gradle.work.NormalizeLineEndings


@CacheableTask
abstract class KnpDataGenTask
internal constructor() : DefaultTask() {

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @get:InputFile
  @get:PathSensitive(NAME_ONLY)
  @get:NormalizeLineEndings
  abstract val konanDependenciesReportFile: RegularFileProperty

  @TaskAction
  protected fun action() {
    prepareOutputDir()
    val konanDependenciesReport = loadKonanDependenciesReport()
    generateKotlinData(konanDependenciesReport)
  }

  private fun prepareOutputDir() {
    outputDir.get().asFile.deleteRecursively()
    outputDir.get().asFile.mkdirs()
  }

  private fun loadKonanDependenciesReport(): KonanDependenciesReport {
    val konanDependenciesReportFile = konanDependenciesReportFile.get().asFile.readText()
    return Json.decodeFromString(
      KonanDependenciesReport.serializer(),
      konanDependenciesReportFile,
    )
  }

  private fun generateKotlinData(report: KonanDependenciesReport) {
    val outputDir = outputDir.get().asFile.toPath()

    val ctx = Context(outputDir, report)

    context(ctx) {
      createKnpVersion()
      createKnBuildTarget()
      createKonanCompileTarget()
      createKonanCompileTargetUtils()
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

  /**
   * Map ID to 'pretty name'.
   */
  val osFamilies = allKonanTargets.keys.map { it.family }
    .associateWith { fam ->
      fam.lowercase(Locale.ROOT)
        .replaceFirstChar { it.uppercase(Locale.ROOT) }
        .replace("Osx", "OsX")
        .replace("Tvos", "TvOs")
        .replace("Ios", "IOs")
        .replace("Watchos", "WatchOs")
    }

  val allBuildPlatforms: SortedSet<Platform> =
    report.data.map { it.buildPlatform }.toSortedSet()

  val allOsFamilies: SortedMap<String, String> =
    allBuildPlatforms.map { it.osFamily }.distinct()
      .associateWith { family ->
        when (family) {
          "linux"   -> "Linux"
          "macos"   -> "MacOs"
          "windows" -> "Windows"
          else      -> error("Unknown OS family: $family")
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
      line(" * A version of a kotlin-native-prebuilt distribution.")
      line(" */")
      block(
        "data class KnpVersion internal constructor(",
        "): java.io.Serializable, Comparable<KnpVersion> {",
      ) {
        line("val name: String,")
        line("val version: String,")
        line("private val major: Int,")
        line("private val minor: Int,")
        line("private val patch: Int,")
        line("private val stage: Stage,")
        line("private val stageRevision: Int,")
      }
      block("", "}") {
        line()
        block("override fun compareTo(other: KnpVersion): Int =", "") {
          block("compareValuesBy(", ")") {
            line("this,")
            line("other,")
            line("KnpVersion::major,")
            line("KnpVersion::minor,")
            line("KnpVersion::patch,")
            line("KnpVersion::stage,")
            line("KnpVersion::stageRevision,")
          }
        }

        line()
        block("internal enum class Stage {", "}") {
          line("Beta,")
          line("RC,")
          line("Release,")
        }

        line()
        block("companion object {", "}") {
          line()
          line("/**")
          line(" * The latest stable release version of a kotlin-native-prebuilt distribution.")
          line(" */")
          block("val latestRelease: KnpVersion by lazy {", "}") {
            line("allVersions.filter { it.stage == Release }.max()")
          }

          line()
          line("/**")
          line(" * All supported kotlin-native-prebuilt distribution versions.")
          line(" */")
          block("val allVersions: Set<KnpVersion> by lazy {", "}") {
            block("setOf(", ")") {
              ctx.allVersions.forEach { (_, name) ->
                line("$name,")
              }
            }
          }

          ctx.allVersions.forEach { (version, name) ->
            line()
            knpVersionInstance(version, name)
          }
        }
      }
    }
  )
}

private fun StringBlockBuilder.knpVersionInstance(
  version: String,
  name: String,
) {
  val majorMinorPatch = version
    .substringBefore("-")
    .split(".")
    .map { it.toInt() }

  require(majorMinorPatch.size == 3) { "Invalid version: $version" }

  val suffix = version.substringAfter("-", "").ifEmpty { null }

  val stage: String
  val stageRevision: Int
  when {
    suffix == null            -> {
      stage = "Release"
      stageRevision = 0
    }

    suffix.startsWith("Beta") -> {
      stage = "Beta"
      stageRevision = suffix.removePrefix(stage).toIntOrNull() ?: 0
    }

    suffix.startsWith("RC")   -> {
      stage = "RC"
      stageRevision = suffix.removePrefix(stage).toIntOrNull() ?: 0
    }

    else                      ->
      error("Unknown version: $version")
  }

  line("/** Version `$version`. */")
  block("val $name: KnpVersion =", "") {
    block("KnpVersion(", ")") {
      line("name = \"$name\",")
      line("version = \"$version\",")
      line("major = ${majorMinorPatch[0]},")
      line("minor = ${majorMinorPatch[1]},")
      line("patch = ${majorMinorPatch[2]},")
      line("stage = ${stage},")
      line("stageRevision = ${stageRevision},")
    }
  }
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
      block("class KnBuildPlatform\nprivate constructor(", ") : java.io.Serializable {") {
        line("val family: OsFamily,")
        line("val arch: OsArch,")
      }
      line()
      block("", "}") {

        sealedEnumClass(
          className = "OsFamily",
          propertyName = "name",
          values = ctx.allOsFamilies,
        )
        line()

        sealedEnumClass(
          className = "OsArch",
          propertyName = "name",
          values = ctx.allOsArches,
        )
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
          block("buildString {", "}") {
            line("append(\"KnBuildPlatform(\")")
            line("append(family.name)")
            line("append(\"-\")")
            line("append(arch.name)")
            line("append(\")\")")
          }
        }

        groupedPlatforms.forEach { (family, platforms) ->
          val osName = ctx.allOsFamilies[family] ?: error("Unknown OS family: $family")

          line()
          block("object $osName {", "}") {
            platforms.forEachIndexed { i, platform ->
              val arch = ctx.allOsArches[platform.osArch] ?: error("Unknown OS arch: ${platform.osArch}")
              if (i > 0) line()
              line("@JvmStatic")
              block("val ${arch}: KnBuildPlatform = ", "") {
                block("KnBuildPlatform(", ")") {
                  line("family = OsFamily.$osName,")
                  line("arch = OsArch.$arch,")
                }
              }
            }
          }
        }

        line()
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

        sealedEnumClass(
          className = "Architecture",
          propertyName = "id",
          values = architectures,
        )
        line()

        sealedEnumClass(
          className = "OsFamily",
          propertyName = "id",
          values = ctx.osFamilies,
        )
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
          block("buildString {", "}") {
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
                line("os = OsFamily.${ctx.osFamilies[target.family]},")
                line("arch = Architecture.${architectures[target.architecture]},")
              }
            }
          }
        }
      }
    }
  )
}


/**
 * Generate a sealed class with a fixed set of instances, used instead of an enum class.
 *
 * The instances are internal data objects, exposed via properties in the companion object.
 *
 * @param[values] Map of each instance's [propertyName] value to its pretty name.
 */
private fun StringBlockBuilder.sealedEnumClass(
  className: String,
  propertyName: String,
  values: Map<String, String>,
) {
  fun internalName(prettyName: String): String =
    prettyName.filter { it.isLetterOrDigit() } + "Internal"

  block("sealed class $className(", ") : java.io.Serializable {") {
    line("val $propertyName: String,")
  }
  block("", "}") {
    block("companion object {", "}") {
      values.values.forEach { prettyName ->
        line("@JvmStatic")
        line("val $prettyName: $className get() = ${internalName(prettyName)}")
        line()
      }
      line("@JvmStatic")
      line("val entries: Set<$className> get() = setOf(${values.values.joinToString()})")
    }
    values.forEach { (value, prettyName) ->
      line()
      block("internal data object ${internalName(prettyName)} : $className(\"$value\") {", "}") {
        line("override fun toString(): String = \"$className.$prettyName\"")
        line()
        line("""@Suppress("unused")""")
        line("private fun readResolve(): Any = ${internalName(prettyName)}")
      }
    }
  }
}

context(ctx: Context)
private fun createKonanCompileTargetUtils() {

  val osFamiliesGroupPrettyNameToOsGroup: Map<String, List<String>> =
    ctx.osFamilies.entries
      .groupBy { (id, _) ->
        when (id) {
          "LINUX"                         -> "Linux"
          "MINGW"                         -> "Windows"
          "ANDROID"                       -> "Android"
          "OSX", "IOS", "WATCHOS", "TVOS" -> "Apple"
          else                            -> error("Unknown OS family: $id")
        }
      }
      .mapValues { (_, entries) ->
        entries.map { (_, prettyName) ->
          prettyName
        }
      }


  ctx.outputDir.resolve("utils.KnCompileTarget.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()

      osFamiliesGroupPrettyNameToOsGroup.forEach { (group, osNames) ->
        line()
        block("val KnCompileTarget.OsFamily.is$group: Boolean", "") {
          line(osNames.joinToString(" || ", prefix = " get() = ") { "this == $it" })
        }
      }
      line()

      osFamiliesGroupPrettyNameToOsGroup.forEach { (osGroup, _) ->
        block("val KnCompileTarget.is$osGroup: Boolean", "") {
          line("get() = os.is$osGroup")
        }
        line()
      }
      line()
    }
  )
}


context(ctx: Context)
private fun createKnpDependency() {

  ctx.outputDir.resolve("KnpDependency.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()

      line("/**")
      line(" * A specific ancillary dependency for a kotlin-native-prebuilt distribution.")
      line(" *")
      line(" * For example, when building on macOS and targeting linux a `gcc` dependency is required.")
      line(" */")
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
          line($$"""  ?: error("Invalid dependency notation: $coord")""")
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

  val contentDir = ctx.outputDir.resolve("content").createDirectories()

  contentDir.resolve("KnDependencyDataSpec.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data.content")
      line()
      line("import dev.adamko.kntoolchain.tools.data.*")
      line()

      line("/**")
      line(" * Details the compilation tools required for a specific host machine and compilation target.")
      line(" */")
      block("sealed class KnDependencyDataSpec {", "}") {
        line()
        line("/** The version of the kotlin-native-prebuilt distribution. */")
        line("abstract val version: KnpVersion")
        line()
        line("/** The platform on which the compilation tools are executed. */")
        line("abstract val buildPlatform: KnBuildPlatform")
        line()
        line("/** The Kotlin Native target platform that the compiler will generate code for. */")
        line("abstract val compileTarget: KnCompileTarget")
        line()
        line("/** The required ancillary dependencies for the kotlin-native-prebuilt distribution. */")
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

    val outputFile = contentDir.resolve("KnDependencyData_${versionPretty}.kt")

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
      line("/**")
      line(" * All known [KnDependencyDataSpec] instances.")
      line(" */")
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
