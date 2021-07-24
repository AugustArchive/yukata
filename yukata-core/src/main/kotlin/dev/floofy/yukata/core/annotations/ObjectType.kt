package dev.floofy.yukata.core.annotations

/**
 * Marks this class as a [ObjectType].
 */
@Target(AnnotationTarget.CLASS)
annotation class ObjectType(
    /**
     * The name of this [ObjectType], by default, it'll use
     * the class' name with `Type`, `type`, `Object`, `object`, `ObjectType`, and `objectType` omitted.
     */
    val name: String = ""
)
