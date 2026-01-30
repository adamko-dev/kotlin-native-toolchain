import java.io.ByteArrayOutputStream
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.*
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.process.ExecOperations
import org.gradle.work.NormalizeLineEndings

@CacheableTask
abstract class CompileCLibTask
@Inject
internal constructor(
  private val exec: ExecOperations,
  private val fs: FileSystemOperations,
  private val layout: ProjectLayout,
  private val providers: ProviderFactory,
) : DefaultTask() {

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @get:Input
  abstract val libName: Property<String>

  @get:Input
  abstract val konanTargetName: Property<String>

  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  @get:IgnoreEmptyDirectories
  @get:NormalizeLineEndings
  abstract val cSourceFiles: ConfigurableFileCollection

  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  @get:IgnoreEmptyDirectories
  @get:NormalizeLineEndings
  abstract val cHeaderDirs: ConfigurableFileCollection

  @get:LocalState
  val workDir: Path
    get() = temporaryDir.resolve("workdir").toPath()

  @get:Internal
  abstract val runKonan: Property<Path>

  init {
    group = project.name
  }

  @TaskAction
  protected fun action() {
    val outputDir: Path = outputDir.get().asFile.toPath()

    fs.sync {
      it.from(cSourceFiles)
      it.into(workDir)
    }

    runClang()

    runAr()

    fs.sync {
      it.from(workDir)
      it.into(outputDir)
      it.include("*.a")
    }
  }

  private fun runClang() {
    val konanTargetName: String = konanTargetName.get()

    val cSourceFiles = cSourceFiles
      .asFileTree
      .map { it.toPath() }
      .filter { it.extension == "c" }

    val cHeaderDirs = cHeaderDirs
      .files
      .map { it.toPath() }
      .filter { it.isDirectory() }
      .filter { it.listDirectoryEntries().isNotEmpty() }

    val clangArgs = buildList {
      add("clang")
      add("clang")
      add(konanTargetName)
      cHeaderDirs.forEach {
        add("-I")
        add(it.invariantSeparatorsPathString)
      }
      add("-c")
      cSourceFiles.forEach {
        add(it.invariantSeparatorsPathString)
      }
    }

    logger.lifecycle("$path clangArgs: $clangArgs")

    execRunKonan(clangArgs)
  }

  private fun runAr() {
    val libName = libName.get()
    val objFiles = workDir.walk()
      .filter { it.extension == "o" }

    val arArgs = buildList {
      add("llvm")
      add("llvm-ar")
      add("-r")
      add("lib${libName}.a")
      objFiles.forEach {
        add(it.invariantSeparatorsPathString)
      }
    }

    logger.lifecycle("$path arArgs: $arArgs")

    execRunKonan(arArgs)
  }

  private fun execRunKonan(
    args: List<String>,
  ) {
    val runKonan = runKonan.get().absolute().normalize()
    val kotlinNativeHomeDir = runKonan.parent.parent
    val konanDataDir = runKonan.parent.parent.parent
    val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)

    val commandLine = buildList {
      if (isWindows) {
        add("cmd")
        add("/c")
        add(
          buildList {
            add(runKonan.pathString)
            addAll(args)
          }.joinToString(" ")
        )
      } else {
        add(runKonan.pathString)
        addAll(args)
      }
    }

    ByteArrayOutputStream().use { execOutput ->
      val execResult = exec.exec { spec ->
        spec.workingDir(workDir)
        spec.commandLine(commandLine)
        spec.environment("KONAN_DATA_DIR", konanDataDir.pathString)
        spec.standardOutput = execOutput
        spec.errorOutput = execOutput
        spec.isIgnoreExitValue = true
      }
      check(execResult.exitValue == 0) {
        """
        ${runKonan.name} failed ${execResult.exitValue}
        ---
        ${execOutput.toString()}
        ---
        """.trimIndent()
      }
    }
  }
}
