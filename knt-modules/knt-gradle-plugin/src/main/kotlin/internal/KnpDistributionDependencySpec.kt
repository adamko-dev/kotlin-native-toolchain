package dev.adamko.kntoolchain.internal

import dev.adamko.kntoolchain.internal.KnpDistributionDependencySpec.Companion.kotlinNativePrebuiltToolchainDependencySpec
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.of

/**
 * Builds a GAV coordinate string for the KotlinNativePrebuilt distribution
 * for the specified OS, Arch, Kotlin version, etc.
 *
 * Use [KnpDistributionDependencySpec.Companion.kotlinNativePrebuiltToolchainDependencySpec]
 * to create a new instance.
 *
 * (A [ValueSource] is used just to make it easier to combine lots of [Provider]s.)
 */
internal abstract class KnpDistributionDependencySpec
internal constructor() :
  ValueSource<String, KnpDistributionDependencySpec.Parameters> {

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
     * See [KnpDistributionDependencySpec].
     */
    internal fun ProviderFactory.kotlinNativePrebuiltToolchainDependencySpec(
      group: Provider<String> = provider { "org.jetbrains.kotlin" },
      module: Provider<String> = provider { "kotlin-native-prebuilt" },
      osName: Provider<String>,
      archName: Provider<String>,
      kotlinVersion: Provider<String>,
      archiveExtension: Provider<String> = osName.map { if ("win" in it.lowercase()) "zip" else "tar.gz" },
    ): Provider<String> {
      return of(KnpDistributionDependencySpec::class) { spec ->
        spec.parameters.group.set(group)
        spec.parameters.module.set(module)
        spec.parameters.osName.set(osName)
        spec.parameters.archName.set(archName)
        spec.parameters.kotlinVersion.set(kotlinVersion)
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
