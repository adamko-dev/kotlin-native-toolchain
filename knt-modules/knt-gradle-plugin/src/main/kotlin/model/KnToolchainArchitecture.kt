package dev.adamko.kntoolchain.model

sealed interface KnToolchainArchitecture {

  val name: String

  data object AArch64 : KnToolchainArchitecture {
    override val name: String = "aarch64"
  }

  @Suppress("ClassName")
  data object X86_64 : KnToolchainArchitecture {
    override val name: String = "x86_64"
  }

  @JvmInline
  value class Custom(
    override val name: String
  ) : KnToolchainArchitecture
}
