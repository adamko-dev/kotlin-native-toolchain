plugins {
  id("conventions.kotlin-jvm")
  id("conventions.maven-publishing")
  id("dev.adamko.kntoolchain.tools.konan-dependencies-data-fetcher")
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
