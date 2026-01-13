package conventions

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("conventions.base")
  kotlin("jvm")
}

kotlin {
  jvmToolchain {
    languageVersion.set(compilerOptions.jvmTarget.map { JavaLanguageVersion.of(it.target) })
  }

  compilerOptions {
    jvmTarget = JvmTarget.JVM_17

    optIn.add("kotlin.io.path.ExperimentalPathApi")
    optIn.add("kotlin.time.ExperimentalTime")
    optIn.add("kotlin.ExperimentalStdlibApi")

    freeCompilerArgs.addAll(
      "-Xconsistent-data-class-copy-visibility",
      "-Xcontext-parameters",
      "-Xwhen-guards",
      "-Xnon-local-break-continue",
      "-Xmulti-dollar-interpolation",
      "-Xnested-type-aliases",
      "-Xcontext-sensitive-resolution",
    )

    freeCompilerArgs.add(jvmTarget.map { "-Xjdk-release=${it.target}" })
  }
}

java {
  toolchain {
    languageVersion = kotlin.compilerOptions.jvmTarget.map { JavaLanguageVersion.of(it.target) }
  }
  withSourcesJar()
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  systemProperty("junit.jupiter.tempdir.cleanup.mode.default", "ON_SUCCESS")
}

sourceSets.configureEach {
  java.setSrcDirs(emptyList<String>())
}
