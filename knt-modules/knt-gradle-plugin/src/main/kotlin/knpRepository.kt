package dev.adamko.kntoolchain

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.ivy

/**
 * Ivy repository for fetching Kotlin Native Prebuilt dependencies.
 */
fun RepositoryHandler.kotlinNativePrebuiltDependencies() {
  ivy("https://download.jetbrains.com") {
    name = KN_NATIVE_PREBUILT_DEPS_REPO_NAME
    patternLayout { pattern ->
      pattern.artifact("[orgPath]/[module]-[revision](-[classifier])(.[ext])")
      pattern.artifact("[orgPath](/[module])/[artifact]-[revision](-[classifier])(.[ext])")
    }
    metadataSources { metadata ->
      metadata.artifact()
    }
    content { content ->
      content.includeGroupAndSubgroups("kotlin.native")
    }
  }
}

private const val KN_NATIVE_PREBUILT_DEPS_REPO_NAME = "Kotlin Native Prebuilt Dependencies"
