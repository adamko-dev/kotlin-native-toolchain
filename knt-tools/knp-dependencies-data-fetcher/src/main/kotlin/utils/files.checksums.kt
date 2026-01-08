package dev.adamko.kntoolchain.tools.utils

import java.io.File
import java.io.OutputStream.nullOutputStream
import java.math.BigInteger
import java.nio.file.Path
import java.security.DigestOutputStream
import java.security.MessageDigest
import kotlin.io.path.exists
import kotlin.io.path.inputStream

internal fun Path.md5ChecksumOrNull(): String? {
  return if (!exists()) null
  else md5Checksum()
}

internal fun Path.md5Checksum(): String =
  checksum("MD5")


internal fun File.sha512Checksum(): String =
  toPath().sha512Checksum()

internal fun Path.sha512Checksum(): String =
  checksum("SHA-512")

private fun Path.checksum(algorithm: String): String {
  val md = MessageDigest.getInstance(algorithm)
  DigestOutputStream(nullOutputStream(), md).use { os ->
    inputStream().use {
      it.transferTo(os)
    }
  }
  return BigInteger(1, md.digest()).toString(16)
    .padStart(md.digestLength * 2, '0')
}
