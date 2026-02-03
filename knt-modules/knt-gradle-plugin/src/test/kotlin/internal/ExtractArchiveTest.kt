package dev.adamko.kntoolchain.internal

import dev.adamko.kntoolchain.internal.utils.extractArchive
import dev.adamko.kntoolchain.test_utils.createModuleTarGz
import dev.adamko.kntoolchain.test_utils.createModuleZip
import dev.adamko.kntoolchain.test_utils.toTreeString
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ExtractArchiveTest {

  @Test
  fun extractZip(
    @TempDir tmpDir: Path,
  ) {
    val zipFile = tmpDir.createModuleZip("foo")
    val destDir = tmpDir.resolve("dest")

    extractArchive(
      archive = zipFile,
      destinationDir = destDir,
      excludes = emptySet(),
    )

    destDir.toTreeString() shouldBe """
      dest/
      └── demo-content.txt
      """.trimIndent()
  }

  @Test
  fun extractTarGz(
    @TempDir tmpDir: Path,
  ) {
    val zipFile = tmpDir.createModuleTarGz("foo")
    val destDir = tmpDir.resolve("dest")

    extractArchive(
      archive = zipFile,
      destinationDir = destDir,
      excludes = emptySet(),
    )

    destDir.toTreeString() shouldBe """
      dest/
      └── demo-content.txt
      """.trimIndent()
  }
}
