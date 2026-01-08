package dev.adamko.kntoolchain.internal

import org.gradle.api.Project

@Suppress("UnstableApiUsage")
internal fun Project.isRootProject(): Boolean =
  isolated == isolated.rootProject
