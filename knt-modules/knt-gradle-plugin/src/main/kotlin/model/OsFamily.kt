package dev.adamko.kntoolchain.model

import java.io.Serializable

/**
 * Operating system family of a kotlin-native-prebuilt distribution.
 *
 * @see KotlinNativePrebuiltDistributionSpec.osFamily
 */
sealed interface OsFamily : Serializable {
  val name: String

  data object Linux : OsFamily, Serializable {
    override val name: String = "linux"

    /** Required for configuration cache support. */
    @Suppress("unused")
    private fun readResolve(): Any = Linux
  }

  data object MacOs : OsFamily, Serializable {
    override val name: String = "macos"

    /** Required for configuration cache support. */
    @Suppress("unused")
    private fun readResolve(): Any = MacOs
  }

  data object Windows : OsFamily, Serializable {
    override val name: String = "windows"

    /** Required for configuration cache support. */
    @Suppress("unused")
    private fun readResolve(): Any = Windows
  }

  companion object {
    fun current(): OsFamily {
      val javaOsName = System.getProperty("os.name")
      return when {
        javaOsName == "Mac OS X"         -> MacOs
        javaOsName == "Linux"            -> Linux
        javaOsName.startsWith("Windows") -> Windows
        else                             -> throw IllegalStateException("Unknown operating system: $javaOsName")
      }
    }
  }
}
