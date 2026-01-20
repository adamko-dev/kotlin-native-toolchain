plugins {
  id("conventions.kotlin-jvm")
  id("conventions.maven-publishing")
  id("dev.adamko.knp.KnpDataGenPlugin")
}

dependencies {
}

tasks.knpDataGen {
  outputDir.set(layout.projectDirectory.dir("src/mainGenerated/kotlin"))
}

kotlin {
  sourceSets.main {
    kotlin.srcDir(tasks.knpDataGen)
  }
}

publishing {
  publications.register<MavenPublication>("maven") {
    from(components["java"])
  }
}
