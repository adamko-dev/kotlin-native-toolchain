package dev.adamko.kntoolchain.test_utils

import java.nio.file.Path
import kotlin.io.path.*

@KotlinAssignmentOverloadTarget
abstract class GradleProjectFile {
  protected abstract val file: Path

  fun assign(content: String) {
    if (!file.exists()) {
      file.parent.createDirectories()
      file.createFile()
    }
    file.writeText(content)
  }

  open operator fun plusAssign(content: String) {
    file.appendText(content)
  }

  fun modify(modifier: (content: String) -> String) {
    file.writeText(modifier(file.readText()))
  }
}

class GradlePropertiesFile(
  override val file: Path
) : GradleProjectFile()

class GradleScriptFile(
  override val file: Path
) : GradleProjectFile() {

  /**
   * Adds `import` lines before the rest of the file content.
   */
  override operator fun plusAssign(content: String) {
    val newContent = file.readText() + content
    val processedContent =
      newContent.lineSequence()
        .reduce { acc, line ->
          if (line.startsWith("import ")) {
            line + "\n" + acc
          } else {
            acc + "\n" + line
          }
        }

    file.writeText(processedContent)
  }
}
