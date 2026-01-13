package dev.adamko.kntoolchain.internal

import dev.adamko.kntoolchain.internal.KnpDependenciesCoordsSpec.Companion.knpDependenciesCoordsSpec
import dev.adamko.kntoolchain.model.KotlinNativePrebuiltDistributionSpec
import dev.adamko.kntoolchain.tools.datamodel.KonanDependenciesReport
import dev.adamko.kntoolchain.tools.datamodel.KotlinVersionTargetDependencies
import kotlinx.serialization.json.Json
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.provideDelegate

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
  ValueSource<Set<KnpDependenciesCoordsSpec.Coordinates>, KnpDependenciesCoordsSpec.Parameters> {

  internal interface Parameters : ValueSourceParameters {
    val osName: Property<String>
    val archName: Property<String>
    val kotlinVersion: Property<String>
  }

  internal data class Coordinates(
    val group: String,
    val module: String,
    val version: String,
    val extension: String,
    val classifier: String?,
    val artifact: String?,
  ) {
    override fun toString(): String = buildString {
      append(group)
      append(":")
      append(module)
      append(":")
      append(version)
      if (classifier != null) {
        append(":$classifier")
      }
      if (artifact != null) {
        append("[$artifact]")
      }
    }

    fun asDependencyNotation(): String =
      createDependencyNotation(
        group = group,
        name = module,
        version = version,
        classifier = classifier,
        extension = extension,
      )
  }

  override fun obtain(): Set<Coordinates> {
    val osName = parameters.osName.get()
    val archName = parameters.archName.get()
    val kotlinVersion = parameters.kotlinVersion.get()

    val coords = dependenciesCoordinates(
      kotlinVersion = kotlinVersion,
      hostFamily = osName,
      hostArch = archName,
    )

    val result = coords.map { coord ->
      Coordinates(
        group = coord.group,
        module = coord.module,
        version = coord.version,
        extension = coord.extension,
        classifier = coord.classifier,
        artifact = coord.artifact,
      )
    }

    val groupedResult = result.groupingBy { it }.eachCount()

    val duplicates = groupedResult.filterValues { it > 1 }.keys
    if (duplicates.isNotEmpty()) {
      logger.warn("KnpDependenciesCoordsSpec: Duplicate dependencies detected: $duplicates")
    }

    return groupedResult.keys
  }

  internal fun dependenciesCoordinates(
    kotlinVersion: String,
    hostFamily: String,
    hostArch: String,
  ): Set<KotlinVersionTargetDependencies.Coordinates> {
    val data = konanDependenciesReport.data
      .firstOrNull {
        it.dist.version == kotlinVersion
            && it.dist.hostFamily == hostFamily
            && it.dist.hostArch == hostArch
      }
      ?: error("No dependencies found for Kotlin version kotlinVersion:$kotlinVersion, hostFamily:$hostFamily, hostArch:$hostArch")

    return data.dependencyCoords.values.flatten().toSet()
  }

  companion object {

    private val logger: Logger = Logging.getLogger(KnpDependenciesCoordsSpec::class.java)

    private val konanDependenciesReportJson: String by lazy {
      Companion::class.java.getResourceAsStream("/dev/adamko/kn-toolchains/KonanDependenciesReport.json")!!
        .use { it.readAllBytes().decodeToString() }
    }

    private val konanDependenciesReport: KonanDependenciesReport by lazy {
      Json.decodeFromString(
        KonanDependenciesReport.serializer(),
        konanDependenciesReportJson,
      )
    }

    /**
     * See [KnpDependenciesCoordsSpec].
     */
    internal fun ProviderFactory.knpDependenciesCoordsSpec(
      knpSpec: KotlinNativePrebuiltDistributionSpec,
    ): Provider<Set<Coordinates>> {
      return of(KnpDependenciesCoordsSpec::class) { spec ->
        spec.parameters.osName.set(knpSpec.osFamily.map { it.id })
        spec.parameters.archName.set(knpSpec.architecture.map { it.id })
        spec.parameters.kotlinVersion.set(knpSpec.version)
      }
    }
  }
}
