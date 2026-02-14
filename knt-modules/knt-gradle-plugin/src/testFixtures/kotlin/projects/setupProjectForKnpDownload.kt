package dev.adamko.kntoolchain.test_utils.projects

import dev.adamko.kntoolchain.test_utils.GradleTestContext
import dev.adamko.kntoolchain.test_utils.kntGradlePluginProjectVersion
import dev.adamko.kntoolchain.tools.data.KnBuildPlatform
import dev.adamko.kntoolchain.tools.data.KnpVersion
import kotlin.io.path.invariantSeparatorsPathString

fun GradleTestContext.setupProjectForKnpDownload(
  knpVersion: KnpVersion = KnpVersion.V2_3_0,
  buildPlatform: KnBuildPlatform = KnBuildPlatform.MacOs.X86_64,
) {
  val dummyRepo = projectDir.resolve("dummy-repo")
  createMockKnpMavenRepo(dummyRepo)

  settingsGradleKts += """
        |plugins {
        |  id("dev.adamko.kotlin-native-toolchain") version "$kntGradlePluginProjectVersion"
        |}
        |
        |dependencyResolutionManagement.repositories
        |  .withType<IvyArtifactRepository>()
        |  .matching { it.name == "Kotlin Native Prebuilt Dependencies" }
        |  .configureEach { 
        |    setUrl("${dummyRepo.toUri()}")
        |  }
        |  
        |knToolchain {
        |  baseInstallDir = file("${konanDataDir.invariantSeparatorsPathString}")
        |}
        |""".trimMargin()

  settingsGradleKts.modify { content ->
    content.replace(
      "mavenCentral()",
      """
      val dummyRepo =
        maven(file("${dummyRepo.invariantSeparatorsPathString}")) {
          name = "DummyKnpRepo"
        }
      exclusiveContent {
        forRepositories(dummyRepo)
        filter { includeModule("org.jetbrains.kotlin", "kotlin-native-prebuilt") }
      }
      mavenCentral()
      """.trimIndent()
    )
  }

  buildGradleKts += """
        |import kotlin.io.path.*
        |import dev.adamko.kntoolchain.tools.data.*
        |
        |plugins {
        |  id("dev.adamko.kotlin-native-toolchain")
        |}
        |
        |knToolchain {
        |  kotlinNativePrebuiltDistribution {
        |    version = KnpVersion.${knpVersion.name}
        |    buildPlatform = KnBuildPlatform.${buildPlatform.family.name}.${buildPlatform.arch.name}
        |    compileTargets = KnCompileTarget.allTargets
        |  }
        |}
        |""".trimMargin()
}
