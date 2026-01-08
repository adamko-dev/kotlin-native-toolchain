package dev.adamko.kntoolchain

import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.PluginAware
import org.gradle.kotlin.dsl.apply

abstract class KnToolchainPlugin
@Inject
internal constructor() : Plugin<PluginAware> {

  override fun apply(target: PluginAware) {
    when (target) {
      is Project  -> target.pluginManager.apply(KnToolchainProjectPlugin::class)
      is Settings -> target.pluginManager.apply(KnToolchainSettingsPlugin::class)
      else        -> error("KnToolchainPlugin, unexpected target type: ${target::class}")
    }
  }

  companion object
}
