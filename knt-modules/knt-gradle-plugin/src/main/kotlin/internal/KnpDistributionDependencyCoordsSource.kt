package dev.adamko.kntoolchain.internal

import dev.adamko.kntoolchain.internal.KnpDistributionDependencyCoordsSource.Companion.kotlinNativePrebuiltToolchainDependencySpec
import dev.adamko.kntoolchain.internal.utils.createDependencyNotation
import dev.adamko.kntoolchain.tools.data.KnBuildPlatform
import dev.adamko.kntoolchain.tools.data.KnpVersion
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.of

/**
 * Builds a GAV coordinate string for the KotlinNativePrebuilt distribution
 * for the specified OS, Arch, Kotlin version, etc.
 *
 * Use [KnpDistributionDependencyCoordsSource.Companion.kotlinNativePrebuiltToolchainDependencySpec]
 * to create a new instance.
 *
 * (A [ValueSource] is used just to make it easier to combine lots of [Provider]s.)
 */
internal abstract class KnpDistributionDependencyCoordsSource
internal constructor() :
  ValueSource<String, KnpDistributionDependencyCoordsSource.Parameters> {

  internal interface Parameters : ValueSourceParameters {
    val group: Property<String>
    val module: Property<String>
    val osName: Property<String>
    val archName: Property<String>
    val kotlinVersion: Property<String>
    val archiveExtension: Property<String>
  }

  override fun obtain(): String {
    val group by parameters.group.getOrThrow()
    val module by parameters.module.getOrThrow()
    val osName by parameters.osName.getOrThrow()
    val archName by parameters.archName.getOrThrow()
    val kotlinVersion by parameters.kotlinVersion.getOrThrow()
    val archiveExtension by parameters.archiveExtension.getOrThrow()
    return createDependencyNotation(
      group = group,
      name = module,
      version = kotlinVersion,
      classifier = "${osName}-${archName}",
      extension = archiveExtension,
    )
  }

  companion object {
    /**
     * See [KnpDistributionDependencyCoordsSource].
     */
    internal fun ProviderFactory.kotlinNativePrebuiltToolchainDependencySpec(
//      osFamily: Provider<OsFamily>,
//      architecture: Provider<Architecture>,
      buildPlatform: Provider<KnBuildPlatform>,
      version: Provider<KnpVersion>,
      group: Provider<String> = provider { "org.jetbrains.kotlin" },
      module: Provider<String> = provider { "kotlin-native-prebuilt" },
      archiveExtension: Provider<String> =
        buildPlatform.map { platform ->
          if (platform.family == KnBuildPlatform.OsFamily.Windows)
            "zip" else "tar.gz"
        }
    ): Provider<String> {
      return of(KnpDistributionDependencyCoordsSource::class) { spec ->
        spec.parameters.group.set(group)
        spec.parameters.module.set(module)
        spec.parameters.osName.set(buildPlatform.map { it.family.value })
        spec.parameters.archName.set(buildPlatform.map { it.arch.value })
//        spec.parameters.archName.set(architecture.map { it.id })
        spec.parameters.kotlinVersion.set(version.map { it.value })
        spec.parameters.archiveExtension.set(archiveExtension)
      }
    }

    private fun <T : Any> Provider<T>.getOrThrow(): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, T>> {
      return PropertyDelegateProvider { _: Any?, _: KProperty<*> ->
        ReadOnlyProperty { _: Any?, property: KProperty<*> ->
          this@getOrThrow.orNull
            ?: error("KotlinNativePrebuiltToolchainDependencySpec: Missing parameter '${property.name}'")
        }
      }
    }
  }
}
