package dev.adamko.kntoolchain.internal

import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.Path
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.of

/**
 * Get the OS-specific cache directory for the current user.
 */
internal abstract class UserOsCacheDirSource
@Inject
internal constructor() : ValueSource<Path, UserOsCacheDirSource.Parameters> {

  interface Parameters : ValueSourceParameters {
    /** `systemProperty("os.name")` */
    val osName: Property<String>

    /** `systemProperty("user.home")` */
    val homeDir: Property<String>

    /** `environmentVariable("APPDATA")` */
    val appDataDir: Property<String>
  }

  override fun obtain(): Path {
    return Path(userCacheDir())
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
    fun ProviderFactory.userCacheDirSource(): Provider<Path> =
      of(UserOsCacheDirSource::class) { spec ->
        spec.parameters.osName.set(systemProperty("os.name"))
        spec.parameters.homeDir.set(systemProperty("user.home"))
        spec.parameters.appDataDir.set(environmentVariable("APPDATA"))
      }
  }
}
