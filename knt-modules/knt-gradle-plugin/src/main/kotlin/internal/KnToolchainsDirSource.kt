package dev.adamko.kntoolchain.internal

import dev.adamko.kntoolchain.internal.KnToolchainsDirSource.Companion.KN_TOOLCHAINS_DIR
import dev.adamko.kntoolchain.internal.KnToolchainsDirSource.Companion.knToolchainsDir
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.Path
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.of

/**
 * Get the location of the global kn-toolchains directory.
 *
 * Either uses the value of the [KN_TOOLCHAINS_DIR] environment variable,
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

    /** `systemProperty("os.name")` */
    val osName: Property<String>

    /** `systemProperty("user.home")` */
    val homeDir: Property<String>

    /** `environmentVariable("APPDATA")` */
    val appDataDir: Property<String>
  }

  override fun obtain(): Path {
    val knToolchainsDir = parameters.knToolchainsDir.orNull

    val value = if (knToolchainsDir != null) {
      Path(knToolchainsDir)
    } else {
      val cacheDirName = "kn-toolchains"
      Path(userCacheDir()).resolve(cacheDirName)
    }

    logger.info { "KnToolchainsDirSource: $value" }

    return value
  }

  private fun userCacheDir(): String {
    val osName = parameters.osName.get().lowercase()

    val homeDir = parameters.homeDir.get()
    val appDataDir = parameters.appDataDir.orNull ?: homeDir

    return when {
      "win" in osName -> "$appDataDir/Caches/"
      "mac" in osName -> "$homeDir/Library/Caches/"
      "nix" in osName -> "$homeDir/.cache/"
      else            -> "$homeDir/.cache/"
    }
  }

  companion object {
    private const val KN_TOOLCHAINS_DIR = "KN_TOOLCHAINS_DIR"

    private val logger: Logger = Logging.getLogger(KnToolchainsDirSource::class.java)

    fun ProviderFactory.knToolchainsDir(): Provider<Path> {
      return of(KnToolchainsDirSource::class) { spec ->
        spec.parameters.knToolchainsDir.set(environmentVariable(KN_TOOLCHAINS_DIR))
        spec.parameters.osName.set(systemProperty("os.name"))
        spec.parameters.homeDir.set(systemProperty("user.home"))
        spec.parameters.appDataDir.set(environmentVariable("APPDATA"))
      }
    }
  }
}
