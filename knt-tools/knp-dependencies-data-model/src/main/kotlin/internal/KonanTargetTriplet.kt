package dev.adamko.kntoolchain.tools.datamodel.internal

import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * An encoded triplet of [org.jetbrains.kotlin.konan.target.KonanTarget].
 *
 * The artifact providing [org.jetbrains.kotlin.konan.target.KonanTarget]
 * is compile-only.
 */
@Serializable
@JvmInline
value class KonanTargetTriplet internal constructor(
  val value: String
) {

  fun decode(): KonanTarget {
    val (name, familyString, architectureString) =
      value.split(":", limit = 3)

    val family = enumValueOf<Family>(familyString)
    val architecture = enumValueOf<Architecture>(architectureString)

    val result =
      KonanTarget::class.sealedSubclasses
        .mapNotNull { it.objectInstance }
        .firstOrNull {
          it.name == name
              && it.family == family
              && it.architecture == architecture
        }

    return requireNotNull(result) {
      "Failed to decode KonanTargetTriplet($value) to KonanTarget."
    }
  }

  companion object {
    fun encode(value: KonanTarget): KonanTargetTriplet {
      return KonanTargetTriplet(value.run { "${name}:${family}:${architecture}" })
    }
  }
}
