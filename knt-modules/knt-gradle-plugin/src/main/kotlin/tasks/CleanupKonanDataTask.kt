package dev.adamko.kntoolchain.tasks

import dev.adamko.kntoolchain.internal.utils.CACHE_DIR_TAG_FILENAME
import dev.adamko.kntoolchain.internal.utils.walk
import dev.adamko.kntoolchain.model.DependencyInstallReport
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
abstract class CleanupKonanDataTask
internal constructor() : DefaultTask() {

  @get:Input
  internal abstract val report: Property<DependencyInstallReport>

  @get:Input
  abstract val inactivityLimit: Property<Duration>

  @TaskAction
  protected fun action() {
    val report = report.get()
    val inactivityLimit = inactivityLimit.get()

    val inactiveDependencies =
      report.dependencies.filter { dependency ->
        val cacheDirTag = dependency.installDir.resolve(CACHE_DIR_TAG_FILENAME)
        val actualInactivity = cacheDirTag.inactiveFor()
        actualInactivity > inactivityLimit
      }.toSet()

    if (inactiveDependencies.isNotEmpty()) {
      logger.warn(buildString {
        appendLine("Found inactive dependencies:")
        inactiveDependencies.forEach { appendLine(" - ${it.archiveName}, ${it.installDir}") }
      })
    }

    val allTaggedDirs: Set<Path> =
      report.konanDataDir
        .walk(maxDepth = 10)
        .filter { it.isRegularFile() }
        .filter { it.name == CACHE_DIR_TAG_FILENAME }
        .filter { cacheDirTag ->
          val actualInactivity = cacheDirTag.inactiveFor()
          actualInactivity > inactivityLimit
        }
        .map { it.parent }
        .toSet()

    val knownInstallDirs =
      report.dependencies.map { it.installDir }.toSet()

    val unknownInstallDirs = allTaggedDirs - knownInstallDirs

    logger.warn(
      buildString {
        appendLine("Found unknown expired install dirs:")
        unknownInstallDirs.forEach { appendLine(" - $it") }
      }
    )

    // TODO Okay, so now we know the unused dirs.
    //      Next: how to clean them up?
    //      Just delete them, or report them, or request confirmation?
  }

  companion object {
    private fun Path.inactiveFor(): Duration {
      return Duration.between(
        getLastModifiedTime().toInstant(),
        Instant.now(),
      )
    }
  }
}
