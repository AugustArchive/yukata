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
 * Represents a [node][AstNode] as a directive.
 *
 * Directives provide a way to describe alternate runtime execution and type validation behaviour
 * in a GraphQL document. In some cases, you need to provide options to alter GraphQL's execution behaviour
 * in ways field arguments will not suffice, such as conditionally including or skipping a field. Directives
 * provide this by describing additional information to the executor.
 *
 * - Directives have a [name][NameNode] along with a [list of arguments][List] which may accept values of any input type.
 * - Directives can be used to describe additional information for types, fields, fragments, and operations.
 *
 * As future versions of GraphQL adopt new configurable execution capabilities, they may be exposed via directives.
 */
data class DirectiveNode(
    override val location: Location?,
    val name: NameNode,
    val args: List<ArgumentNode>?
): AstNode()
