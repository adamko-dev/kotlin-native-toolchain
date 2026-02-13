package conventions

import ext.credentialsAction
import ext.skipPublishingTestFixtures

plugins {
  id("conventions.base")
  `maven-publish`
  id("dev.adamko.dev-publish")
  signing
  id("com.gradleup.nmcp")
}

//region POM convention
publishing {
  publications.withType<MavenPublication>().configureEach {
    pom {
      name.convention("Kotlin Native Toolchain")
      description.convention("Gradle plugin, for cache-friendly 'kotlin-native-prebuilt' provisioning. Installs required tools for compiling C/C++ code to be used by Kotlin/Native.")
      url.convention("https://github.com/adamko-dev/kotlin-native-toolchain")

      scm {
        connection.convention("scm:git:https://github.com/adamko-dev/kotlin-native-toolchain")
        developerConnection.convention("scm:git:https://github.com/adamko-dev/kotlin-native-toolchain")
        url.convention("https://github.com/adamko-dev/kotlin-native-toolchain")
      }

      licenses {
        license {
          name.convention("Apache-2.0")
          url.convention("https://www.apache.org/licenses/LICENSE-2.0.txt")
        }
      }

      developers {
        developer {
          email.set("adam@adamko.dev")
        }
      }
    }
  }
}
//endregion

publishing {
  publications.withType<MavenPublication>().configureEach {
    versionMapping {
      usage("java-api") {
        fromResolutionOf("runtimeClasspath")
      }
      usage("java-runtime") {
        fromResolutionResult()
      }
    }
  }
}

pluginManager.withPlugin("java-test-fixtures") {
  skipPublishingTestFixtures()
}

val sonatypeRepositoryCredentials: Provider<Action<PasswordCredentials>> =
  providers.credentialsAction("sonatypeRepository")

val projectVersion: Provider<String> = provider { project.version.toString() }


//region Signing
val signingKeyId: Provider<String> =
  providers.gradleProperty("dev.adamko.kxstsgen.signing.keyId")
    .orElse(providers.environmentVariable("MAVEN_SONATYPE_SIGNING_KEY_ID"))
val signingKey: Provider<String> =
  providers.gradleProperty("dev.adamko.kxstsgen.signing.key")
    .orElse(providers.environmentVariable("MAVEN_SONATYPE_SIGNING_KEY"))
val signingPassword: Provider<String> =
  providers.gradleProperty("dev.adamko.kxstsgen.signing.password")
    .orElse(providers.environmentVariable("MAVEN_SONATYPE_SIGNING_PASSWORD"))

signing {
  val keyId = signingKeyId.orNull
  val key = signingKey.orNull
  val password = signingPassword.orNull

  val signingCredentialsPresent =
    !keyId.isNullOrBlank() && !key.isNullOrBlank() && !password.isNullOrBlank()

  if (signingCredentialsPresent) {
    logger.info("maven-publishing.gradle.kts enabled signing for ${project.displayName}")
    useInMemoryPgpKeys(keyId, key, password)
  }

  setRequired({
    signingCredentialsPresent || gradle.taskGraph.allTasks
      .filterIsInstance<PublishToMavenRepository>()
      .any { task ->
        task.repository.name in setOf(
          "SonatypeRelease",
        )
      }
  })
}

afterEvaluate {
  // Register signatures afterEvaluate, otherwise the signing plugin creates the signing tasks
  // too early, before all the publications are added.
  signing.sign(publishing.publications)
}
//endregion


//region Javadoc JAR stub
// use creating, not registering, because the signing plugin doesn't accept task providers
val javadocJarStub by tasks.registering(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Stub javadoc.jar artifact (required by Maven Central)"
  archiveClassifier.set("javadoc")
}
//endregion


plugins.withType<JavaPlatformPlugin>().configureEach {
//  val javadocJarStub = javadocStubTask()
  publishing.publications.create<MavenPublication>("mavenJavaPlatform") {
    from(components["javaPlatform"])
    //artifact(javadocJarStub)
  }
}


//region Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://youtrack.jetbrains.com/issue/KT-46466 https://github.com/gradle/gradle/issues/26091
tasks.withType<AbstractPublishToMaven>().configureEach {
  val signingTasks = tasks.withType<Sign>()
  mustRunAfter(signingTasks)
}
//endregion


//region publishing logging
tasks.withType<AbstractPublishToMaven>().configureEach {
  group = project.name
  val publicationGAV = provider { publication?.run { "$groupId:$artifactId:$version" } }
  inputs.property("publicationGAV", publicationGAV).optional(true)
  doLast("log publication GAV") {
    if (publicationGAV.isPresent) {
      logger.lifecycle("[${path}] ${publicationGAV.get()}")
    }
  }
}
//endregion
