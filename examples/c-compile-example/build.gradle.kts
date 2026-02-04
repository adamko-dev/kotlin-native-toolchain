import dev.adamko.kntoolchain.tools.data.KnCompileTarget
import dev.adamko.kntoolchain.tools.data.KnpVersion
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform") version "2.3.0"
  id("dev.adamko.kotlin-native-toolchain") version "main-SNAPSHOT"
}

kotlin {
  macosArm64()
  macosX64()
  linuxX64()
  mingwX64()

  compilerOptions {
    optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
  }

  targets.withType<KotlinNativeTarget>().configureEach {

    binaries.executable {
      entryPoint("main")
    }

    compilations.named<KotlinNativeCompilation>("main") {
      cinterops {
        register("myclib") {
          definitionFile.set(layout.projectDirectory.file("myclib.def"))
        }
      }
    }
  }
}


//region Simple compile
// Uses pre-installed clang and ar.
// This will work for the host platform, but not for cross-platform compilation.
val compileMyCLib by tasks.registering {
  group = project.name

  val exec = serviceOf<ExecOperations>()
  val fs = serviceOf<FileSystemOperations>()

  val clibSrcDir = layout.projectDirectory.dir("myclib/src")
  inputs.dir(clibSrcDir)
    .withPathSensitivity(PathSensitivity.RELATIVE)
    .normalizeLineEndings()

  val workDir = temporaryDir.resolve("workdir")
  val outputDir = layout.projectDirectory.dir("staticlibs")
  outputs.dir(outputDir)

  outputs.cacheIf { true }

  doLast {
    fs.sync {
      from(clibSrcDir)
      into(workDir)
    }

    exec.exec {
      executable("clang")
      args(
        "-c",
        "myclib.c",
        "-o",
        "myclib.o"
      )
      workingDir(workDir)
    }

    exec.exec {
      executable("ar")
      args("r", "libmyclib.a", "myclib.o")
      workingDir(workDir)
    }

    fs.sync {
      from(workDir)
      into(outputDir)
      include("*.a")
    }
  }
}
//endregion

knToolchain {
  kotlinNativePrebuiltDistribution {
    version = KnpVersion.V2_3_0
    // Only fetch the required kotlin-native-prebuilt distributions.
    // (This could be improved by only fetching knp-dists supported by the host machine,
    // e.g. don't fetch OsX when running on Windows.)
    compileTargets = KnCompileTarget.allTargets.filter {
      it.os in setOf(
        KnCompileTarget.OsFamily.OsX,
        KnCompileTarget.OsFamily.Linux,
        KnCompileTarget.OsFamily.Mingw,
      )
    }
  }
}

val compileMyClibForAll by tasks.registering {
  group = project.name
}

kotlin.targets.withType<KotlinNativeTarget>().all target@{
  val konanTargetName = this@target.konanTarget.name
  val staticLibDir = layout.projectDirectory.dir("staticlibs/$konanTargetName").asFile

  val compileMyClibForTarget =
    tasks.register(
      "compileMyClibFor${this@target.name.replaceFirstChar { it.uppercaseChar() }}",
      CompileCLibTask::class
    ) {
      libName.set("myclib")
      outputDir = staticLibDir
      cSourceFiles.from("myclib/src")
      cHeaderDirs.from("myclib/include")
      runKonan = knToolchain.runKonan()
      this.konanTargetName = konanTargetName
    }

  compileMyClibForAll.configure {
    dependsOn(compileMyClibForTarget)
  }

  compilations.named<KotlinNativeCompilation>("main") {
    cinterops.all {
      project.tasks.named(interopProcessingTaskName) {
        dependsOn(compileMyClibForTarget)
      }
    }
  }

  binaries.configureEach {
    linkTaskProvider.configure {
      dependsOn(compileMyClibForTarget)
      toolOptions.freeCompilerArgs.addAll(
        "-linker-option",
        "-L${staticLibDir.invariantSeparatorsPath}"
      )
    }
  }
}
