package dev.adamko.kntoolchain.tools.datamodel

import java.util.function.IntFunction
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class KonanDependenciesReport(
  private val data: List<KotlinVersionTargetDependencies>,
) : List<KotlinVersionTargetDependencies> by data {
  @Deprecated("Deprecated in Java")
  override fun <T : Any> toArray(generator: IntFunction<Array<out T>>): Array<out T> {
    @Suppress("DEPRECATION")
    return super.toArray(generator)
  }
}
