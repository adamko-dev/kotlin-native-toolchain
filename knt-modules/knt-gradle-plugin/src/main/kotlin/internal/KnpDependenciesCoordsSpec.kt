package dev.adamko.kntoolchain.internal

import dev.adamko.kntoolchain.internal.KnpDependenciesCoordsSpec.Companion.knpDependenciesCoordsSpec
import dev.adamko.kntoolchain.model.KotlinNativePrebuiltDistributionSpec
import dev.adamko.kntoolchain.tools.data.*
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.of

/**
 * Get all KNP dependencies for the given properties.
 *
 * Builds a GAV coordinate string for the KotlinNativePrebuilt distribution
 * for the specified OS, Arch, Kotlin version, etc.
 *
 * Use [KnpDependenciesCoordsSpec.Companion.knpDependenciesCoordsSpec]
 * to create a new instance.
 *
 * A [org.gradle.api.provider.ValueSource] is used just to make it easier to combine lots of [Provider]s.
 */
internal abstract class KnpDependenciesCoordsSpec
internal constructor() :
  ValueSource<Set<KnpDependency>, KnpDependenciesCoordsSpec.Parameters> {

  internal interface Parameters : ValueSourceParameters {
    /** The platform on which the compilation tools are executed. */
    val buildPlatform: Property<KnBuildPlatform>
    /** This is the platform that the compiler will generate code for. */
    val compileTargets: SetProperty<KnCompileTarget>
    val kotlinVersion: Property<KnpVersion>
  }

//  internal data class Coordinates(
//    val group: String,
//    val module: String,
//    val version: String,
//    val extension: String,
//    val classifier: String?,
////    val coords: String,
//    val artifact: String?,
//  ) {
//    override fun toString(): String = buildString {
//      append(group)
//      append(":")
//      append(module)
//      append(":")
//      append(version)
//      if (classifier != null) {
//        append(":$classifier")
//      }
//      if (artifact != null) {
//        append("[$artifact]")
//      }
//    }
//
//    fun asDependencyNotation(): String =
//      createDependencyNotation(
//        group = group,
//        name = module,
//        version = version,
//        classifier = classifier,
//        extension = extension,
//      )
//  }

  override fun obtain(): Set<KnpDependency> {

    val coords = dependenciesCoordinates()

//    val result = coords.map { coord ->
//      Coordinates(
//        group = coord.group,
//        module = coord.module,
//        version = coord.version,
//        extension = coord.extension,
//        classifier = coord.classifier,
//        artifact = coord.artifact,
//      )
//    }

    val groupedResult = coords.groupingBy { it }.eachCount()

    val duplicates = groupedResult.filterValues { it > 1 }.keys
    if (duplicates.isNotEmpty()) {
      logger.warn("KnpDependenciesCoordsSpec: Duplicate dependencies detected: $duplicates")
    }

    return groupedResult.keys
  }

  internal fun dependenciesCoordinates(): Set<KnpDependency> {
    val requestedTargets = parameters.compileTargets.get()

    val data = knDependencyData.filter { data ->
      data.version == parameters.kotlinVersion.get()
          && data.compileTarget in requestedTargets
          && data.buildPlatform == parameters.buildPlatform.get()
    }

    require(data.isNotEmpty()) {
      "No dependency data found for Kotlin version:${parameters.kotlinVersion.get()} and targets:${requestedTargets} and buildPlatform:${parameters.buildPlatform.get()}"
    }

    return data.flatMap { it.dependencies }.toSet()

//    val version: String = parameters.kotlinVersion.get()
//    //val targetPlatform = parameters.targetPlatform.get()
//    val buildPlatform = parameters.buildPlatform.get()
//
//    val data = konanDependenciesReport.data
//      .firstOrNull { deps ->
//        deps.version == version
////            && it.dist.hostFamily == hostFamily
//            && deps.buildPlatform == buildPlatform
//      }
////    val data = konanDependenciesReport(version).data
////      .firstOrNull { deps ->
////        deps.version == version
//////            && deps.targetPlatform == targetPlatform
////            && deps.buildPlatform == buildPlatform
////      }
//      ?: error("No dependencies found for Kotlin version:$version, buildPlatform:$buildPlatform")
//
//    return data.dependencies.values.flatten().toSet()
  }

  companion object {

    private val logger: Logger = Logging.getLogger(KnpDependenciesCoordsSpec::class.java)

    /**
     * See [KnpDependenciesCoordsSpec].
     */
    internal fun ProviderFactory.knpDependenciesCoordsSpec(
      knpSpec: KotlinNativePrebuiltDistributionSpec,
    ): Provider<Set<KnpDependency>> {
      return of(KnpDependenciesCoordsSpec::class) { spec ->
        spec.parameters.buildPlatform.set(knpSpec.buildPlatform)
        spec.parameters.compileTargets.set(knpSpec.compileTargets)
        spec.parameters.kotlinVersion.set(knpSpec.version)
//        spec.parameters.buildPlatform.set(
//          knpSpec.osFamily.zip(knpSpec.architecture) { osFamily, arch -> Platform(osFamily.id, arch.id) }
//        )
//        spec.parameters.targetPlatform.set(
//          knpSpec.osFamily.zip(knpSpec.architecture) { osFamily, arch -> Platform(osFamily.id, arch.id) }
//        )
////        spec.parameters.osName.set(knpSpec.osFamily.map { it.id })
////        spec.parameters.archName.set(knpSpec.architecture.map { it.id })
//        spec.parameters.kotlinVersion.set(knpSpec.version)
      }
    }
  }
}
