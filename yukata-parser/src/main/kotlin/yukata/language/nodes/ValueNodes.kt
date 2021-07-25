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

@file:JvmName("ValueNodesKt")

package yukata.language.nodes

import yukata.language.SourceLocation
import yukata.language.ast.ASTNode

/**
 * Represents a node of a specific value.
 */
sealed class ValueNode(override val location: SourceLocation?): ASTNode()

/**
 * Represents this [value node][ValueNode] as a [Double] type.
 */
class DoubleValueNode(location: SourceLocation?, val value: Double): ValueNode(location)

/**
 * Represents this [value node][ValueNode] as a enumeration member type.
 */
class EnumValueNode(location: SourceLocation?, val value: String): ValueNode(location)

/**
 * Represents this [value node][ValueNode] as a [Int] type.
 */
class IntValueNode(location: SourceLocation?, val value: Int): ValueNode(location)

/**
 * Represents this [value node][ValueNode] as a [List] of [value nodes][ValueNode].
 */
class ArrayValueNode(location: SourceLocation?, val values: List<ValueNode>): ValueNode(location)

/**
 * Represents this [value node][ValueNode] as `null`.
 */
class NullValueNode(location: SourceLocation?): ValueNode(location)

/**
 * Represents this [value node][ValueNode] as a object.
 */
class ObjectValueNode(location: SourceLocation?, val members: List<ObjectMemberValueNode>): ValueNode(location) {
    class ObjectMemberValueNode(
        location: SourceLocation?,
        val name: NameNode,
        val value: ValueNode
    ): ValueNode(location)
}

/**
 * Represents this [value node][ValueNode] as a [String] type.
 */
class StringValueNode(location: SourceLocation?, val value: String): ValueNode(location)

/**
 * Represents this [value node][ValueNode] as a variable
 */
class VariableValueNode(location: SourceLocation?, val name: NameNode): ValueNode(location)
