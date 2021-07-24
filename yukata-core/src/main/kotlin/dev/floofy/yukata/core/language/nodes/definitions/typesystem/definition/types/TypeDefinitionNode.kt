package dev.floofy.yukata.core.language.nodes.definitions.typesystem.definition.types

import dev.floofy.yukata.core.language.ast.Location
import dev.floofy.yukata.core.language.nodes.DirectiveNode
import dev.floofy.yukata.core.language.nodes.NameNode
import dev.floofy.yukata.core.language.nodes.definitions.typesystem.definition.DefinitionTypeSystemNode
import dev.floofy.yukata.core.language.nodes.values.StringValueNode

sealed class TypeDefinitionNode(
    location: Location?,
    val name: NameNode,
    val description: StringValueNode?,
    val directives: List<DirectiveNode>?
): DefinitionTypeSystemNode(location)
