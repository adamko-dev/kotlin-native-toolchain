package ext

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

/**
 * Add compiler arguments for building Gradle plugins.
 *
 * https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin_compiler_arguments
 */
fun KotlinJvmCompilerOptions.addKotlinGradlePluginOptions() {
  freeCompilerArgs.addAll(
    "-java-parameters",
    //"-Xjvm-default=all",
    "-Xsam-conversions=class",
    "-Xjsr305=strict",
    "-Xjspecify-annotations=strict",
  )
}
