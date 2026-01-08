package conventions

plugins {
  base
}

group = "dev.adamko.kotlin-native-toolchain"
version = "1.0.0"

tasks.withType<Test>().configureEach {
  systemProperty("junit.jupiter.tempdir.cleanup.mode.default", "ON_SUCCESS")
}
