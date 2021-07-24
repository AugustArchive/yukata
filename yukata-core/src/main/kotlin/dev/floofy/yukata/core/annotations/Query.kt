package dev.floofy.yukata.core.annotations

/**
 * Marks this property as a [Query] within the GraphQL schema.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Query(
    /**
     * The name of the query to set. By default, it'll use
     * the callable's name (cannot contain backticks or it'll error) with
     * `Mutation` / `mutation` omitted.
     */
    val name: String = ""
)
