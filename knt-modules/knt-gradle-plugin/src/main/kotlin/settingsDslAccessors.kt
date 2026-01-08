package dev.adamko.kntoolchain

import org.gradle.api.Action
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

val Settings.knToolchain: KnToolchainSettingsExtension
  get() = extensions.getByType(KnToolchainSettingsExtension::class)

fun Settings.knToolchain(action: Action<KnToolchainSettingsExtension>): Unit =
  extensions.configure(KnToolchainSettingsExtension::class, action)
