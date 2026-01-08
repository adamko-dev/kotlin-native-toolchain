package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.test_utils.GradleTestContext
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir


class TaskRegistrationTest {
  @Test
  fun `knt settings plugin`(
    @TempDir tmpDir: Path,
  ) {
    with(GradleTestContext(tmpDir)) {
      settingsGradleKts += """
        |plugins {
        |  id("dev.adamko.kotlin-native-toolchain") version "+"
        |}
        |""".trimMargin()

      runner
        .withArguments(
          "tasks",
          "--stacktrace",
          "--full-stacktrace",
        )
        .build()
        .apply {
          output shouldContain "checkKnToolchains - Checks that the Kotlin/Native prebuilt toolchain installation is valid."
        }
    }
  }

  @Test
  fun `knt project plugin`(
    @TempDir tmpDir: Path,
  ) {
    with(GradleTestContext(tmpDir)) {
      buildGradleKts += """
        |plugins {
        |  id("dev.adamko.kotlin-native-toolchain") version "+"
        |}
        |""".trimMargin()

      runner
        .withArguments(
          "tasks",
          "--stacktrace",
          "--full-stacktrace",
        )
        .build()
        .apply {
          output shouldNotContain "checkKnToolchains - Checks that the Kotlin/Native prebuilt toolchain installation is valid."
        }
    }
  }
}
