package dev.adamko.kntoolchain.tools.internal.datamodel

import dev.adamko.kntoolchain.tools.internal.datamodel.KonanTargetData.Companion.encode
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
internal value class KonanTargetData private constructor(
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
  }

  companion object {
    fun encode(value: KonanTarget): KonanTargetData {
      return KonanTargetData(value.run { "${name}:${family}:${architecture}" })
    }
  }
}
