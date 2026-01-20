package dev.adamko.kntoolchain.tools.datamodel.internal

import dev.adamko.kntoolchain.tools.datamodel.internal.KonanTargetData.Companion.encode
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * An encoded ID identifying a [KonanTarget].
 *
 * Note that the dependency containing [KonanTarget] is compile-only,
 * so it must be present at runtime to use [encode] and [decode].
 */
@Serializable
@JvmInline
value class KonanTargetData private constructor(
  val value: String,
) : Comparable<KonanTargetData> {

  /** [KonanTarget.name] */
  val name: String get() = value.substringBefore(":")
  /** [KonanTarget.family] */
  val family: String get() = value.substringAfter(":").substringBefore(":")
  /** [KonanTarget.architecture] */
  val architecture: String get() = value.substringAfterLast(":")

  override fun compareTo(other: KonanTargetData): Int =
    name.compareTo(other.name)

  fun decode(): KonanTarget? {
    return KonanTarget.predefinedTargets[name]
//    val family = enumValueOf<Family>(this.family)
//    val architecture = enumValueOf<Architecture>(this.architecture)
//
//    val result: KonanTarget? =
//      KonanTarget.predefinedTargets.values.firstOrNull { target ->
//        target.name == this.name
//            && target.family == family
//            && target.architecture == architecture
//      }
//
//    return requireNotNull(result) {
//      "Failed to decode KonanTargetTriplet($value) to KonanTarget."
//    }
  }

  companion object {
    fun encode(value: KonanTarget): KonanTargetData {
      return KonanTargetData(value.run { "${name}:${family}:${architecture}" })
//      return KonanTargetSpec(value.name)
    }
  }
}
