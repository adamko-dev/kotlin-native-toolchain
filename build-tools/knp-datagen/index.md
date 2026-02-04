# knp-datagen

Tools for retrieving kotlin-native-prebuilt distribution dependency information.

Each kotlin-native-prebuilt distribution requires additional dependencies.
These are computed based on the Kotlin/Native OS and architecture target,
using information defined in the `konan.properties` file packaged in the distribution.

To make it easier for the knt-gradle-plugin to download the dependencies,
this project extracts the dependencies and stores them in a JSON file.

### Workings

1. Determine the dependencies required per host machine, and K/N target:
    1. Determine the available Kotlin versions.
    2. For each Kotlin version, query Maven Central to determine the variants of
       `org.jetbrains.kotlin:kotlin-native-prebuilt`.
       There is a variant for each operating system family (Windows, macOS, Linux), and the architectures.
    3. For each `kotlin-native-prebuilt` variant, extract the single `konan.properties` file.
    4. For each `konan.properties` file extract the required dependency URLs.
    5. Convert the dependency URLs to GAV Maven coordinates.
    6. Save the data to a JSON file.
2. In [knp-dependencies-data](knp-dependencies-data),
   load the JSON file. Share it as an outgoing Configuration.
3. In [knt-gradle-plugin](../knt-modules/knt-gradle-plugin),
   pass the data into the program.
    1. For each kotlin-native-prebuilt dependency, unpackage the dependency.

The consuming project must add an Ivy repo for resolving the dependencies:
`https://download.jetbrains.com/kotlin/native`
