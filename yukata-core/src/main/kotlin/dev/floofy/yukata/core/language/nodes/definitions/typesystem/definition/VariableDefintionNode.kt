package dev.floofy.yukata.core.language.nodes.definitions.typesystem.definition

import dev.floofy.yukata.core.language.ast.AstNode
import dev.floofy.yukata.core.language.ast.Location
import dev.floofy.yukata.core.language.nodes.DirectiveNode
import dev.floofy.yukata.core.language.nodes.typed.TypeNode
import dev.floofy.yukata.core.language.nodes.values.ValueNode
import dev.floofy.yukata.core.language.nodes.values.VariableValueNode

data class VariableDefintionNode(
    override val location: Location?,
    val variable: VariableValueNode,
    val type: TypeNode,
    val defaultValue: ValueNode?,
    val directives: List<DirectiveNode>?
): AstNode()
