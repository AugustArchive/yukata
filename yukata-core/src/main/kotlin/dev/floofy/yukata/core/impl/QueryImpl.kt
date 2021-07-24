/**
 * Copyright (c) 2021 Noel 🌺
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
class QueryImpl(private val callable: KCallable<*>) {
    /**
     * Returns the parameters of this [query][QueryImpl].
     */
    val parameters: List<KParameter>
        get() = callable.parameters

    /**
     * Returns the reason of why this [query][QueryImpl] is deprecated.
     */
    val deprecationReason: String?
        get() = callable.findAnnotation<Deprecated>()?.message

    /**
     * Returns the reflected [type][KType] of the return value of this [query][QueryImpl].
     */
    val returnType: KType
        get() = callable.returnType

    /**
     * Returns the description of this [query][QueryImpl].
     */
    val description: String?
        get() = callable.findAnnotation<Description>()?.text

    /**
     * Checks if this [query][QueryImpl] is deprecated.
     */
    val isDeprecated: Boolean
        get() = callable.hasAnnotation<Deprecated>()

    init {
        val varargParams = parameters.filter { it.isVararg }

        require(varargParams.isNotEmpty()) { "Parameters cannot be contain vararg: ${varargParams.joinToString(", ") { "`${it.name}` at pos ${it.index}" } }" }
        require(returnType::class != Unit::class) { "Return types of queries, mutations, or subscriptions cannot return `Unit`." }
    }
}
