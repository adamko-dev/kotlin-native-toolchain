@file:Suppress("UnstableApiUsage")

package dev.adamko.kntoolchain.tasks

import dev.adamko.kntoolchain.internal.utils.GroupTestEventReporterContext
import dev.adamko.kntoolchain.internal.utils.start
import dev.adamko.kntoolchain.model.DependencyInstallReport
import dev.adamko.kntoolchain.model.KotlinNativePrebuiltDistributionSpec
import dev.adamko.kntoolchain.operations.CreateKnToolchainsStatusReport.Companion.createKnToolchainsStatusReport
import javax.inject.Inject
import kotlin.io.path.name
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.testing.TestEventReporterFactory

@CacheableTask
abstract class CheckKnToolchainIntegrityTask
@Inject
internal constructor(
  private val objects: ObjectFactory,
  private val providers: ProviderFactory,
  private val fs: FileSystemOperations,
  private val testEventReporter: TestEventReporterFactory,
) : DefaultTask() {

  /**
   * The directory to write HTML test reports to.
   */
  @get:OutputDirectory
  abstract val reportDirectory: DirectoryProperty

  /**
   * The directory to write binary test results to.
   */
  @get:OutputDirectory
  abstract val binaryResultsDirectory: DirectoryProperty

  @get:InputFiles
  // Use @InputFiles because this is optional. @InputDirectory requires the directory exists.
  @get:PathSensitive(RELATIVE)
  @get:IgnoreEmptyDirectories
  abstract val baseInstallDir: DirectoryProperty

  @get:InputFiles
  // Use @InputFiles because this is optional. @InputDirectory requires the directory exists.
  @get:PathSensitive(RELATIVE)
  @get:IgnoreEmptyDirectories
  abstract val checksumsDir: DirectoryProperty

  /**
   * [Internal] - tracked by [knpDistsSourceFiles].
   */
  @get:Internal
  internal abstract val knpDists: ListProperty<KotlinNativePrebuiltDistributionSpec>

  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  protected val knpDistsSourceFiles: Provider<FileCollection>
    get() =
      knpDists.map { specs ->
        specs.fold(objects.fileCollection()) { acc, tcSpec ->
          val files = objects.fileCollection()
            .from(tcSpec.sourceArchive)
            .from(tcSpec.sourceDependencies)
            .asFileTree
            .matching { filter ->
              filter.exclude(tcSpec.installFileExcludes.get())
            }
          acc.from(files)
        }
      }

  private val tmpBinaryResultsDir: Provider<Directory> =
    objects.directoryProperty()
      .fileValue(temporaryDir.resolve("binary-results"))

  private val tmpReportDir: Provider<Directory> =
    objects.directoryProperty()
      .fileValue(temporaryDir.resolve("report"))

  @TaskAction
  protected fun action() {
    prepareTmpReportDirs()

    try {
      runAllTests()
    } finally {
      syncTestReports()
    }
  }

  private fun prepareTmpReportDirs() {
    fs.delete { spec ->
      spec.delete(tmpBinaryResultsDir)
      spec.delete(tmpReportDir)
    }
  }

  private fun runAllTests() {
    // https://docs.gradle.org/current/userguide/test_reporting_api.html#test_reporting_api
    testEventReporter.createTestEventReporter(
      TEST_EVENT_ROOT_GROUP_NAME,
      tmpBinaryResultsDir.get(),
      tmpReportDir.get(),
      true
    ).start { root ->
      executeChecksumTests(root)

      // TODO could also check if executables can run?
      //      e.g. run `konan.sh --version` and `clang --version`
    }
  }

  private fun executeChecksumTests(root: GroupTestEventReporterContext) {

    val report = providers.createKnToolchainsStatusReport(
      installSpecs = knpDists.get(),
      konanDataDir = baseInstallDir,
      checksumsDir = checksumsDir,
    )

    root.reportGroup("verify checksums").use { group ->
      val report = report.orNull

      report?.dependencies?.forEach { dependency ->
        group.reportTest(dependency.installDir.name, dependency.installDir.name) { test ->
          when (dependency.status) {
            is DependencyInstallReport.DependencyStatus.Invalid -> {
              test.failed(
                dependency.status.reason,
                dependency.status.details,
              )
            }

            DependencyInstallReport.DependencyStatus.Valid      -> {
              test.succeeded()
            }
          }
        }
      }
    }
  }

  private fun syncTestReports() {
    fs.sync { spec ->
      spec.from(tmpReportDir)
      spec.into(reportDirectory)
    }

    fs.sync { spec ->
      spec.from(tmpBinaryResultsDir)
      spec.into(binaryResultsDirectory)
    }
  }

  companion object {
    internal const val TEST_EVENT_ROOT_GROUP_NAME = "KnToolchainIntegrity"
  }
}
