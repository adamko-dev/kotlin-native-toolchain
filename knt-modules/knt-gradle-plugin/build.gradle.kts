@file:Suppress("UnstableApiUsage")

import ext.addKotlinGradlePluginOptions

plugins {
  id("conventions.kotlin-gradle-plugin")
  id("conventions.maven-publishing")
  `java-test-fixtures`
  //id("dev.adamko.knp.KnpDataGenPlugin")
}

//val knpDependenciesDataModelCoords: Provider<String> = gitVersion.map { version ->
//  "dev.adamko.kotlin-native-toolchain:knp-dependencies-data-model:$version"
//}

dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())

  implementation(projects.kntModules.kntDependencyData)
//  implementation(knpDependenciesDataModelCoords)

  // TODO try replacing coroutines with Java executors...
  implementation(platform(libs.kotlinxCoroutines.bom))
  implementation(libs.kotlinxCoroutines.core)

  implementation(libs.apache.commonsCompress)

//  implementation(platform(libs.kotlinxSerialization.bom))
//  implementation(libs.kotlinxSerialization.json)

  //compileOnly(libs.gradlePlugin.kotlin)
  compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")

  testFixturesCompileOnly(gradleTestKit())
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

    val testIntegration by registering(JvmTestSuite::class) {
      dependencies {
        implementation(gradleTestKit())


        implementation(projects.kntModules.kntDependencyData)

        implementation(libs.apache.commonsCompress)
      }
      targets.configureEach {
        testTask.configure {
          val devMavenRepo = devPublish.devMavenRepo
//          inputs.dir(devMavenRepo)
//            .withPropertyName("devMavenRepo")
//            .withPathSensitivity(PathSensitivity.RELATIVE)
//            .withNormalizer(ClasspathNormalizer::class)
//            .ignoreEmptyDirectories()
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
  }
}

tasks.check {
  dependsOn(testing.suites)
}
