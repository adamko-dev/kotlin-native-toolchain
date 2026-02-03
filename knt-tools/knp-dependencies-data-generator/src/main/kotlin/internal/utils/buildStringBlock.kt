package dev.adamko.knp.internal.utils

/**
 * Build a string with indentable blocks.
 *
 * Use [StringBlockBuilder.line] to add a line.
 *
 * Use [StringBlockBuilder.block] to add an indented block.
 *
 * @param[defaultIndent] Each block will be indented with this string.
 */
@StringBlockBuilderDsl
internal fun buildStringBlock(
  defaultIndent: String = "  ",
  block: StringBlockBuilder.() -> Unit,
): String {
  val builder = StringBlockBuilderImpl(
    defaultIndent = defaultIndent
  )
  builder.apply(block)
  return builder.render()
}

/**
 * @see buildStringBlock
 */
@StringBlockBuilderDsl
internal sealed interface StringBlockBuilder {

  /**
   * Add a line to the current block.
   */
  @StringBlockBuilderDsl
  fun line(content: String = "")

  /**
   * Starts a new indented block, surrounded by [open] and [close].
   *
   * All content added in [content] will be intended one level more than the current indentation.
   *
   * [open] and [close] will _not_ be indented.
   */
  @StringBlockBuilderDsl
  fun block(open: String, close: String, content: StringBlockBuilder.() -> Unit)
}

private class StringBlockBuilderImpl(
  val level: Int = 0,
  val defaultIndent: String,
) : StringBlockBuilder {
  val lines: ArrayDeque<CodeLine> = ArrayDeque()

  override fun line(
    content: String,
  ) {
    lines.addLast(CodeLine(content, level))
  }

  override fun block(
    open: String,
    close: String,
    content: StringBlockBuilder.() -> Unit,
  ) {
    val builder = StringBlockBuilderImpl(level + 1, defaultIndent)
    builder.apply(content)
    if (open.isNotEmpty()) line(open)
    lines += builder.lines
    if (close.isNotEmpty()) line(close)
  }

  fun render(): String {
    return lines
      .joinToString("\n") { line ->
        line.content
          .prependIndent(defaultIndent.repeat(line.level))
          .trimEnd()
      }
      .trimEnd()
      .let {
        if (it.isBlank()) it else "$it\n"
      }
  }
}

private data class CodeLine(
  val content: String,
  val level: Int,
)

@DslMarker
@MustBeDocumented
internal annotation class StringBlockBuilderDsl
