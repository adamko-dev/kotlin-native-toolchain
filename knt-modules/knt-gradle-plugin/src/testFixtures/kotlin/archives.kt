package dev.adamko.kntoolchain.test_utils

import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.outputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream

fun Path.createModuleTarGz(
  name: String,
  content: (name: String, sink: TarArchiveOutputStream) -> Unit = { _, _ -> },
): Path {
  require(this.isDirectory())

  val out: Path = resolve("$name.tar.gz")
  val entryName = "$name/demo-content.txt"
  val content = "dummy module $name\n".toByteArray(Charsets.UTF_8)

  out.outputStream()
    .let(::GzipCompressorOutputStream)
    .let(::TarArchiveOutputStream)
    .use { sink ->
      val entry = TarArchiveEntry(entryName).apply {
        name
        size = content.size.toLong()
      }
      sink.putArchiveEntry(entry)
      sink.write(content)
      sink.closeArchiveEntry()

      content(name, sink)

      sink.finish()
    }

  return out
}

fun Path.createModuleZip(
  name: String,
  content: (name: String, sink: ZipArchiveOutputStream) -> Unit = { _, _ -> },
): Path {
  require(this.isDirectory())

  val out: Path = resolve("$name.zip")
  val entryName = "$name/demo-content.txt"
  val content = "dummy module $name\n".toByteArray(Charsets.UTF_8)

  out.outputStream()
    .let(::ZipArchiveOutputStream)
    .use { sink ->

      // ensure root dir exists in zip
      sink.putArchiveEntry(ZipArchiveEntry("$name/"))
      sink.closeArchiveEntry()

      val entry = ZipArchiveEntry(entryName).apply {
        size = content.size.toLong()
      }
      sink.putArchiveEntry(entry)
      sink.write(content)
      sink.closeArchiveEntry()

      content(name, sink)

      sink.finish()
    }

  return out
}
