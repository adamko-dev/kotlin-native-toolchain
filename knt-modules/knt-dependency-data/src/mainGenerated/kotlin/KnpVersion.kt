package dev.adamko.kntoolchain.tools.data

import java.io.Serializable as JavaIoSerializable

/**
 * A version of a kotlin-native-prebuilt distribution.
 */
data class KnpVersion internal constructor(
  val name: String,
  val version: String,
  private val major: Int,
  private val minor: Int,
  private val patch: Int,
  private val stage: Stage,
  private val stageRevision: Int,
): JavaIoSerializable, Comparable<KnpVersion> {

  override fun compareTo(other: KnpVersion): Int =
    compareValuesBy(
      this,
      other,
      KnpVersion::major,
      KnpVersion::minor,
      KnpVersion::patch,
      KnpVersion::stage,
      KnpVersion::stageRevision,
    )

  internal enum class Stage {
    Beta,
    RC,
    Release,
  }

  companion object {

    /**
     * The latest stable release version of a kotlin-native-prebuilt distribution.
     */
    val latestRelease: KnpVersion by lazy {
      allVersions.filter { it.stage == Release }.max()
    }

    /**
     * All supported kotlin-native-prebuilt distribution versions.
     */
    val allVersions: Set<KnpVersion> by lazy {
      setOf(
        V2_0_0,
        V2_0_10,
        V2_0_20,
        V2_0_21,
        V2_1_0,
        V2_1_10,
        V2_1_20,
        V2_1_21,
        V2_2_0,
        V2_2_10,
        V2_2_20,
        V2_2_21,
        V2_3_0,
        V2_3_10,
        V2_3_20,
        V2_3_21,
        V2_4_0,
        V2_4_10,
        V2_4_20,
        V2_4_21,
        V2_5_0_Beta2,
      )
    }

    /** Version `2.0.0`. */
    val V2_0_0: KnpVersion =
      KnpVersion(
        name = "V2_0_0",
        version = "2.0.0",
        major = 2,
        minor = 0,
        patch = 0,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.0.10`. */
    val V2_0_10: KnpVersion =
      KnpVersion(
        name = "V2_0_10",
        version = "2.0.10",
        major = 2,
        minor = 0,
        patch = 10,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.0.20`. */
    val V2_0_20: KnpVersion =
      KnpVersion(
        name = "V2_0_20",
        version = "2.0.20",
        major = 2,
        minor = 0,
        patch = 20,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.0.21`. */
    val V2_0_21: KnpVersion =
      KnpVersion(
        name = "V2_0_21",
        version = "2.0.21",
        major = 2,
        minor = 0,
        patch = 21,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.1.0`. */
    val V2_1_0: KnpVersion =
      KnpVersion(
        name = "V2_1_0",
        version = "2.1.0",
        major = 2,
        minor = 1,
        patch = 0,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.1.10`. */
    val V2_1_10: KnpVersion =
      KnpVersion(
        name = "V2_1_10",
        version = "2.1.10",
        major = 2,
        minor = 1,
        patch = 10,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.1.20`. */
    val V2_1_20: KnpVersion =
      KnpVersion(
        name = "V2_1_20",
        version = "2.1.20",
        major = 2,
        minor = 1,
        patch = 20,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.1.21`. */
    val V2_1_21: KnpVersion =
      KnpVersion(
        name = "V2_1_21",
        version = "2.1.21",
        major = 2,
        minor = 1,
        patch = 21,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.2.0`. */
    val V2_2_0: KnpVersion =
      KnpVersion(
        name = "V2_2_0",
        version = "2.2.0",
        major = 2,
        minor = 2,
        patch = 0,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.2.10`. */
    val V2_2_10: KnpVersion =
      KnpVersion(
        name = "V2_2_10",
        version = "2.2.10",
        major = 2,
        minor = 2,
        patch = 10,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.2.20`. */
    val V2_2_20: KnpVersion =
      KnpVersion(
        name = "V2_2_20",
        version = "2.2.20",
        major = 2,
        minor = 2,
        patch = 20,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.2.21`. */
    val V2_2_21: KnpVersion =
      KnpVersion(
        name = "V2_2_21",
        version = "2.2.21",
        major = 2,
        minor = 2,
        patch = 21,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.3.0`. */
    val V2_3_0: KnpVersion =
      KnpVersion(
        name = "V2_3_0",
        version = "2.3.0",
        major = 2,
        minor = 3,
        patch = 0,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.3.10`. */
    val V2_3_10: KnpVersion =
      KnpVersion(
        name = "V2_3_10",
        version = "2.3.10",
        major = 2,
        minor = 3,
        patch = 10,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.3.20`. */
    val V2_3_20: KnpVersion =
      KnpVersion(
        name = "V2_3_20",
        version = "2.3.20",
        major = 2,
        minor = 3,
        patch = 20,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.3.21`. */
    val V2_3_21: KnpVersion =
      KnpVersion(
        name = "V2_3_21",
        version = "2.3.21",
        major = 2,
        minor = 3,
        patch = 21,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.4.0`. */
    val V2_4_0: KnpVersion =
      KnpVersion(
        name = "V2_4_0",
        version = "2.4.0",
        major = 2,
        minor = 4,
        patch = 0,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.4.10`. */
    val V2_4_10: KnpVersion =
      KnpVersion(
        name = "V2_4_10",
        version = "2.4.10",
        major = 2,
        minor = 4,
        patch = 10,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.4.20`. */
    val V2_4_20: KnpVersion =
      KnpVersion(
        name = "V2_4_20",
        version = "2.4.20",
        major = 2,
        minor = 4,
        patch = 20,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.4.21`. */
    val V2_4_21: KnpVersion =
      KnpVersion(
        name = "V2_4_21",
        version = "2.4.21",
        major = 2,
        minor = 4,
        patch = 21,
        stage = Release,
        stageRevision = 0,
      )

    /** Version `2.5.0-Beta2`. */
    val V2_5_0_Beta2: KnpVersion =
      KnpVersion(
        name = "V2_5_0_Beta2",
        version = "2.5.0-Beta2",
        major = 2,
        minor = 5,
        patch = 0,
        stage = Beta,
        stageRevision = 2,
      )
  }
}
