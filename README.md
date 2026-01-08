# Kotlin Native Prebuilt Toolchain

Kotlin Native Prebuilt Toolchain Gradle plugin, for cache-friendly 'kotlin-native-prebuilt' provisioning.
Installs required tools for compiling C/C++ code to be used by Kotlin/Native.

### Usage

KNPT is a Gradle plugin and can be applied to a project as follows:

```kotlin
// build.gradle.kts
plugins {
  
}
```



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
