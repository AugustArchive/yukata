/**
 * Copyright (c) 2021 Noel 🌺
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package yukata.language

import kotlin.math.floor

/**
 * Represents a source of a GraphQL request or schematic file.
 */
data class Source(
    /**
     * The name of this [Source], it'll return the full path
     * if this was ran using `yukata.core.Schema#compile(java.io.File)` or
     * it'll just return "GraphQL Request" if `yukata.core.Schema#execute(java.lang.String, kotlin.Map?)` was used.
     */
    val name: String = "GraphQL Request",

    /**
     * The body of this [Source] object.
     */
    val body: String,

    /**
     * The offset location of this [Source].
     */
    val locationOffset: Location = Location.Empty
) {
    fun print(location: Location): String {
        val columnOffset = locationOffset.column - 1
        val body = whitespace(columnOffset) + body

        val lineIndex = location.line - 1
        val lineOffset = locationOffset.line - 1
        val lineNumber = location.line + lineOffset

        val actualOffsetOfColumn = if (location.line == 1) columnOffset else 0
        val columnNumber = location.column + actualOffsetOfColumn
        val locationString = "$name($lineNumber:$columnNumber) ->\n"
        val lines = body.split("\\r\\n|[\\n\\r]".toRegex())
        val locationLine = lines[lineIndex]

        // Special case for minified documents
        if (locationLine.length > 120) {
            val sublineIndex = floor(columnNumber.toDouble() / 80).toInt()
            val sublineColumn = columnNumber % 80
            val sublines = locationLine.chunked(80)

            return (
                locationString +
                printPrefixedLines(
                    listOf(
                        listOf("$lineNumber", sublines[0]),
                        sublines.slice(1..(sublineIndex + 1)).map { listOf("", it) }.flatten(),
                        listOf(" ", whitespace(sublineColumn - 1) + "^"),
                        listOf("", sublines[sublineIndex + 1])
                    )
                )
            )
        }

        return (
            locationString +
            printPrefixedLines(
                listOf(
                    listOf("${lineNumber - 1}", lines.getOrNull(lineIndex - 1)),
                    listOf("$lineNumber", locationLine),
                    listOf("", whitespace(columnNumber - 1) + "^"),
                    listOf("${lineNumber + 1}", lines.getOrNull(lineIndex + 1))
                )
            )
        )
    }

    private fun whitespace(len: Int): String = (1..len).joinToString("") { "" }
    private fun String.padLeft(len: Int): String = whitespace(len - this.length) + this
    private fun printPrefixedLines(lines: List<List<String?>>): String {
        val existing = lines.filter { it.getOrNull(1) != null }
        val padLen = existing.map {
            it[0]?.length ?: error("item line length == null")
        }.maxOrNull() ?: error("line is null. :(")

        return existing.joinToString("\n") {
            it[0]!!.padLeft(padLen) + if (it.getOrNull(1).isNullOrBlank()) " |" else " | " + it[1]
        }
    }
}
