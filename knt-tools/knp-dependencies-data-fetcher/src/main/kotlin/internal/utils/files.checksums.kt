package dev.adamko.kntoolchain.tools.internal.utils

import java.io.OutputStream.nullOutputStream
import java.math.BigInteger
import java.nio.file.Path
import java.security.DigestOutputStream
import java.security.MessageDigest
import kotlin.io.path.*

internal fun Path.md5ChecksumOrNull(): String? {
  return if (!exists()) null
  else md5Checksum()
}

internal fun Path.md5Checksum(): String =
  checksum(this, "MD5")


//internal fun File.sha512Checksum(): String =
//  toPath().sha512Checksum()


//internal fun Path.sha512Checksum(): String {
//  return when {
//    isRegularFile() || isDirectory() ->
//      checksum(this, "SHA-512")
//
//    else                             ->
//      error("Cannot checksum $this: it is neither a file nor a directory")
//  }
//}

private fun checksum(path: Path, algorithm: String): String {
  val md = MessageDigest.getInstance(algorithm)

  DigestOutputStream(nullOutputStream(), md).use { os ->

    when {
      path.isRegularFile() ->
        path.inputStream().use {
          it.transferTo(os)
        }

      path.isDirectory()   ->
        path.walk()
          .filter { it.isRegularFile() }
          .forEach { f -> f.inputStream().use { it.transferTo(os) } }

      else                 ->
        error("Cannot checksum $path: it is neither a file nor a directory")
    }
  }

  return BigInteger(1, md.digest()).toString(16)
    .padStart(md.digestLength * 2, '0')
}
