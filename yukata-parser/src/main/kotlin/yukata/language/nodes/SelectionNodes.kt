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

package yukata.language.nodes

import yukata.language.SourceLocation
import yukata.language.ast.ASTNode

/**
 * Represents a set of [selection nodes][SelectionNode].
 */
data class SelectionSetNode(
    override var location: SourceLocation?,
    val selectors: List<SelectionNode>
): ASTNode()

/**
 * Represents a selection ast node.
 */
open class SelectionNode(override var location: SourceLocation?): ASTNode()

/**
 * Represents a field selection [ast node][ASTNode].
 */
class FieldSelectionNode(
    location: SourceLocation?,
    val alias: NameNode?,
    val name: NameNode,
    val arguments: List<ArgumentNode>?,
    val directives: List<DirectiveNode>?,
    val selectionSet: SelectionSetNode?
): SelectionNode(location)

/**
 * Represents a fragment spread [ast node][ASTNode].
 */
class FragmentSpreadSelectionNode(
    location: SourceLocation?,
    val name: NameNode,
    val directives: List<DirectiveNode>?
): SelectionNode(location)

/**
 * Represents a inline fragment [ast node][ASTNode].
 */
class InlineFragmentSelectionNode(
    location: SourceLocation?,
    val typeCondition: NamedTypeNode?,
    val directives: List<DirectiveNode>?,
    val selectionSet: SelectionSetNode
): SelectionNode(location)
