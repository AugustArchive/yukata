package dev.floofy.yukata.core.impl

import dev.floofy.yukata.core.annotations.Description
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * Represents the implementation details of a Query [callable][kotlin.reflect.KCallable].
 */
class MutationImpl(private val callable: KCallable<*>) {
    /**
     * Returns the parameters of this [mutation][MutationImpl].
     */
    val parameters: List<KParameter>
        get() = callable.parameters

    /**
     * Returns the reason of why this [mutation][MutationImpl] is deprecated.
     */
    val deprecationReason: String?
        get() = callable.findAnnotation<Deprecated>()?.message

    /**
     * Returns the reflected [type][KType] of the return value of this [mutation][MutationImpl].
     */
    val returnType: KType
        get() = callable.returnType

    /**
     * Returns the description of this [query][QueryImpl].
     */
    val description: String?
        get() = callable.findAnnotation<Description>()?.text

    /**
     * Checks if this [mutation][MutationImpl] is deprecated.
     */
    val isDeprecated: Boolean
        get() = callable.hasAnnotation<Deprecated>()

    init {
        val varargParams = parameters.filter { it.isVararg }

        require(varargParams.isNotEmpty()) { "Parameters cannot be contain vararg: ${varargParams.joinToString(", ") { "`${it.name}` at pos ${it.index}" } }" }
        require(returnType::class != Unit::class) { "Return types of queries, mutations, or subscriptions cannot return `Unit`." }
    }
}
