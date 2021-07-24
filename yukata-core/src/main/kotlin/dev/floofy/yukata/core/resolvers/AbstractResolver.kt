package dev.floofy.yukata.core.resolvers

import kotlin.reflect.KCallable
import kotlin.reflect.full.hasAnnotation

/**
 * Represents a resolver to implement for any [Queries][dev.floofy.yukata.core.impl.Query],
 * [Mutations][dev.floofy.yukata.core.impl.Mutation], and [Subscriptions][dev.floofy.yukata.core.impl.Subscription].
 *
 * @param name The name of this [resolver][AbstractResolver]. It'll use
 * the class name & omits `Resolver` / `resolver` from the class name.
 */
abstract class AbstractResolver(val name: String = "") {
    val queries: List<KCallable<*>>
        get() = this::class.members.toList()
}
