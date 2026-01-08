import ext.excludeProjectConfigurationDirs

plugins {
  id("conventions.base")
  idea
}

idea {
  module {
    excludeProjectConfigurationDirs(layout, providers)
  }
}

tasks.updateDaemonJvm {
  languageVersion = JavaLanguageVersion.of(21)
}
