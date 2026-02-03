package dev.adamko.kntoolchain.tools.datamodel

import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.konan.target.KonanTarget

/**
 * An encoded ID identifying a [org.jetbrains.kotlin.konan.target.KonanTarget].
 *
 * Note that the dependency containing [org.jetbrains.kotlin.konan.target.KonanTarget] is compile-only,
 * so it must be present at runtime to use [encode] and [decode].
 */
@Serializable
@JvmInline
value class KonanTargetData private constructor(
  val value: String,
) : Comparable<KonanTargetData> {

  /** [org.jetbrains.kotlin.konan.target.KonanTarget.name] */
  val name: String get() = value.substringBefore(":")
  /** [org.jetbrains.kotlin.konan.target.KonanTarget.family] */
  val family: String get() = value.substringAfter(":").substringBefore(":")
  /** [org.jetbrains.kotlin.konan.target.KonanTarget.architecture] */
  val architecture: String get() = value.substringAfterLast(":")

  override fun compareTo(other: KonanTargetData): Int =
    name.compareTo(other.name)

  fun decode(): KonanTarget? {
    return KonanTarget.predefinedTargets[name]
  }

  companion object {
    fun encode(value: KonanTarget): KonanTargetData {
      return KonanTargetData(value.run { "${name}:${family}:${architecture}" })
    }
  }
}
