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

package yukata.core.resolvers

import dev.floofy.yukata.core.impl.QueryImpl
import kotlin.reflect.full.hasAnnotation
import yukata.core.annotations.Mutation
import yukata.core.annotations.Query
import yukata.core.impl.MutationImpl

/**
 * Represents a resolver to implement for any [Queries][dev.floofy.yukata.core.impl.QueryImpl] and
 * [Mutations][dev.floofy.yukata.core.impl.MutationImpl].
 *
 * @param resolverName The name of this [resolver][AbstractResolver]. It'll use
 * the class name & omits `Resolver` / `resolver` from the class name.
 */
abstract class AbstractResolver(private val resolverName: String? = null) {
    /**
     * Returns the list of mutations available in this [resolver][AbstractResolver].
     */
    val mutations: List<yukata.core.impl.MutationImpl>
        get() = this::class.members.filter { it.hasAnnotation<yukata.core.annotations.Mutation>() }.map {
            yukata.core.impl.MutationImpl(
                it
            )
        }

    /**
     * Returns the list of queries available in this [resolver][AbstractResolver].
     */
    val queries: List<QueryImpl>
        get() = this::class.members.filter { it.hasAnnotation<yukata.core.annotations.Query>() }.map { QueryImpl(it) }

    /**
     * Returns the name of this [resolver][AbstractResolver], useful for schema errors.
     */
    val name: String = resolverName
        ?: this::class.qualifiedName?.replace("Resolver", "")?.replace("resolver", "")
        ?: error("cannot be local or anonymous class.")
}
