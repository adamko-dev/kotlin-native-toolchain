package conventions

plugins {
  id("conventions.kotlin-jvm")
  `java-gradle-plugin`
  id("conventions.maven-publishing")
  id("com.gradle.plugin-publish")
}

tasks.validatePlugins {
  enableStricterValidation = true
}

gradlePlugin {
  isAutomatedPublishing = true
}

kotlin {
  compilerOptions {
    // Add compiler arguments for building Gradle plugins.
    // https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin_compiler_arguments
    freeCompilerArgs.addAll(
      "-java-parameters",
      //"-Xjvm-default=all",
      "-Xsam-conversions=class",
      "-Xjsr305=strict",
      "-Xjspecify-annotations=strict",
    )
  }
}


dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())
  compileOnly(kotlin("stdlib"))
}


//sourceSets {
//  configureEach {
//    java.setSrcDirs(emptyList<File>())
//  }
//}

// kotlin-stdlib should be excluded for Gradle plugins because it will be provided at runtime by Gradle.
configurations
  .matching {
    it.isCanBeDeclared
        && !it.isCanBeResolved
        && !it.isCanBeConsumed
        && it.name != JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME
        && it.name in setOf(
      JavaPlugin.API_CONFIGURATION_NAME,
      JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
      JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME,
      JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME,
    )
  }
  .configureEach {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
  }
