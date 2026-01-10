package dev.adamko.kntoolchain.model

import java.io.Serializable

/**
 * Architecture of a kotlin-native-prebuilt distribution.
 *
 * @see KotlinNativePrebuiltDistributionSpec.architecture
 */
sealed interface Architecture : Serializable {

  val name: String

  data object AArch64 : Architecture, Serializable {
    override val name: String = "aarch64"

    /** Required for configuration cache support. */
    @Suppress("unused")
    private fun readResolve(): Any = AArch64
  }

  @Suppress("ClassName")
  data object X86_64 : Architecture, Serializable {
    override val name: String = "x86_64"

    /** Required for configuration cache support. */
    @Suppress("unused")
    private fun readResolve(): Any = X86_64
  }

  companion object {
    fun current(): Architecture {
      return when (val osArch = System.getProperty("os.arch")) {
        "x86_64",
        "amd64"   -> X86_64

        "arm64",
        "aarch64" -> AArch64

        else      -> throw IllegalStateException("Unknown hardware platform: $osArch")
      }
    }
  }
}
