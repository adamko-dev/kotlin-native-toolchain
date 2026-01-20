plugins {
  id("conventions.kotlin-jvm")
  kotlin("plugin.serialization") version embeddedKotlinVersion
}

description = """
  Shared data types for fetching Konan dependencies.
  
  This project is an included build because it is used in both the gradle plugin and the standalone data-fetcher.
  """.trimIndent()

dependencies {
  implementation(platform(libs.kotlinxSerialization.bom))
  implementation(libs.kotlinxSerialization.core)

  compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")
}
