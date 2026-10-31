@file:Suppress("UnstableApiUsage")

import ext.addKotlinGradlePluginOptions

plugins {
  id("conventions.kotlin-gradle-plugin")
  id("conventions.maven-publishing")
  `java-test-fixtures`
}

dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())

  implementation(projects.kntModules.kntDependencyData)

  implementation(libs.apache.commonsCompress)

  compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")

  testFixturesCompileOnly(gradleTestKit())
  testFixturesCompileOnly(libs.apache.commonsCompress)
}

kotlin {
  compilerOptions {
    addKotlinGradlePluginOptions()
  }
}

gradlePlugin {
  plugins.register("kotlinNativeToolchain") {
    id = "dev.adamko.kotlin-native-toolchain"
    implementationClass = "dev.adamko.kntoolchain.KnToolchainPlugin"
  }
}

dependencies {
  devPublication(projects.kntModules.kntDependencyData)
  devPublication(project())
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      dependencies {
        implementation(platform(libs.junit.bom))
        implementation(libs.junit.jupiter)
        runtimeOnly(libs.junit.platformLauncher)

        implementation(libs.kotest.assertions)

        implementation(testFixtures(project(project.path)))
      }

      targets.configureEach {
        testTask.configure {
          // Used as a read-only dependency cache by TestKit builds.
          val hostGradleUserHome = gradle.gradleUserHomeDir
          jvmArgumentProviders.add {
            listOf("-DhostGradleUserHome=${hostGradleUserHome.invariantSeparatorsPath}")
          }
        }
      }
    }

    val test by getting(JvmTestSuite::class) {
      dependencies {
        implementation(libs.apache.commonsCompress)
      }
    }

    val testIntegration by registering(JvmTestSuite::class) {
      dependencies {
        implementation(gradleTestKit())

        implementation(projects.kntModules.kntDependencyData)

        implementation(libs.apache.commonsCompress)
      }
      targets.configureEach {
        testTask.configure {
          val devMavenRepo = devPublish.devMavenRepo
          dependsOn(tasks.updateDevRepo)
          jvmArgumentProviders.add {
            listOf(
              "-DdevMavenRepo=${devMavenRepo.get().asFile.invariantSeparatorsPath}"
            )
          }

          val projectVersion = providers.provider { project.version.toString() }
          jvmArgumentProviders.add {
            listOf("-DkntGradlePluginProjectVersion=${projectVersion.get()}")
          }
        }
      }
    }

    val testExamples by registering(JvmTestSuite::class) {

      dependencies {
        implementation(gradleTestKit())
      }

      targets.configureEach {
        testTask.configure {
          val devMavenRepo = devPublish.devMavenRepo
          dependsOn(tasks.updateDevRepo)
          jvmArgumentProviders.add {
            listOf(
              "-DdevMavenRepo=${devMavenRepo.get().asFile.invariantSeparatorsPath}"
            )
          }

          val examplesDir = layout.settingsDirectory.dir("examples")
          inputs.dir(examplesDir)
            .withPathSensitivity(PathSensitivity.RELATIVE)
            .ignoreEmptyDirectories()
          jvmArgumentProviders.add {
            listOf(
              "-DexamplesDir=${examplesDir.asFile.invariantSeparatorsPath}"
            )
          }

          val projectVersion = providers.provider { project.version.toString() }
          jvmArgumentProviders.add {
            listOf("-DkntGradlePluginProjectVersion=${projectVersion.get()}")
          }

          environment("KONAN_USE_INTERNAL_SERVER", "0")
        }
      }
    }
  }
}

tasks.check {
  dependsOn(testing.suites)
}
