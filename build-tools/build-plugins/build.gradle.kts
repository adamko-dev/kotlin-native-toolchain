import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion

plugins {
  `kotlin-dsl`
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")
  //implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:${expectedKotlinDslPluginsVersion}")
  //implementation(libs.gradlePlugin.kotlin)
  implementation(libs.gradlePlugin.devPublish)

  implementation(libs.gradlePlugin.pluginPublishPlugin)
  implementation(libs.gradlePlugin.nmcp)
}

kotlin {
  jvmToolchain(21)
}
