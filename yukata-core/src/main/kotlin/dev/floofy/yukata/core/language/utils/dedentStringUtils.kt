package dev.floofy.yukata.core.language.utils

internal fun isLeading(str: String): Int {
    var i = 0
    while (i < str.length && (str[i] == ' ' || str[i] == '\t'))
        i++

    return i
}

internal fun String.isBlank(): Boolean {
    return isLeading(this) == this.length
}

internal fun String.blockStringIndent(lines: List<String>): Int {
    var common: Int? = null
    for (line in lines.drop(1)) {
        val indent = isLeading(line)
        if (indent == line.length)
            continue

        if (common == null || indent < common) {
            common = indent
            if (common == 0)
                break
        }
    }

    return common ?: 0
}

/**
 * Produces the value of a block string from its parsed raw value, similar to
 * CoffeeScript's block string, Python's docstring trim or Ruby's strip_heredoc.
 *
 * This implements the GraphQL spec's BlockStringValue() static algorithm.
 */
fun String.dedentBlockStringValue(): String {
    val lines = this.split("\\r\\n|[\\n\\r]").toMutableList()
    val commonIndent = this.blockStringIndent(lines)

    if (commonIndent != 0) {
        for (i in lines.drop(1).indices) {
            val line = lines[i + 1]
            val remove = if (line.length <= commonIndent - 1) line.length else commonIndent
            lines[i + 1] = line.removeRange(0, remove)
        }
    }

    var start = 0
    while (start < lines.size && lines[start].isBlank())
        ++start

    var end = lines.size
    while (end > start && lines.last().isBlank())
        --end

    return lines.slice(start..end).joinToString("\n")
}
