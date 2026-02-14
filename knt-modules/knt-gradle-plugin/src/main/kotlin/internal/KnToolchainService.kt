package dev.adamko.kntoolchain.internal

import dev.adamko.kntoolchain.model.KotlinNativePrebuiltDistributionSpec
import javax.inject.Inject
import org.gradle.api.provider.ListProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

internal abstract class KnToolchainService
@Inject
internal constructor() : BuildService<KnToolchainService.Parameters> {

  internal interface Parameters : BuildServiceParameters

  /**
   * KNP dists requested from all subprojects.
   */
  abstract val requestedKnpDists: ListProperty<KotlinNativePrebuiltDistributionSpec>

  companion object {
    const val SERVICE_NAME = "knToolchainService"
  }
}
