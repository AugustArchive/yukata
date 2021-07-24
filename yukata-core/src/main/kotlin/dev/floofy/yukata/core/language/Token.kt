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

package dev.floofy.yukata.core.language

import dev.floofy.yukata.core.language.kinds.TokenKind

/**
 * Represents a [lexical][Lexer] token.
 */
data class Token(
    /**
     * Returns the kind this [token][Token] is.
     */
    val kind: TokenKind,

    /**
     * The character offset which this Node begins
     */
    val start: Int = 0,

    /**
     * The character offset which this Node ends
     */
    val end: Int = 0,

    /**
     * 1-indexed line number on which this [Token] begins.
     */
    val line: Int = 0,

    /**
     * 1-indexed column number on which this [Token] beings.
     */
    val column: Int = 0,

    /**
     * Represents the interpreted value of this [Token], or `null`
     * if this [Token] is a punctuational token.
     */
    val value: String? = null,

    /**
     * Tokens exists as nodes in a double-linked-list amongst all tokens,
     * even including ignored tokens! [TokenKind.StartOfFile] will always be the first node
     * and [TokenKind.EndOfFile] will always be the last token.
     */
    var prevToken: Token? = null,

    /**
     * Returns the next token in the tree, read the documentation for [Token.prevToken] for more information.
     */
    var nextToken: Token? = null
)
