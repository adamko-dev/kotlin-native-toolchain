plugins {
  `kotlin-dsl`
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")
  implementation(libs.gradlePlugin.devPublish)

  implementation(libs.gradlePlugin.pluginPublishPlugin)
  implementation(libs.gradlePlugin.nmcp)
}

kotlin {
  jvmToolchain(21)
}
