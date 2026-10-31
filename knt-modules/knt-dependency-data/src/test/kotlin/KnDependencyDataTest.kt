package dev.adamko.kntoolchain.tools.data

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class KnDependencyDataTest {

  @TestFactory
  fun `generated data should be valid TOML`(): List<DynamicTest> {
    val tomlParser = TomlParser(TomlInputConfig())

    fun loadData(version: KnpVersion): String? {
      val resourceName = "knpdeps-${version.version}.toml"
      return KnDependencyDataSpec::class.java.getResourceAsStream(resourceName)
        ?.bufferedReader()
        ?.use { it.readText() }
    }

    return KnpVersion.allVersions.map { version ->
      DynamicTest.dynamicTest("version $version") {
        val data = loadData(version)
        data.shouldNotBeNull()
        tomlParser.parseString(data).children.shouldNotBeEmpty()
      }
    }
  }

  @Test
  fun `knDependencyData should contain data for every version`() {
    knDependencyData.map { it.version }.toSet() shouldBe KnpVersion.allVersions
  }

  @Test
  fun `knDependencyData entries should be unique`() {
    knDependencyData.map { it.run { "$version $buildPlatform $compileTarget" } }.shouldBeUnique()
  }

  @Test
  fun `knDependencyData entries should have dependencies`() {
    knDependencyData.forEach { it.dependencies.shouldNotBeEmpty() }
  }

  @Test
  fun `knDependencyData should contain artifacts`() {
    knDependencyData.flatMap { it.dependencies }.mapNotNull { it.artifact }.shouldNotBeEmpty()
  }

  @Test
  fun `knDependencyDataFor should only contain data for the requested version and build platform`() {
    KnpVersion.allVersions.forEach { version ->
      knDependencyDataFor(version).map { it.version }.toSet() shouldBe setOf(version)

      KnBuildPlatform.allPlatforms.forEach { buildPlatform ->
        knDependencyDataFor(version, buildPlatform).forEach {
          it.version shouldBe version
          it.buildPlatform shouldBe buildPlatform
        }
      }
    }
  }

  @Test
  fun `knDependencyDataFor should be empty for build platform without data`() {
    knDependencyDataFor(KnpVersion.V2_5_0_Beta2, KnBuildPlatform.MacOs.X86_64).shouldBeEmpty()
  }
}
