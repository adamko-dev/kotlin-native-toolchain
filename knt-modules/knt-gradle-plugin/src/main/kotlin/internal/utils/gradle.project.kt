package dev.adamko.kntoolchain.internal.utils

import org.gradle.api.Project

@Suppress("UnstableApiUsage")
internal fun Project.isRootProject(): Boolean =
  isolated == isolated.rootProject
