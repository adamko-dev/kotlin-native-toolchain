package dev.adamko.kntoolchain.tools.data

import io.github.z4kn4fein.semver.toVersion
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KnpVersionTest {

  @Test
  fun `latestRelease should be the highest stable version`() {
    val expected = KnpVersion.allVersions
      .filter { it.version.toVersion().isStable }
      .maxBy { it.version.toVersion() }

    KnpVersion.latestRelease shouldBe expected
  }

  @Test
  fun `allVersions should be ordered by semantic version`() {
    val expected = KnpVersion.allVersions
      .sortedBy { it.version.toVersion() }
      .map { it.version }

    KnpVersion.allVersions.sorted().map { it.version } shouldContainExactly expected
  }

  @Test
  fun `pre-release stages should be ordered before the release`() {
    val release = KnpVersion.latestRelease
    val beta = release.copy(stage = KnpVersion.Stage.Beta, stageRevision = 1)
    val rc = release.copy(stage = KnpVersion.Stage.RC, stageRevision = 1)

    listOf(release, rc, beta).sorted() shouldContainExactly listOf(beta, rc, release)
  }

  @Test
  fun `name should be derived from version`() {
    KnpVersion.allVersions.forEach { knpVersion ->
      val expected = "V" + knpVersion.version.replace(Regex("[^A-Za-z0-9]"), "_")
      knpVersion.name shouldBe expected
    }
  }
}
