plugins {
  `kotlin-dsl`
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")
  implementation("org.jetbrains.kotlin:kotlin-assignment:${embeddedKotlinVersion}")
  implementation(libs.gradlePlugin.devPublish)

  implementation(libs.gradlePlugin.pluginPublishPlugin)
  implementation(libs.gradlePlugin.nmcp)

  implementation(platform(libs.kotlinxSerialization.bom))
  implementation(libs.kotlinxSerialization.core)
}

kotlin {
  jvmToolchain(21)
}
