package dev.adamko.kntoolchain.tools.data

import dev.adamko.kntoolchain.tools.data.KnCompileTarget.OsFamily
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KnCompileTargetUtilsTest {

  @Test
  fun `OsFamily isLinux should only match Linux`() {
    OsFamily.entries.filter { it.isLinux } shouldContainExactlyInAnyOrder listOf(OsFamily.Linux)
  }

  @Test
  fun `OsFamily isWindows should only match Mingw`() {
    OsFamily.entries.filter { it.isWindows } shouldContainExactlyInAnyOrder listOf(OsFamily.Mingw)
  }

  @Test
  fun `OsFamily isAndroid should only match Android`() {
    OsFamily.entries.filter { it.isAndroid } shouldContainExactlyInAnyOrder listOf(OsFamily.Android)
  }

  @Test
  fun `OsFamily isApple should only match Apple OS families`() {
    OsFamily.entries.filter { it.isApple } shouldContainExactlyInAnyOrder listOf(
      OsFamily.OsX,
      OsFamily.IOs,
      OsFamily.WatchOs,
      OsFamily.TvOs,
    )
  }

  @Test
  fun `every OsFamily should be in exactly one group`() {
    OsFamily.entries.forEach { os ->
      val groups = listOf(os.isLinux, os.isWindows, os.isAndroid, os.isApple)
      withClue(os) {
        groups.count { it } shouldBe 1
      }
    }
  }

  @Test
  fun `KnCompileTarget helpers should match OsFamily helpers`() {
    KnCompileTarget.allTargets.forEach { target ->
      withClue(target) {
        target.isLinux shouldBe target.os.isLinux
        target.isWindows shouldBe target.os.isWindows
        target.isAndroid shouldBe target.os.isAndroid
        target.isApple shouldBe target.os.isApple
      }
    }
  }

  @Test
  fun `KnCompileTarget helpers should classify known targets`() {
    KnCompileTarget.allTargets.filter { it.isLinux } shouldContainExactlyInAnyOrder listOf(
      KnCompileTarget.LinuxX64,
      KnCompileTarget.LinuxArm32Hfp,
      KnCompileTarget.LinuxArm64,
    )

    KnCompileTarget.allTargets.filter { it.isWindows } shouldContainExactlyInAnyOrder listOf(
      KnCompileTarget.MingwX64,
    )

    KnCompileTarget.allTargets.filter { it.isAndroid } shouldContainExactlyInAnyOrder listOf(
      KnCompileTarget.AndroidX86,
      KnCompileTarget.AndroidX64,
      KnCompileTarget.AndroidArm32,
      KnCompileTarget.AndroidArm64,
    )

    KnCompileTarget.allTargets.filter { it.isApple } shouldContainExactlyInAnyOrder listOf(
      KnCompileTarget.MacOsX64,
      KnCompileTarget.MacOsArm64,
      KnCompileTarget.IOsArm64,
      KnCompileTarget.IOsX64,
      KnCompileTarget.IOsSimulatorArm64,
      KnCompileTarget.WatchOsArm32,
      KnCompileTarget.WatchOsArm64,
      KnCompileTarget.WatchOsX64,
      KnCompileTarget.WatchOsSimulatorArm64,
      KnCompileTarget.WatchOsDeviceArm64,
      KnCompileTarget.TvOsArm64,
      KnCompileTarget.TvOsX64,
      KnCompileTarget.TvOsSimulatorArm64,
    )
  }
}
