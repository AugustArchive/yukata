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

@file:JvmName("DefinitionNodesKt")

package yukata.language.nodes

import yukata.language.OperationKind
import yukata.language.SourceLocation
import yukata.language.ast.ASTNode

/**
 * Represents a [node][ASTNode] as a definition.
 */
sealed class DefinitionNode(override var location: SourceLocation?): ASTNode()

// ~~~~~~~~~~~ Type System ~~~~~~~~~~~ \\

/**
 * Represents a [definition node][DefinitionNode] as a type-system definition node.
 */
sealed class TypeSystemDefinitionNode(location: SourceLocation?): DefinitionNode(location)

class SchemaDefinitionNode(
    location: SourceLocation?,
    val description: StringValueNode?,
    val directives: List<ConstDirectiveNode>?,
    val operationTypes: List<OperationTypeDefinitionNode>?
): TypeSystemDefinitionNode(location)

class VariableDefinitionNode(
    override var location: SourceLocation?,
    val variable: VariableValueNode,
    val type: TypeNode,
    val defaultValue: ValueNode?,
    val directives: List<ConstDirectiveNode>?
): ASTNode()

class FieldDefinitionNode(
    location: SourceLocation?,
    val description: StringValueNode?,
    val name: NameNode,
    val arguments: List<InputValueNode>?,
    val type: TypeNode,
    val directives: List<ConstDirectiveNode>?
): TypeSystemDefinitionNode(location)

class DirectiveTypeDefinitionNode(
    location: SourceLocation?,
    val name: NameNode,
    val description: StringValueNode?,
    val arguments: List<InputValueNode>?,
    val repeatable: Boolean,
    val locations: List<NameNode>
): TypeSystemDefinitionNode(location)

class EnumTypeDefinitionNode(
    location: SourceLocation?,
    val description: StringValueNode?,
    val name: NameNode,
    val directives: List<ConstDirectiveNode>?,
    val values: List<EnumValueDefinitionNode>?
): TypeSystemDefinitionNode(location)

class EnumValueDefinitionNode(
    location: SourceLocation?,
    val description: StringValueNode?,
    val name: NameNode,
    val directives: List<ConstDirectiveNode>?
): TypeSystemDefinitionNode(location)

class InputObjectTypeDefinitionNode(
    location: SourceLocation?,
    val description: StringValueNode?,
    val name: NameNode,
    val directives: List<ConstDirectiveNode>?,
    val fields: List<InputValueNode>?
): TypeSystemDefinitionNode(location)

class InterfaceTypeDefinitionNode(
    location: SourceLocation?,
    val description: StringValueNode?,
    val name: NameNode,
    val directives: List<ConstDirectiveNode>?,
    val interfaces: List<NamedTypeNode>?,
    val fields: List<FieldDefinitionNode>?
): TypeSystemDefinitionNode(location)

class UnionTypeDefinitionNode(
    location: SourceLocation?,
    val description: StringValueNode?,
    val name: NameNode,
    val directives: List<ConstDirectiveNode>?,
    val types: List<NamedTypeNode>
): TypeSystemDefinitionNode(location)

class ScalarTypeDefinitionNode(
    location: SourceLocation?,
    val description: StringValueNode?,
    val name: NameNode,
    val directives: List<ConstDirectiveNode>?
): TypeSystemDefinitionNode(location)

class ObjectTypeDefinitionNode(
    location: SourceLocation?,
    val description: StringValueNode?,
    val name: NameNode,
    val directives: List<ConstDirectiveNode>?,
    val interfaces: List<NamedTypeNode>?,
    val fields: List<FieldDefinitionNode>?
): TypeSystemDefinitionNode(location)

// ~~~~~~~~~~~ Executable ~~~~~~~~~~~ \\
sealed class ExecutableDefinitionNode(
    location: SourceLocation?,
    val name: NameNode?,
    val variables: List<VariableDefinitionNode>?,
    val directives: List<DirectiveNode>?,
    val selectionSet: SelectionSetNode
): DefinitionNode(location)

/**
 * Represents a fragment definition
 */
class FragmentDefinitionNode(
    location: SourceLocation?,
    name: NameNode?,
    directives: List<DirectiveNode>? = listOf(),
    selectionSet: SelectionSetNode,
    val condition: NamedTypeNode
): ExecutableDefinitionNode(location, name, emptyList(), directives, selectionSet)

/**
 * Represents a operation definition
 */
class OperationDefinitionNode(
    location: SourceLocation?,
    name: NameNode?,
    variables: List<VariableDefinitionNode>?,
    directives: List<DirectiveNode>? = listOf(),
    selectionSet: SelectionSetNode,
    val operation: OperationKind
): ExecutableDefinitionNode(location, name, variables, directives, selectionSet)

/**
 * Represents a operation type definition.
 */
class OperationTypeDefinitionNode(
    override var location: SourceLocation?,
    val operation: OperationKind,
    val type: NamedTypeNode
): ASTNode()
