package dev.adamko.kntoolchain.tools.internal.utils

import java.io.OutputStream.nullOutputStream
import java.nio.file.Path
import java.security.DigestOutputStream
import java.security.MessageDigest
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile

/**
 * Create an MD5 checksum for the content of the file if it exists.
 * Otherwise, return `null`.
 */
internal fun Path.md5ChecksumContentOrNull(): String? {
  if (!exists()) return null

  return md5ChecksumContent()
}

/**
 * Create an MD5 checksum for the content of the file.
 */
internal fun Path.md5ChecksumContent(): String =
  checksum(this, "MD5")

private fun checksum(path: Path, algorithm: String): String {
  val md = MessageDigest.getInstance(algorithm)

  require(path.isRegularFile()) {
    "Cannot checksum $path: it is not a regular file"
  }

  DigestOutputStream(nullOutputStream(), md).use { os ->
    path.inputStream().use {
      it.transferTo(os)
    }
  }

  return md.digest().toHexString()
}
