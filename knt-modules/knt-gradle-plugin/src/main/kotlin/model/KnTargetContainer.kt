package dev.adamko.kntoolchain.model

import dev.adamko.kntoolchain.model.KnTarget.Companion.createKnTarget
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.domainObjectContainer

abstract class KnTargetContainer
@Inject internal constructor(
  private val objects: ObjectFactory
) {
  private val container: NamedDomainObjectContainer<KnTarget> =
    objects.domainObjectContainer(KnTarget::class) { konanTargetName ->
      objects.createKnTarget(konanTargetName = konanTargetName)
    }

  fun named(name: String): NamedDomainObjectProvider<KnTarget> =
    container.named(name)

  fun configureEach(action: Action<KnTarget>) {
    container.configureEach(action)
  }

  internal fun register(konanTargetName: String): NamedDomainObjectProvider<KnTarget> {
    val value = container.register(konanTargetName)
//    extensions.add(konanTargetName, value)
    return value
  }

  private val extensions: ExtensionContainer
    get() = (this as ExtensionAware).extensions
}
