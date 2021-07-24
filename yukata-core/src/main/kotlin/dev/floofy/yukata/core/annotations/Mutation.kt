package dev.floofy.yukata.core.annotations

/**
 * Marks this function as a [Mutation] within the GraphQL schema.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Mutation(
    /**
     * The name of the mutation to set. By default, it'll use
     * the callable's name (cannot contain backticks or it'll error) with
     * `Mutation` / `mutation` omitted.
     */
    val name: String = ""
)
