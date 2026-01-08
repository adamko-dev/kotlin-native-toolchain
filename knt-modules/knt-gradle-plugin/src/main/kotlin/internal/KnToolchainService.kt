package dev.adamko.kntoolchain.internal

import dev.adamko.kntoolchain.model.KnToolchainSpec
import javax.inject.Inject
import org.gradle.api.provider.ListProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

internal abstract class KnToolchainService
@Inject internal constructor() : BuildService<KnToolchainService.Parameters> {

  internal interface Parameters : BuildServiceParameters

  abstract val requestedKnToolchainSpecs: ListProperty<KnToolchainSpec>

  companion object {
    const val SERVICE_NAME = "knToolchainService"
  }
}
