package dev.adamko.kntoolchain.test_utils.junit

import dev.adamko.kntoolchain.tools.data.KnBuildPlatform
import java.util.stream.Stream
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.ParameterDeclarations

class KnpOsArchArgs : ArgumentsProvider {
  override fun provideArguments(
    parameters: ParameterDeclarations,
    context: ExtensionContext,
  ): Stream<out Arguments> =
    KnBuildPlatform.allPlatforms.map {
      Arguments.of(it)
    }.stream()
}
