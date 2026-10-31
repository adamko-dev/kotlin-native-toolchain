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

    val ctx = Context(
      kotlinDir = outputDir.resolve("kotlin"),
      resourcesDir = outputDir.resolve("resources"),
      report = report,
    )

    context(ctx) {
      createKnpVersion()
      createKnBuildTarget()
      createKonanCompileTarget()
      createKonanCompileTargetUtils()
      createKnDependencyDataResource()
      createKnpDependency()
    }
  }
}

private class Context(
  kotlinDir: Path,
  resourcesDir: Path,
  val report: KonanDependenciesReport,
) {

  val kotlinDir: Path = kotlinDir.createDirectories()
  val resourcesDir: Path = resourcesDir.createDirectories()

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
  ctx.kotlinDir.resolve("KnpVersion.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()
      line("import java.io.Serializable as JavaIoSerializable")
      line()

      line("/**")
      line(" * A version of a kotlin-native-prebuilt distribution.")
      line(" */")
      block(
        "data class KnpVersion internal constructor(",
        "): JavaIoSerializable, Comparable<KnpVersion> {",
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

private const val KnBuildPlatformName = "KnBuildPlatform"

context(ctx: Context)
private fun createKnBuildTarget() {

  val groupedPlatforms = ctx.allBuildPlatforms.groupBy { it.osFamily }


  ctx.kotlinDir.resolve("${KnBuildPlatformName}.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()
      line("import java.io.Serializable as JavaIoSerializable")
      line()

      line("/**")
      line(" * A platform that can build Kotlin/Native code.")
      line(" */")
      block("class ${KnBuildPlatformName}\nprivate constructor(", ") : JavaIoSerializable {") {
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
          line("if (other !is ${KnBuildPlatformName}) return false")
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
            line("append(\"${KnBuildPlatformName}(\")")
            line("append(family.name)")
            line("append(\"-\")")
            line("append(arch.name)")
            line("append(\")\")")
          }
        }

        groupedPlatforms.forEach { (family, platforms) ->
          val osName = ctx.allOsFamilies[family] ?: error("Unknown OS family: $family")

          line()
          dataObject(name = osName, scope = KnBuildPlatformName, supertypes = "JavaIoSerializable") {
            platforms.forEachIndexed { i, platform ->
              val arch = ctx.allOsArches[platform.osArch] ?: error("Unknown OS arch: ${platform.osArch}")
              if (i > 0) line()
              line("@JvmStatic")
              block("val ${arch}: $KnBuildPlatformName = ", "") {
                block("${KnBuildPlatformName}(", ")") {
                  line("family = OsFamily.$osName,")
                  line("arch = OsArch.$arch,")
                }
              }
            }
          }
        }

        line()
        block("companion object {", "}") {
          block("val allPlatforms: Set<${KnBuildPlatformName}> by lazy {", "}") {
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


  ctx.kotlinDir.resolve("KnCompileTarget.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()
      line("import java.io.Serializable as JavaIoSerializable")
      line()

      line("/**")
      line(" * A target platform for Kotlin/Native code.")
      line(" */")
      block("class KnCompileTarget\nprivate constructor(", "): JavaIoSerializable {") {
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

  block("sealed class $className(", ") : JavaIoSerializable {") {
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
      dataObject(
        name = internalName(prettyName),
        prettyName = prettyName,
        scope = className,
        modifiers = "internal",
        supertypes = "$className(\"$value\")",
      )
    }
  }
}


/**
 * Generate a `data object`, with an explicit `toString()` and a `readResolve()`
 * (to preserve the singleton when deserialized).
 */
private fun StringBlockBuilder.dataObject(
  name: String,
  prettyName: String = name,
  scope: String,
  modifiers: String = "",
  supertypes: String = "",
  content: StringBlockBuilder.() -> Unit = {},
) {
  val header = buildString {
    if (modifiers.isNotEmpty()) append("$modifiers ")
    append("data object $name")
    if (supertypes.isNotEmpty()) append(" : $supertypes")
    append(" {")
  }
  block(header, "}") {
    content()
    line("override fun toString(): String = \"${scope}.${prettyName}\"")
    line()
    // data objects don't have readResolve(). Without it deserialization (e.g. Gradle CC) creates duplicate instances.
    // readResolve() must return Any (JVM Object) for Java serialization to detect it.
    line("""@Suppress("unused")""")
    line("private fun readResolve(): Any = $name")
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


  ctx.kotlinDir.resolve("utils.KnCompileTarget.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()

      osFamiliesGroupPrettyNameToOsGroup.forEach { (group, osNames) ->
        line()
        line("/**")
        line(" * `true` if this OS family belongs to the $group group.")
        line(" *")
        line(" * $group group:")
        osNames.forEach { osName ->
          line(" * - [KnCompileTarget.OsFamily.$osName]")
        }
        line(" */")
        block("val KnCompileTarget.OsFamily.is$group: Boolean", "") {
          line(osNames.joinToString(" || ", prefix = " get() = ") { "this == $it" })
        }
      }
      line()

      osFamiliesGroupPrettyNameToOsGroup.forEach { (osGroup, _) ->
        line("/**")
        line(" * `true` if this target's [KnCompileTarget.os] belongs to the $osGroup OS group.")
        line(" *")
        line(" * @see KnCompileTarget.OsFamily.is$osGroup")
        line(" */")
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

  ctx.kotlinDir.resolve("KnpDependency.kt").writeText(
    buildStringBlock {
      line("package dev.adamko.kntoolchain.tools.data")
      line()
      line("import java.io.Serializable as JavaIoSerializable")
      line()

      line("/**")
      line(" * A specific ancillary dependency for a kotlin-native-prebuilt distribution.")
      line(" *")
      line(" * For example, when building on macOS and targeting Linux a `gcc` dependency is required.")
      line(" */")
      block("data class KnpDependency internal constructor(", ") : JavaIoSerializable {") {
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

/**
 * Generate resource files containing the dependencies for every version, build platform, and compile target.
 *
 * The files are loaded at runtime by `knDependencyData`, so data changes don't require code changes.
 * The format must be kept in sync with `parseKnDependencyData()`.
 *
 * ## Format specification
 *
 * One [TOML](https://toml.io) file per Kotlin version, named `knpdeps-<version>.toml`, in `dev/adamko/kntoolchain/tools/data/`.
 *
 * ```toml
 * # comment
 * format = 1
 * kotlinVersion = "2.1.0"
 *
 * [artifacts]
 * "kotlin.native.resources.llvm:16.0.0-x86_64-linux:80@tar.gz" = "llvm-16.0.0-x86_64-linux-essentials"
 *
 * [linux-x86_64]
 * android_arm32 = [
 *   "kotlin.native:target-sysroot:1:android_ndk@tar.gz",
 *   "kotlin.native.resources.llvm:16.0.0-x86_64-linux:80@tar.gz",
 * ]
 * android_arm64 = { sameAs = "android_arm32" }
 *
 * [macos-aarch64]
 * ...
 * ```
 *
 * The files are valid TOML.
 * `parseKnDependencyData()` only supports the subset written by this generator:
 * - full-line `#` comments,
 * - strings without escapes,
 * - multi-line arrays, with one item per line and a trailing comma,
 * - single-line inline tables.
 *
 * ### Root table
 *
 * - `format = <format version>`. The current format version is `1`.
 * - `kotlinVersion = "<version>"`, the Kotlin version the data is for (the same as in the file name).
 *
 * ### `[artifacts]`
 *
 * Optional. Maps dependency coordinates to the name of the downloaded file, if it differs from the module name.
 *
 * ### Build platforms
 *
 * One table per build platform, named `<os family>-<os arch>`, e.g. `[linux-x86_64]`.
 * A build platform that has no table has no data for that version.
 *
 * Each compile target (e.g. `android_arm32`) is a key, with a value that is either:
 * - an array: the dependency coordinates, in Gradle dependency notation,
 *   `group:module:version[:classifier]@extension`.
 * - an inline table `{ sameAs = "<compile target>" }`: another compile target in the same build platform,
 *   with the same dependencies. The inline table allows more options to be added in the future.
 *
 * TOML keys are unique, so each compile target has exactly one entry.
 *
 * The generator uses `sameAs` whenever an entry has the same dependencies as an earlier entry.
 * Build platforms and compile targets are sorted by name.
 */
context(ctx: Context)
private fun createKnDependencyDataResource() {
  val dataDir = ctx.resourcesDir.resolve("dev/adamko/kntoolchain/tools/data").createDirectories()

  ctx.report.data.groupBy { it.version }.forEach { (version, versionEntries) ->

    val entriesByBuildPlatform = versionEntries
      .groupBy { "${it.buildPlatform.osFamily}-${it.buildPlatform.osArch}" }
      .toSortedMap()

    /** The downloaded file name of each dependency, if it differs from the module name. */
    val artifacts = versionEntries
      .flatMap { it.dependencies }
      .groupBy({ it.coords() }, { it.artifact })
      .filterValues { it.filterNotNull().isNotEmpty() }
      .mapValues { (coords, artifacts) ->
        artifacts.distinct().singleOrNull()
          ?: error("Dependency $coords for version $version has conflicting artifacts $artifacts")
      }
      .toSortedMap()

    val data = generateData(version, entriesByBuildPlatform, artifacts)

    dataDir.resolve("knpdeps-$version.toml").writeText(data, Charsets.UTF_8)
  }
}


private fun generateData(
  version: String,
  entriesByBuildPlatform: SortedMap<String, List<KotlinVersionTargetDependencies>>,
  artifacts: SortedMap<String, String>
): String {

  fun tomlString(value: String): String = "\"$value\""

  return buildStringBlock {
    line("# Kotlin/Native $version dependency data.")
    line("format = 1")
    line("kotlinVersion = ${tomlString(version)}")

    if (artifacts.isNotEmpty()) {
      line()
      line("[artifacts]")
      artifacts.forEach { (coords, artifact) ->
        line("${tomlString(coords)} = ${tomlString(artifact)}")
      }
    }

    entriesByBuildPlatform.forEach { (buildPlatform, platformEntries) ->
      val entries = platformEntries.sortedBy { it.targetPlatform }

      /** Map each distinct set of dependencies to the compile target of the first entry that uses it. */
      val firstEntryTargets = mutableMapOf<Set<KotlinVersionTargetDependencies.Coordinates>, String>()

      line()
      line("[$buildPlatform]")
      entries.forEach { entry ->
        val target = entry.targetPlatform.name
        require(entry.dependencies.isNotEmpty()) {
          "Entry '$target' for version $version and build platform $buildPlatform has no dependencies"
        }
        val firstEntryTarget = firstEntryTargets.putIfAbsent(entry.dependencies, target)
        if (firstEntryTarget != null) {
          line("$target = { sameAs = ${tomlString(firstEntryTarget)} }")
        } else {
          block("$target = [", "]") {
            entry.dependencies.forEach { dep ->
              line("${tomlString(dep.coords())},")
            }
          }
        }
      }
    }
  }
}
