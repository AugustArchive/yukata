package dev.floofy.yukata.core.language.nodes

import dev.floofy.yukata.core.language.ast.AstNode
import dev.floofy.yukata.core.language.ast.Location
import dev.floofy.yukata.core.language.nodes.typed.TypeNode
import dev.floofy.yukata.core.language.nodes.values.StringValueNode
import dev.floofy.yukata.core.language.nodes.values.ValueNode

data class InputValueNode(
    override val location: Location?,
    val name: NameNode,
    val description: StringValueNode?,
    val type: TypeNode,
    val defaultValue: ValueNode?,
    val directives: List<DirectiveNode>?
): AstNode()
