package dev.adamko.kntoolchain.model

import dev.adamko.kntoolchain.tools.data.KnBuildPlatform
import dev.adamko.kntoolchain.tools.data.KnCompileTarget
import java.nio.file.Path

/**
 * A provisioned Kotlin-native-prebuilt toolchain.
 *
 * Can be used on [buildPlatform] to target [compileTargets].
 */
data class ProvisionedKnToolchain internal constructor(
  /** The platform the toolchain runs on. */
  val buildPlatform: KnBuildPlatform,
  val compileTargets: Set<KnCompileTarget>,
  /** The base directory where all distributions are installed (`KONAN_DATA_DIR`). */
  val konanDataDir: Path,
  /** The directory where this specific distribution was installed. */
  val installationDir: Path,
  /** Path to the `run_konan` or `run_konan.bat` script. */
  val runKonan: Path,
)
