package dev.floofy.yukata.core.annotations

/**
 * Marks this class as a [Enumeration] property. This will only
 * work on enum classes, and nothing more.
 */
@Target(AnnotationTarget.CLASS)
annotation class Enumeration(
    /**
     * The name of this [Enum]. The default value for this
     * [Enum] class would be the class name with `Enum` / `enum` being
     * omitted.
     */
    val name: String = ""
)
