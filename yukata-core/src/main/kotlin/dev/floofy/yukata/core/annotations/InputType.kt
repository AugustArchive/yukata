package dev.floofy.yukata.core.annotations

/**
 * Represents a input type in a GraphQL schema.
 */
@Target(AnnotationTarget.CLASS)
annotation class InputType(
    /**
     * Sets the name of this [InputType], by default, it'll use
     * the object's name.
     */
    val name: String = ""
)
