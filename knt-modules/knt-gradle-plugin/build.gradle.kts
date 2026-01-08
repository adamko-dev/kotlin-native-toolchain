@file:Suppress("UnstableApiUsage")

import ext.addKotlinGradlePluginOptions
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE

plugins {
  id("conventions.kotlin-gradle-plugin")
  id("conventions.maven-publishing")
  `java-test-fixtures`
}

dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())

  implementation("dev.adamko.kotlin-native-toolchain:knp-dependencies-data-model:1.0.0")

  // TODO try replacing coroutines with Java executors...
  implementation(platform(libs.kotlinxCoroutines.bom))
  implementation(libs.kotlinxCoroutines.core)

  implementation(libs.apache.commonsCompress)

  implementation(platform(libs.kotlinxSerialization.bom))
  implementation(libs.kotlinxSerialization.json)

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

val konanDependenciesReportDependencies: NamedDomainObjectProvider<DependencyScopeConfiguration> =
  configurations.dependencyScope("konanDependenciesReport") {
    defaultDependencies {
      add(project.dependencies.create("dev.adamko.kotlin-native-toolchain:knp-dependencies-data"))
    }
  }

val konanDependenciesReportResolver: NamedDomainObjectProvider<ResolvableConfiguration> =
  configurations.resolvable(konanDependenciesReportDependencies.name + "Resolver") {
    extendsFrom(konanDependenciesReportDependencies.get())
    attributes {
      attribute(USAGE_ATTRIBUTE, objects.named("konan-dependencies-report"))
    }
  }

val prepareKonanDependenciesReport by tasks.registering(Sync::class) {
  from(konanDependenciesReportResolver) {
    into("dev/adamko/kn-toolchains/")
  }
  into(layout.buildDirectory.dir("generated/resources/"))
}

kotlin.sourceSets.main {
  resources.srcDir(prepareKonanDependenciesReport)
}

dependencies {
  devPublication("dev.adamko.kotlin-native-toolchain:knp-dependencies-data-model:1.0.0")
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
        }
      }
    }
  }
}

tasks.check {
  dependsOn(testing.suites)
}
