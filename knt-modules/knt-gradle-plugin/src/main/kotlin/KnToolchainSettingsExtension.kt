package dev.adamko.kntoolchain

import javax.inject.Inject

abstract class KnToolchainSettingsExtension
@Inject
internal constructor() {

  companion object {
    const val EXTENSION_NAME = "knToolchains"
  }
}
