package dev.adamko.kntoolchain.tools.internal.utils

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal fun ProviderFactory.isCi(): Provider<Boolean> =
  environmentVariable("CI")
    .map(String::toBoolean)
    .orElse(false)

internal operator fun Provider<Boolean>.not(): Provider<Boolean> =
  map { !it }
