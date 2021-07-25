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

/**
 * Represents the location in a [Source] object.
 */
data class Location(
    /**
     * 1-indexed value of the line.
     */
    val line: Int,

    /**
     * 1-indexed value of the column.
     */
    val column: Int
) {
    companion object {
        /**
         * Returns a "empty" [Location] object.
         */
        val Empty: Location = Location(1, 1)
    }
}

/**
 * Represents the location of an [ASTNode][yukata.language.ast.ASTNode].
 */
data class SourceLocation(
    /**
     * The character offset of this [Node][yukata.language.ast.ASTNode] begins.
     */
    val start: Int,

    /**
     * The character offset of this [Node][yukata.language.ast.ASTNode] ends.
     */
    val end: Int,

    /**
     * The starting [Token] to determine the source location.
     */
    val startToken: Token,

    /**
     * The end [Token] to determine the source location.
     */
    val endToken: Token,

    /**
     * The source object available.
     */
    val source: Source
) {
    constructor(start: Token, end: Token, source: Source): this(
        start = start.start,
        end = end.end,
        startToken = start,
        endToken = end,
        source = source
    )
}
