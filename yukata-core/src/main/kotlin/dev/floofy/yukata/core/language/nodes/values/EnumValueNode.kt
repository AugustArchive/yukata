package dev.floofy.yukata.core.language.nodes.values

import dev.floofy.yukata.core.language.ast.Location

class EnumValueNode(
    location: Location?,
    val value: String
): ValueNode(location)
