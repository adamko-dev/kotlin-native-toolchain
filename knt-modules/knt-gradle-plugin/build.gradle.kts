@file:Suppress("UnstableApiUsage")

import ext.addKotlinGradlePluginOptions

plugins {
  id("conventions.kotlin-gradle-plugin")
  id("conventions.maven-publishing")
  `java-test-fixtures`
  kotlin("plugin.assignment")
}

dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())

  implementation(projects.kntModules.kntDependencyData)

  implementation(libs.apache.commonsCompress)

  compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")

  testFixturesImplementation(platform(libs.junit.bom))
  testFixturesImplementation(libs.junit.jupiter)
  testFixturesCompileOnly(gradleTestKit())
  testFixturesCompileOnly(libs.apache.commonsCompress)
  testFixturesCompileOnly(projects.kntModules.kntDependencyData)
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
        }
      }
    }
  }
}

tasks.check {
  dependsOn(testing.suites)
}

assignment {
  annotation("dev.adamko.kntoolchain.test_utils.KotlinAssignmentOverloadTarget")
}
