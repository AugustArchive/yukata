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

@file:JvmName("TypeSystemNodesKt")

package yukata.language.nodes

import yukata.language.SourceLocation
import yukata.language.ast.ASTNode

/**
 * Represents a type-system node for lists, strings, and non-nulled types.
 */
sealed class TypeNode(override val location: SourceLocation?): ASTNode()

/**
 * Represents a node for a List type.
 */
class ListTypeNode(location: SourceLocation?, val type: TypeNode): TypeNode(location) {
    val isNullable: Boolean = type !is NonNulledTypeNode
}

/**
 * Represents a type-system node for a Name type.
 */
class NamedTypeNode(location: SourceLocation?, val name: NameNode): TypeNode(location)

/**
 * Represents a type-system node for a non-nullable object.
 */
class NonNulledTypeNode(location: SourceLocation?, val type: TypeNode): TypeNode(location)
