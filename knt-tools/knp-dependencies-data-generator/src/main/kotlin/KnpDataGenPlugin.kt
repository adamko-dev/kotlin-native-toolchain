package dev.adamko.knp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

abstract class KnpDataGenPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.tasks.register("knpDataGen", KnpDataGenTask::class) { task ->
      task.outputDir.convention(
        project.layout.buildDirectory.dir("generated/knp-dependencies-data")
      )
    }
  }
}
