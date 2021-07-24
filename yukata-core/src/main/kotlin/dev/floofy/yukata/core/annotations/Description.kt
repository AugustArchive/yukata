package dev.floofy.yukata.core.annotations

/**
 * Represents a description for a resolver, [Query], [Mutation], [Subscription],
 * [ObjectType], [InputType], or [Enumeration].
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Description(
    /**
     * The text of this [Description] to set in the schema.
     */
    val text: String
)
