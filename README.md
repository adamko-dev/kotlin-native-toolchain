# Kotlin Native Toolchain

The Kotlin Native Toolchain Gradle plugin installs the tools required for Kotlin/Native development.
For example, installing `clang` for compiling C/C++ code to be compatible with Kotlin/Native.

It is easy to use, and cache-friendly.

### Usage

#### Requirements

- Gradle 9.0+
- Kotlin 2.0.0+
   (Versions of kotlin-native-prebuilt prior to 2.0.0 were not published to Maven Central.)

#### Using in a project

KNPT is a Gradle plugin and can be applied to a project as follows:

```kotlin
// build.gradle.kts
import dev.adamko.kntoolchain.kotlinNativePrebuiltDependencies

plugins {
  id("dev.adamko.kotlin-native-toolchain") version "x.x.x"
}

repositories {
  kotlinNativePrebuiltDependencies()
}
```

The installation directory can be obtained using `knToolchain.provisionInstallation()`,
which returns a `Provider<Path>`.
**Important**: the `Provider` must only be evaluated in the task execution phase.

```kotlin
val knToolchainUser by tasks.registering {
  val knToolchain = knToolchain.provisionInstallation()

  doLast {
    val knToolchain = knToolchain.get()
    println("knToolchain dir: {" + knToolchain.invariantSeparatorsPathString + "}")
  }
}
```

```kotlin
val knToolchainUser by tasks.registering {
  val knToolchain = knToolchain.provisionInstallation()
  inputs.dir(knToolchain)

  doLast {
    val knToolchain = knToolchain.get()
    println("knToolchain dir: " + knToolchain.invariantSeparatorsPathString)
  }
}
```

#### Using in settings

The `dev.adamko.kotlin-native-toolchain` plugin can also be applied as a Settings plugin,
in a `settings.gradle.kts` file.

This has two advantages:

1. The `kotlinNativePrebuiltDependencies()` repository will be automatically added in `dependencyResolutionManagement {}`.
2. The plugin will register a task that will verify the checksums of the install distributions.

### Configuration

The location of the installation directory can be configured in the DSL, or via the environment variable `KN_TOOLCHAINS_DIR`.

### Motivation

C code must be compiled before it can be used in Kotlin/Native code.

K/N uses C compilation tools (for example, `gcc` and `clang`).
Furthermore, specific versions of these tools are required.
Installing older versions of these tools on a machine is sometimes challenging.

K/N's solution a pre-packaged distribution of the required tools, named 'kotlin-native-prebuilt'.
Each K/N target and Kotlin version has a variant of the prebuilt distribution.
Kotlin Gradle plugin provisions distributions into `~/.konan/`, by default.
The default install tool
(see [`org.jetbrains.kotlin:kotlin-native-utils`][DependencyDownloader.kt])
is not cache-friendly, and is quite tightly integrated into KGP.

Other Gradle plugins could rely on KGP to provision a kotlin-native-prebuilt distribution,
however, relying on KGP causes unnecessary coupling with K/N compilation, commonizer, and cinterop tasks.
It's hard to get the coupling right.
Problems manifest in strange compilation errors, (because KGP hasn't provisioned kotlin-native-prebuilt yet),
or IntelliJ import errors (because it's hard to trigger KGP's provisioning).

[DependencyDownloader.kt]: https://github.com/JetBrains/kotlin/blob/v2.1.21/native/utils/src/org/jetbrains/kotlin/konan/util/DependencyDownloader.kt
