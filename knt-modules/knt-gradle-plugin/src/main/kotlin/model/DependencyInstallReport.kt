package dev.adamko.kntoolchain.model

import java.nio.file.Path


/**
 * Describe the status of a kotlin-native-prebuilt dependency installation.
 *
 * Created by [dev.adamko.kntoolchain.operations.CreateKnToolchainsStatusReport].
 */
internal data class DependencyInstallReport internal constructor(
  val konanDataDir: Path,
  val dependencies: List<DependencyInfo>,
) {

  internal data class DependencyInfo internal constructor(
    val archiveName: String,
    val installDir: Path,
    val status: DependencyStatus,
  )

  internal sealed interface DependencyStatus {
    data object Valid : DependencyStatus

    data class Invalid internal constructor(
      val reason: String,
      val details: String? = null,
    ) : DependencyStatus
  }
}
