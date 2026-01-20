package dev.adamko.kntoolchain.model

import javax.inject.Inject
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance

/**
 * Kotlin/Native compilation target.
 */
abstract class KnTarget
@Inject
internal constructor(
  val konanTargetName: String,
//  private val name: String,
) : Named {
  override fun getName(): String = konanTargetName

  abstract val enabled: Property<Boolean>

  companion object {
    internal fun ObjectFactory.createKnTarget(
      konanTargetName: String,
//      name: String = konanTargetName
//        .split("[^A-Za-z0-9]".toRegex())
//        .joinToString("") { it.uppercaseFirstChar() },
    ): KnTarget {
      return newInstance<KnTarget>(
//        name,
        konanTargetName
      ).apply {
        enabled.convention(true)
      }
    }
  }
}
