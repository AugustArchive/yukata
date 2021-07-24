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

package dev.floofy.yukata.core.language.nodes.selection

import dev.floofy.yukata.core.language.ast.Location
import dev.floofy.yukata.core.language.nodes.ArgumentNode
import dev.floofy.yukata.core.language.nodes.DirectiveNode
import dev.floofy.yukata.core.language.nodes.NameNode
import dev.floofy.yukata.core.language.nodes.SelectionSetNode

class SelectionFieldNode(
    parent: SelectionNode?,
    val alias: NameNode?,
    val name: NameNode,
    val arguments: List<ArgumentNode>?,
    val directives: List<DirectiveNode>?
): SelectionNode(parent) {
    private var _selectionSet: SelectionSetNode? = null
    private var _loc: Location? = null

    override val location: Location?
        get() = _loc

    val selectionSet: SelectionSetNode?
        get() = _selectionSet

    internal fun finalize(set: SelectionSetNode?, location: Location?): SelectionFieldNode {
        _selectionSet = set
        _loc = location

        return this
    }

    override val fullPath: String
        get() = (parent?.fullPath?.let { "$it." } ?: "") + (alias ?: name).value
}