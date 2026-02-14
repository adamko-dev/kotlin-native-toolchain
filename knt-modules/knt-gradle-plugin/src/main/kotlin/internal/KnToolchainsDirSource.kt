package dev.adamko.kntoolchain.internal

import dev.adamko.kntoolchain.internal.KnToolchainsDirSource.Companion.KN_TOOLCHAINS_DIR_ENV_VAR
import dev.adamko.kntoolchain.internal.KnToolchainsDirSource.Companion.knToolchainsDir
import dev.adamko.kntoolchain.internal.UserOsCacheDirSource.Companion.userCacheDirSource
import dev.adamko.kntoolchain.internal.utils.info
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.Path
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.of

/**
 * Get the location of the global kn-toolchains directory
 * that contains all knp distributions and their dependencies.
 *
 * Either uses the value of the [KN_TOOLCHAINS_DIR_ENV_VAR] environment variable,
 * or deduces a cache dir from the current operating system
 *
 * Use [KnToolchainsDirSource.Companion.knToolchainsDir] to get a new instance.
 */
// A ValueSource is used to simplify combining lots of providers.
internal abstract class KnToolchainsDirSource
@Inject
internal constructor() : ValueSource<Path, KnToolchainsDirSource.Parameters> {

  interface Parameters : ValueSourceParameters {
    /**
     * Optional user defined directory.
     *
     * Source: environment variable `KN_TOOLCHAINS_DIR`
     */
    val knToolchainsDir: Property<String>

    /**
     * The OS-specific cache directory for the current user.
     */
    val userCacheDir: DirectoryProperty
  }

  override fun obtain(): Path {
    val knToolchainsDir = parameters.knToolchainsDir.orNull

    val value = if (knToolchainsDir != null) {
      Path(knToolchainsDir)
    } else {
      val userCacheDir = parameters.userCacheDir.get().asFile.toPath()
      userCacheDir.resolve(KN_TOOLCHAINS_DIR_NAME)
    }

    logger.info { "KnToolchainsDirSource: $value" }

    return value
  }

  companion object {
    private const val KN_TOOLCHAINS_DIR_ENV_VAR = "KN_TOOLCHAINS_DIR"
    private const val KN_TOOLCHAINS_DIR_NAME = "kn-toolchains"

    private val logger: Logger = Logging.getLogger(KnToolchainsDirSource::class.java)

    fun ProviderFactory.knToolchainsDir(): Provider<Path> {
      return of(KnToolchainsDirSource::class) { spec ->
        spec.parameters.knToolchainsDir.set(environmentVariable(KN_TOOLCHAINS_DIR_ENV_VAR))
        spec.parameters.userCacheDir.fileProvider(userCacheDirSource().map(Path::toFile))
      }
    }
  }
}
