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

package yukata.language.exceptions

import yukata.language.Location
import yukata.language.Source
import yukata.language.ast.ASTNode

/**
 * Represents a [Exception] object of what happened during the [lexical][yukata.language.Lexer] and
 * [parsing][yukata.language.Parser] stages.
 */
class GraphQLException(
    /**
     * The message for debugging purposes
     */
    message: String,

    /**
     * An array of the AST nodes corresponding to this error.
     */
    private val nodes: List<ASTNode>?,

    /**
     * The source GraphQL document for the first location of this error.
     */
    private val source: Source?,

    /**
     * Array of character offsets within the source GraphQL document
     * which corrspond to this error.
     */
    private val positions: List<Int>?,

    /**
     * Extension fields to add to the formatted error
     */
    private val extensions: Map<String, Any?>?,

    /**
     * The original exception of this error.
     */
    private val originalException: Throwable?
): Exception(message, originalException) {
    companion object {
        /**
         * Returns a [GraphQLException] as a syntax error.
         */
        fun asSyntaxError(
            message: String,
            source: Source,
            position: Int,
            original: Throwable? = null
        ): GraphQLException = GraphQLException(
            "Syntax Error: $message",
            nodes = null,
            source = source,
            positions = listOf(position),
            extensions = null,
            originalException = original
        )
    }

    /**
     * Creates a [GraphQLException] from a single [node].
     * @param message The message
     * @param node The [ASTNode].
     */
    constructor(message: String, node: ASTNode?): this(
        message = message,
        nodes = node?.let(::listOf),
        null,
        null,
        null,
        null
    )

    /**
     * Returns a list of source locations where this [GraphQLException] occured.
     */
    val locations: List<Location>? by lazy {
        if (positions != null && source != null) {
            positions.map { pos -> retrieveLocationObject(source, pos) }
        } else {
            nodes?.mapNotNull { node ->
                node.location?.let { retrieveLocationObject(it.source, it.start) }
            }
        }
    }

    private fun retrieveLocationObject(source: Source, pos: Int): Location {
        var line = 1
        var column = pos + 1
        val regex = "\\r\\n|[\\n|\\r]".toRegex()

        regex
            .findAll(source.body)
            .toList()
            .map {
                if (it.range.first < pos) {
                    line += 1
                    column = pos + 1 - (it.range.first - it.value.length)
                }
            }

        return Location(
            line = line,
            column = column
        )
    }

    override fun toString(): String = buildString {
        appendLine("yukata: GraphQLException: $message")
        appendLine()

        if (nodes != null) {
            for (node in nodes) {
                if (node.location != null) {
                    appendLine()
                    append(node.location.source.print(retrieveLocationObject(node.location.source, node.location.start)))
                }
            }
        } else if (source != null && locations != null) {
            for (location in locations!!) {
                appendLine()
                append(source.print(location))
            }
        }

        appendLine()

        val frames = stackTrace.slice(0..10)
        for (frame in frames) {
            appendLine("•    $frame")
        }

        appendLine("... ${frames.size} more")
    }
}
