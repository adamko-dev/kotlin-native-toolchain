package dev.adamko.kntoolchain.tools.data

import dev.adamko.kntoolchain.tools.data.internal.parseKnDependencyData
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KnDependencyDataParserTest {

  @Test
  fun `single-line arrays should be parsed`() {
    val parsed = parseKnDependencyData(
      source = "test.toml",
      content = """
        |format = 1
        |kotlinVersion = "2.3.0"
        |
        |[linux-x86_64]
        |android_arm32 = [ "kotlin.native:a:1@tar.gz", "kotlin.native:b:2@tar.gz" ]
        |android_arm64 = { sameAs = "android_arm32" }
        |linux_x64 = ["kotlin.native:c:3@tar.gz",]
      """.trimMargin(),
    )

    parsed.dependencies shouldBe mapOf(
      KnBuildPlatform.Linux.X86_64 to mapOf(
        KnCompileTarget.AndroidArm32 to setOf(
          KnpDependency("kotlin.native:a:1@tar.gz"),
          KnpDependency("kotlin.native:b:2@tar.gz"),
        ),
        KnCompileTarget.AndroidArm64 to setOf(
          KnpDependency("kotlin.native:a:1@tar.gz"),
          KnpDependency("kotlin.native:b:2@tar.gz"),
        ),
        KnCompileTarget.LinuxX64 to setOf(
          KnpDependency("kotlin.native:c:3@tar.gz"),
        ),
      ),
    )
  }

  @Test
  fun `sameAs should resolve targets declared before and after it`() {
    fun parse(buildPlatformTable: String) =
      parseKnDependencyData(
        source = "test.toml",
        content = """
          |format = 1
          |kotlinVersion = "2.3.0"
          |
          |[linux-x86_64]
          |$buildPlatformTable
        """.trimMargin(),
      ).dependencies

    val sameAsEarlierTarget = parse(
      """
        |android_arm32 = [ "kotlin.native:a:1@tar.gz" ]
        |android_arm64 = { sameAs = "android_arm32" }
      """.trimMargin()
    )
    val sameAsLaterTarget = parse(
      """
        |android_arm64 = { sameAs = "android_arm32" }
        |android_arm32 = [ "kotlin.native:a:1@tar.gz" ]
      """.trimMargin()
    )

    val expected = mapOf(
      KnBuildPlatform.Linux.X86_64 to mapOf(
        KnCompileTarget.AndroidArm32 to setOf(KnpDependency("kotlin.native:a:1@tar.gz")),
        KnCompileTarget.AndroidArm64 to setOf(KnpDependency("kotlin.native:a:1@tar.gz")),
      ),
    )
    sameAsEarlierTarget shouldBe expected
    sameAsLaterTarget shouldBe expected
  }

  @Test
  fun `invalid syntax should fail with the line number`() {
    val failure = shouldThrow<IllegalStateException> {
      parseKnDependencyData(
        source = "test.toml",
        content = """
          |format = 1
          |kotlinVersion = "2.3.0"
          |
          |[linux-x86_64]
          |android_arm32 = [ "kotlin.native:a:1@tar.gz" "kotlin.native:b:2@tar.gz" ]
        """.trimMargin(),
      )
    }

    failure.message shouldBe
        """Invalid test.toml line 5: expected ']'. Line: 'android_arm32 = [ "kotlin.native:a:1@tar.gz" "kotlin.native:b:2@tar.gz" ]'"""
  }
}
