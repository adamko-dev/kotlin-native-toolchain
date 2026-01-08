package dev.adamko.kntoolchain.operations

import dev.adamko.kntoolchain.internal.CACHE_DIR_TAG_FILENAME
import dev.adamko.kntoolchain.internal.checksumFilesMetadata
import dev.adamko.kntoolchain.internal.nameWithoutArchiveExtension
import dev.adamko.kntoolchain.internal.walkFiltered
import dev.adamko.kntoolchain.model.KnToolchainSpec
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.time.measureTimedValue
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

internal sealed class BaseKnToolchainsOperation<R : Any> : ValueSource<R, BaseKnToolchainsOperation.Parameters> {

  interface Parameters : ValueSourceParameters {
    val baseInstallDir: DirectoryProperty
    val checksumsDir: DirectoryProperty
    val installSpecs: ListProperty<KnToolchainSpec>
  }

  protected val baseInstallDir: Path by lazy { parameters.baseInstallDir.get().asFile.toPath() }
  protected val checksumsDir: Path by lazy { parameters.checksumsDir.get().asFile.toPath() }

  /**
   * Get all installation specs for all install specs.
   */
  protected fun getInstallSpecs(): Set<DependencyInstallSpec> =
    parameters.installSpecs.get()
      .flatMap { it.getInstallSpecs() }
      .distinctBy { it.archive }
      .toSet()

  /**
   * Get all installation specs for a distribution.
   *
   * There will be one spec for the actual distribution
   * and multiple for each dependency that the distribution depends on.
   */
  protected fun KnToolchainSpec.getInstallSpecs(): List<DependencyInstallSpec> {
    val knpDistArchive: Path = knpDistArchive()
    val knpDependencyDists: FileCollection = knpDependencyDists()

    val konanDependenciesDir = this@BaseKnToolchainsOperation.baseInstallDir.resolve("dependencies")

    return buildList {
      add(
        DependencyInstallSpec.from(
          archive = knpDistArchive,
          installDir = this@BaseKnToolchainsOperation.baseInstallDir.resolve(knpDistArchive.nameWithoutArchiveExtension()),
        )
      )

      addAll(
        knpDependencyDists
          .map { it.toPath() }
          .map { archive ->
            DependencyInstallSpec.from(
              archive = archive,
              installDir = konanDependenciesDir.resolve(archive.nameWithoutArchiveExtension())
            )
          }
      )
    }
  }

  protected fun computeDirChecksum(
    destinationDir: Path,
    excludes: Set<String>,
  ): String? {
    if (!destinationDir.exists()) {
      logger.warn("Could not compute snapshot for $destinationDir - directory does not exist")
      return null
    }
    if (!destinationDir.isDirectory()) {
      logger.warn("Could not compute snapshot for $destinationDir - not a directory")
      return null
    }
    if (destinationDir.listDirectoryEntries().isEmpty()) {
      logger.warn("Could not compute snapshot for $destinationDir - install directory is empty")
      return null
    }

    val (hash, time) = measureTimedValue {
      destinationDir
        .walkFiltered(excludes = excludes)
        .checksumFilesMetadata(
          baseDir = destinationDir,
          additionalMetadata = buildList {
            add("excludes:")
            addAll(excludes.sorted())
          },
        )
    }

    logger.info("Computed dir checksum for $destinationDir in $time - $hash")

    return hash
  }

  /**
   * Information pertaining to the installation of a single KNP dependency.
   *
   * A KNP dependency can be the actual KNP distribution,
   * or a dependency of the KNP distribution (e.g. clang).
   */
  protected data class DependencyInstallSpec private constructor(
    /** The source archive. */
    val archive: Path,

    /** The local installation directory. */
    val installDir: Path,

    /** The checksum file for the source [archive]. */
    val archiveChecksumFile: Path,

    /** The checksum file of [installDir]. */
    val installDirChecksumFile: Path,

    val installFileExcludes: Set<String>,
  ) {
    /**
     * Cache directory tag file.
     *
     * See https://bford.info/cachedir/
     */
    val installDirCacheTagFile: Path = installDir.resolve(CACHE_DIR_TAG_FILENAME)

    companion object {

      /**
       * Create a new [DependencyInstallSpec].
       */
      context(base: BaseKnToolchainsOperation<*>)
      fun from(
        archive: Path,
        installDir: Path,
      ): DependencyInstallSpec {

        val archiveChecksumFileName = "${archive.name}.hash"
        val installDirChecksumFileName =
          "${installDir.relativeTo(base.baseInstallDir).joinToString("_") { it.name }}.hash"

        return DependencyInstallSpec(
          archive = archive,
          installDir = installDir,
          archiveChecksumFile =
            base.checksumsDir.resolve(archiveChecksumFileName),
          installDirChecksumFile =
            base.checksumsDir.resolve(installDirChecksumFileName),
          installFileExcludes = base.parameters.installSpecs.get().first().installFileExcludes(),
        )
      }
    }
  }

  companion object {
    private val logger: Logger = Logging.getLogger(BaseKnToolchainsOperation::class.java)

    internal fun KnToolchainSpec.knpDistArchive(): Path =
      sourceArchive.get().asFile.toPath()

    internal fun KnToolchainSpec.knpDependencyDists(): FileCollection =
      sourceDependencies

    internal fun KnToolchainSpec.installFileExcludes(): Set<String> =
      installFileExcludes.get()
  }
}
