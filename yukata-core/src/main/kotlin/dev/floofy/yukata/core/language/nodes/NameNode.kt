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

package dev.floofy.yukata.core.language.nodes

import dev.floofy.yukata.core.language.ast.AstNode
import dev.floofy.yukata.core.language.ast.Location

/**
 * Represents a [NameNode].
 *
 * ```
 * Name ::
 *    /[_A-Za-z][_0-9A-Za-z]*\/
 * ```
 *
 * GraphQL documents are full of named things:
 *
 * - operations
 * - fields
 * - arguments
 * - types
 * - directives
 * - fragments
 * - variables
 *
 * All names must follow the same grammatical form. Names in GraphQL are case-sensitive. That is to say name,
 * Name, and NAME all refer to different names. Underscores are significant, which means other_name
 * and othername are two different names.
 *
 * Names in GraphQL are limited to this ASCII subset of possible characters to support interoperation
 * with as many other systems as possible.
 */
data class NameNode(
    override val location: Location?,
    val value: String
): AstNode()
