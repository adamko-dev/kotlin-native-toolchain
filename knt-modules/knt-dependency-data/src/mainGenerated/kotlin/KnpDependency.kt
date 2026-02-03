package dev.adamko.kntoolchain.tools.data

data class KnpDependency internal constructor(
  val coord: String,
  val artifact: String? = null,
) {

  val group: String
  val module: String
  val version: String
  val classifier: String?
  val extension: String

  init {
    val match = coordRegex.matchEntire(coord)
      ?: error("Invalid dependency notation: $coord")
    group = match.groups["group"]?.value
      ?: error("Missing 'group' in dependency notation: $coord")
    module = match.groups["module"]?.value
      ?: error("Missing 'module' in dependency notation: $coord")
    version = match.groups["version"]?.value
      ?: error("Missing 'version' in dependency notation: $coord")
    classifier = match.groups["classifier"]?.value // Optional, can be null
    extension = match.groups["extension"]?.value
      ?: error("Missing 'extension' in dependency notation: $coord")
  }

  companion object {
    private val coordRegex: Regex =
      Regex("""(?<group>[^:]+):(?<module>[^:]+):(?<version>[^:@]+)(?::(?<classifier>[^@]+))?@(?<extension>[.a-z]+)""")
  }
}
