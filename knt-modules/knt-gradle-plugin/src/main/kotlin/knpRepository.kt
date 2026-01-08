package dev.adamko.kntoolchain

//import dev.adamko.kntoolchain.internal.KnpDistributionConfigurations.Companion.KNP_DIST_DEPENDENCY_USAGE
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.ivy

/**
 * Ivy repository for fetching Kotlin Native Prebuilt dependencies.
 */
context(buildscript: ExtensionAware)
fun RepositoryHandler.kotlinNativePrebuiltDependencies() {

//  val services = getRepositoryHandlerServices()

//  val url = services?.repoUrl() ?: "https://download.jetbrains.com"

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
//      services?.knpDistDependencyUsageAttribute()?.let { knpDistDependencyUsage ->
//        content.onlyForAttribute(Usage.USAGE_ATTRIBUTE, knpDistDependencyUsage)
//      }
    }
  }
}

private const val KN_NATIVE_PREBUILT_DEPS_REPO_NAME = "Kotlin Native Prebuilt Dependencies"
//private const val KN_NATIVE_PREBUILT_DEPS_REPO_URL_PROP = "dev.adamko.kntoolchain.kotlinNativePrebuiltDependencies.url"

//private fun RepositoryHandler.getRepositoryHandlerServices(): RepositoryHandlerServices? {
//  if (this !is ExtensionAware) return null
//  //extensions.create("RepositoryHandlerServices", RepositoryHandlerServices::class.java)
//  return extensions.findByType<RepositoryHandlerServices>()
//    ?: extensions.create("RepositoryHandlerServices", RepositoryHandlerServices::class.java)
//}
//
////val x : org.gradle.api.reflect.HasPublicType = TODO()
//internal abstract class RepositoryHandlerServices
//@Inject
//internal constructor(
//  val objects: ObjectFactory,
//  val providers: ProviderFactory,
//) {
//  fun repoUrl(): String? =
//    providers.gradleProperty(KN_NATIVE_PREBUILT_DEPS_REPO_URL_PROP).orNull
//
//  fun knpDistDependencyUsageAttribute(): Usage =
//    objects.named(KNP_DIST_DEPENDENCY_USAGE)
//}
