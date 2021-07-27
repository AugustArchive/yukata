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

package yukata.core

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import yukata.core.annotations.InputType
import yukata.core.annotations.ObjectType
import yukata.core.context.Context
import yukata.core.resolvers.AbstractResolver
import yukata.core.scalars.ScalarsModule

/**
 * Creates a new [Schema] object.
 * @param block The builder to construct a [Schema].
 */
@OptIn(ExperimentalContracts::class)
fun createSchema(block: SchemaBuilder.() -> Unit): Schema {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val builder = SchemaBuilder().apply(block)
    return builder.build()
}

/**
 * Represents a builder class to construct [Schema]s.
 *
 * ## Example
 * ```kotlin
 * val schema = createSchema {
 *     // Register resolvers
 *     resolver(UserResolver)
 *     resolver(LoginResolver)
 *
 *     // Register types
 *     objectType(SomeObjectType)
 *
 *     // Register scalars
 *     scalar(SomeScalar)
 *
 *     // ...
 * }
 * ```
 */
class SchemaBuilder {
    private val resolvers: MutableList<AbstractResolver> = mutableListOf()
    private var useContext: Context? = null
    private val scalarsModule: ScalarsModule = ScalarsModule()

    /**
     * Appends a custom context into the [Schema].
     * @param context The context object to use
     * @return This [SchemaBuilder] for chaining methods.
     */
    fun context(context: Context): SchemaBuilder {
        useContext = context
        return this
    }

    /**
     * Appends a resolver with mutations or queries in this [Schema].
     * @param kClass The class object to use
     * @return This [SchemaBuilder] for chaining methods.
     */
    fun resolver(kClass: KClass<AbstractResolver>): SchemaBuilder {
        val resolver = kClass.createInstance()
        resolvers.add(resolver)

        return this
    }

    /**
     * Adds a object type to this [Schema] but reified as T
     * @return This [SchemaBuilder] for chaining methods.
     */
    inline fun <reified T> objectType(): SchemaBuilder = objectType(T::class)

    /**
     * Adds a object type to this [Schema].
     * @param kClass The class object to use
     * @return This [SchemaBuilder] for chaining methods.
     */
    fun objectType(type: KClass<*>): SchemaBuilder {
        require(type.hasAnnotation<ObjectType>()) { "Object type ${type::class} must have the @ObjectType annotation" }
        require(type.isData) { "Object type ${type::class} must be a data class." }

        return this
    }

    /**
     * Adds a input type to this [Schema] but reified as T.
     * @return This [SchemaBuilder] for chaining methods
     */
    inline fun <reified T> inputType(): SchemaBuilder = inputType(T::class)

    /**
     * Adds a input type to this [Schema].
     * @param type The [class][KClass] to use
     * @return This [SchemaBuilder] for chaining methods
     */
    fun inputType(type: KClass<*>): SchemaBuilder {
        require(type.hasAnnotation<InputType>()) { "Input type ${type::class} must have the @InputType annotation" }
        require(type.isData) { "Input type ${type::class} must be a data class." }

        return this
    }

    fun build(): Schema = Schema()
}
