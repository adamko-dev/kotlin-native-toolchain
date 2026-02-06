package dev.adamko.kntoolchain.tools.data

/**
 * A version of a kotlin-native-prebuilt distribution.
 */
data class KnpVersion private constructor(
  val name: String,
  val version: String,
): java.io.Serializable {

  companion object {
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
        V2_3_20_Beta2,
      )
    }

    /** Version `2.0.0`. */
    val V2_0_0: KnpVersion =
      KnpVersion(
        name = "V2_0_0",
        version = "2.0.0",
      )


    /** Version `2.0.10`. */
    val V2_0_10: KnpVersion =
      KnpVersion(
        name = "V2_0_10",
        version = "2.0.10",
      )


    /** Version `2.0.20`. */
    val V2_0_20: KnpVersion =
      KnpVersion(
        name = "V2_0_20",
        version = "2.0.20",
      )


    /** Version `2.0.21`. */
    val V2_0_21: KnpVersion =
      KnpVersion(
        name = "V2_0_21",
        version = "2.0.21",
      )


    /** Version `2.1.0`. */
    val V2_1_0: KnpVersion =
      KnpVersion(
        name = "V2_1_0",
        version = "2.1.0",
      )


    /** Version `2.1.10`. */
    val V2_1_10: KnpVersion =
      KnpVersion(
        name = "V2_1_10",
        version = "2.1.10",
      )


    /** Version `2.1.20`. */
    val V2_1_20: KnpVersion =
      KnpVersion(
        name = "V2_1_20",
        version = "2.1.20",
      )


    /** Version `2.1.21`. */
    val V2_1_21: KnpVersion =
      KnpVersion(
        name = "V2_1_21",
        version = "2.1.21",
      )


    /** Version `2.2.0`. */
    val V2_2_0: KnpVersion =
      KnpVersion(
        name = "V2_2_0",
        version = "2.2.0",
      )


    /** Version `2.2.10`. */
    val V2_2_10: KnpVersion =
      KnpVersion(
        name = "V2_2_10",
        version = "2.2.10",
      )


    /** Version `2.2.20`. */
    val V2_2_20: KnpVersion =
      KnpVersion(
        name = "V2_2_20",
        version = "2.2.20",
      )


    /** Version `2.2.21`. */
    val V2_2_21: KnpVersion =
      KnpVersion(
        name = "V2_2_21",
        version = "2.2.21",
      )


    /** Version `2.3.0`. */
    val V2_3_0: KnpVersion =
      KnpVersion(
        name = "V2_3_0",
        version = "2.3.0",
      )


    /** Version `2.3.10`. */
    val V2_3_10: KnpVersion =
      KnpVersion(
        name = "V2_3_10",
        version = "2.3.10",
      )


    /** Version `2.3.20-Beta2`. */
    val V2_3_20_Beta2: KnpVersion =
      KnpVersion(
        name = "V2_3_20_Beta2",
        version = "2.3.20-Beta2",
      )

  }
}
