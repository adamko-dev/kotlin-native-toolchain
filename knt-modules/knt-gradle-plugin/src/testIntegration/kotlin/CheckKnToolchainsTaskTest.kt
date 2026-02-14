package dev.adamko.kntoolchain

import dev.adamko.kntoolchain.test_utils.GradleTestContext
import dev.adamko.kntoolchain.test_utils.junit.KnpOsArchArgs
import dev.adamko.kntoolchain.test_utils.projects.setupProjectForKnpDownload
import dev.adamko.kntoolchain.tools.data.KnBuildPlatform
import io.kotest.matchers.collections.shouldContainAllInAnyOrder
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import kotlin.io.path.*
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class CheckKnToolchainsTaskTest {

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  fun `when knp is not requested - expect no konanDataDir is empty - and checkKnToolchains task succeeds`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    setupProject(buildPlatform)

    runner
      .withArguments(
        "check",
      )
      .forwardOutput()
      .build()
      .apply {
        konanDataDir.shouldNotExist()

        task(":checkKnToolchains")?.outcome shouldBe TaskOutcome.SUCCESS
      }
  }

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  fun `when knp is provisioned - expect checkKnToolchains task succeeds`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {

    setupProject(buildPlatform)

    runner.withArguments("knToolchainUser")
      .forwardOutput()
      .build()
      .apply {
        task(":knToolchainUser")?.outcome shouldBe TaskOutcome.SUCCESS
      }

    konanDataDir.shouldExist()

    runner
      .withArguments(
        "check",
      )
      .forwardOutput()
      .build()
      .apply {
        task(":checkKnToolchains")?.outcome shouldBe TaskOutcome.SUCCESS
      }
  }

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  fun `when knp is provisioned - but checksums are missing - expect checkKnToolchains task fails`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = testChecksumMismatch(
    buildPlatform = buildPlatform,
    tmpDir = tmpDir,
    modifyChecksumFile = { it.deleteExisting() },
  )

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  fun `when knp is provisioned - but checksums are invalid - expect checkKnToolchains task fails`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = testChecksumMismatch(
    buildPlatform = buildPlatform,
    tmpDir = tmpDir,
    modifyChecksumFile = { it.writeText("invalid checksum") },
  )

  private fun testChecksumMismatch(
    buildPlatform: KnBuildPlatform,
    tmpDir: Path,
    modifyChecksumFile: (Path) -> Unit,
  ): Unit = with(GradleTestContext(tmpDir)) {

    setupProject(buildPlatform)

    runner.withArguments("knToolchainUser")
      .forwardOutput()
      .build()
      .apply {
        task(":knToolchainUser")?.outcome shouldBe TaskOutcome.SUCCESS
      }

    konanDataDir.shouldExist()
    konanDataDir.resolve("checksums")
      .walk()
      .filter { it.isRegularFile() }
      .filter { it.extension == "hash" }
      .forEach(modifyChecksumFile)

    val allKnpDependencies =
      buildList {
        konanDataDir.listDirectoryEntries()
          .filter { it.isDirectory() }
          .filter { it.startsWith("kotlin-native-prebuilt-") }
          .forEach { add(it.name) }
        konanDataDir.resolve("dependencies")
          .listDirectoryEntries()
          .filter { it.isDirectory() }
          .forEach { add(it.name) }
      }

    runner
      .withArguments(
        "check",
      )
      .forwardOutput()
      .buildAndFail()
      .apply {
        task(":checkKnToolchains")?.outcome shouldBe TaskOutcome.FAILED

        val testOutput = outputReader.useLines { lines ->
          lines
            .dropWhile { it != "> Task :checkKnToolchains FAILED" }
            .takeWhile { it != "FAILURE: Build failed with an exception." }
            .joinToString("\n")
        }

        val checksumTestsOutput =
          testOutput
            .split("\n\n")
            .filter { it.startsWith("KnToolchainIntegrity") }


        val actualCheckedDirs =
          checksumTestsOutput
            .map {
              it.substringBefore(" FAILED\n")
                .substringAfterLast(" > ")
            }

        actualCheckedDirs shouldContainAllInAnyOrder allKnpDependencies
      }
  }

  @ParameterizedTest
  @ArgumentsSource(KnpOsArchArgs::class)
  fun `expect checkKnToolchains supports build-cache`(
    buildPlatform: KnBuildPlatform,
    @TempDir tmpDir: Path,
  ): Unit = with(GradleTestContext(tmpDir)) {
    setupProject(buildPlatform)

    gradleProperties += """
      |org.gradle.caching.debug=true
      |""".trimMargin()

    runner.withArguments("knToolchainUser")
      .forwardOutput()
      .build()
      .apply {
        task(":knToolchainUser")?.outcome shouldBe TaskOutcome.SUCCESS
      }

    konanDataDir.shouldExist()

    runner
      .withArguments("check")
      .forwardOutput()
      .build()
      .apply {
        task(":checkKnToolchains")?.outcome shouldBe TaskOutcome.SUCCESS
      }

    runner
      .withArguments("clean")
      .forwardOutput()
      .build()
      .apply {
        task(":clean")?.outcome shouldBe TaskOutcome.SUCCESS
      }

    runner
      .withArguments("check")
      .forwardOutput()
      .build()
      .apply {
        task(":checkKnToolchains")?.outcome shouldBe TaskOutcome.FROM_CACHE
      }
  }

  fun `expect check failure when knp dir is modified`() {}
  fun `expect check failure when knp dependency dir is modified`() {}
}

private fun GradleTestContext.setupProject(buildPlatform: KnBuildPlatform) {
  setupProjectForKnpDownload(
    buildPlatform = buildPlatform,
  )

  buildGradleKts += """
        |val knToolchainUser by tasks.registering {
        |  val knToolchain = knToolchain.provisionInstallation()
        |
        |  doLast {
        |    val knToolchain = knToolchain.get()
        |    println("knToolchain dir: {" + knToolchain.invariantSeparatorsPathString + "}")
        |  }
        |}
        |""".trimMargin()
}
