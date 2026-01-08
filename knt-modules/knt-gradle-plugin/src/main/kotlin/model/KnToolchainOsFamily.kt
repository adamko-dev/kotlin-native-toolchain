package dev.adamko.kntoolchain.model

sealed interface KnToolchainOsFamily {
  val name: String

  data object Linux : KnToolchainOsFamily {
    override val name: String = "linux"
  }

  data object MacOs : KnToolchainOsFamily {
    override val name: String = "macos"
  }

  data object Windows : KnToolchainOsFamily {
    override val name: String = "windows"
  }

  @JvmInline
  value class Custom(
    override val name: String,
  ) : KnToolchainOsFamily
}
