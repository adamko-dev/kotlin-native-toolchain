package dev.adamko.kntoolchain.model

import java.io.Serializable

/**
 * Operating system family of a kotlin-native-prebuilt distribution.
 *
 * @see KotlinNativePrebuiltDistributionSpec.osFamily
 */
@Deprecated("moved to data")
sealed class OsFamily : Serializable {
  abstract val name: String
  val id: String get() = name.lowercase()

  data object Linux : OsFamily(), Serializable {
    override val name: String = "Linux"

    /** Required for configuration cache support. */
    @Suppress("unused")
    private fun readResolve(): Any = Linux
  }

  data object MacOs : OsFamily(), Serializable {
    override val name: String = "MacOs"

    /** Required for configuration cache support. */
    @Suppress("unused")
    private fun readResolve(): Any = MacOs
  }

  data object Windows : OsFamily(), Serializable {
    override val name: String = "Windows"

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
