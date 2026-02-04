package internal.utils

import dev.adamko.kntoolchain.tools.internal.utils.md5ChecksumContent
import dev.adamko.kntoolchain.tools.internal.utils.md5ChecksumContentOrNull
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class FilesChecksumsTest {

  @Test
  fun `test md5ChecksumContent`(
    @TempDir tempDir: Path,
  ) {
    val fileContent = "iqklwwvzcfgxkteckcfrhpv"
    val expectedChecksum = "000486b2f7418f7432056f8ec660bc3d"

    val file1 = tempDir.resolve("foo.txt").apply {
      writeText(fileContent)
    }
    file1.md5ChecksumContent() shouldBe expectedChecksum

    val file2 = tempDir.resolve("x/y/z.bar.txt").apply {
      parent.createDirectories()
      writeText(fileContent)
    }
    file2.md5ChecksumContent() shouldBe expectedChecksum
  }

  @Test
  fun `test md5ChecksumContentOrNull`(
    @TempDir tempDir: Path,
  ) {
    val file = tempDir.resolve("foo.txt")
    file.md5ChecksumContentOrNull() shouldBe null

    file.writeText("")
    file.md5ChecksumContentOrNull() shouldBe "d41d8cd98f00b204e9800998ecf8427e"
  }
}
