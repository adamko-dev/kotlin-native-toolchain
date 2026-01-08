package dev.adamko.kntoolchain.internal

import java.io.InputStream
import java.io.OutputStream.nullOutputStream
import java.nio.file.Path
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile


internal inline fun buildChecksum(
  algorithm: String = "SHA-256",
  checksummer: BuildChecksumContext.() -> Unit,
): String {
  val checksumBytes = buildChecksumBytes(
    algorithm = algorithm,
    checksummer = checksummer,
  )
  return Base64.getEncoder().encodeToString(checksumBytes)
}


internal inline fun buildChecksumBytes(
  algorithm: String = "SHA-256",
  checksummer: BuildChecksumContext.() -> Unit,
): ByteArray {
  val messageDigester = MessageDigest.getInstance(algorithm)
  DigestOutputStream(nullOutputStream(), messageDigester).use { digestStream ->
    val ctx = newBuildChecksumContext(digestStream)
    ctx.checksummer()
  }
  return messageDigester.digest()
}


internal sealed interface BuildChecksumContext {

  fun checksum(value: Int)

  fun checksum(bytes: ByteArray)

  /**
   * Transfer [inputStream] to the checksum.
   *
   * Does not close the input stream.
   */
  fun checksum(inputStream: InputStream)

  companion object {

    /**
     * Add the content of the file [src] to the checksum.
     *
     * [src] must be an existing regular file.
     */
    fun BuildChecksumContext.checksumFile(src: Path) {
      require(src.isRegularFile()) {
        "src $src must be a regular file, but was ${src.describeType()}"
      }
      src.inputStream().use {
        checksum(it)
      }
    }

    /**
     * Add the content of [value] to the checksum.
     *
     * The value will be encoded using UTF8 - see [String.encodeToByteArray].
     */
    fun BuildChecksumContext.checksum(value: String): Unit =
      checksum(value.encodeToByteArray())

    /**
     * Add [value] to the checksum.
     *
     * The value will be encoded using UTF8 - see [String.encodeToByteArray].
     */
    fun BuildChecksumContext.checksum(value: Long): Unit =
      checksum(value.toByteArray())
  }
}


@PublishedApi
internal fun newBuildChecksumContext(
  digestStream: DigestOutputStream
): BuildChecksumContext =
  BuildChecksumContextImpl(digestStream)


private class BuildChecksumContextImpl(
  private val digestStream: DigestOutputStream,
) : BuildChecksumContext {
  override fun checksum(value: Int) {
    digestStream.write(value)
  }

  override fun checksum(bytes: ByteArray) {
    digestStream.write(bytes)
  }

  override fun checksum(inputStream: InputStream) {
    inputStream.transferTo(digestStream)
  }
}
