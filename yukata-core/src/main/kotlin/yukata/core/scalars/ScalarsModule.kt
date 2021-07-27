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

package yukata.core.scalars

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class ScalarsModuleBuilder {
    private var module: ScalarsModule = ScalarsModule()

    /**
     * Extend a [ScalarsModule] in this [builder][ScalarsModuleBuilder].
     * @param mod The scalars module to use
     * @return This [ScalarsModuleBuilder] to chain methods
     */
    fun extend(mod: ScalarsModule): ScalarsModuleBuilder {
        val oldMod = module
        module = oldMod.extend(mod)

        return this
    }

    /**
     * Injects a new [ScalarSerializer] into this [ScalarsModuleBuilder], reified as [T].
     * @return This [ScalarsModuleBuilder] to chain methods
     */
    inline fun <reified T: ScalarSerializer<*>> scalar(): ScalarsModuleBuilder = scalar(T::class)

    /**
     * Injects a new [ScalarSerializer] into this [ScalarsModuleBuilder].
     * @param kClass The class to use
     * @return This [ScalarsModuleBuilder] to chain methods
     */
    fun <T: ScalarSerializer<*>> scalar(kClass: KClass<T>): ScalarsModuleBuilder = scalar(kClass.createInstance())

    /**
     * Injects a new [ScalarSerializer] into this [ScalarsModuleBuilder].
     * @param scalar The serializer to use
     * @return This [ScalarsModuleBuilder] to chain methods
     */
    fun scalar(scalar: ScalarSerializer<*>): ScalarsModuleBuilder {
        module += scalar
        return this
    }

    fun build(): ScalarsModule = module
}

/**
 * Creates a new [ScalarsModule] using a [block].
 * @param block The builder object
 * @return This [ScalarsModule].
 */
@OptIn(ExperimentalContracts::class)
fun ScalarsModule(block: ScalarsModuleBuilder.() -> Unit): ScalarsModule {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val module = ScalarsModule()
    val builder = ScalarsModuleBuilder().apply(block).build()
    return module.extend(builder)
}

/**
 * Represents a collection of [scalars][ScalarSerializer] available.
 */
class ScalarsModule: MutableList<ScalarSerializer<*>> by mutableListOf() {
    init {
        this += BooleanScalar()
    }

    /**
     * Extends this [ScalarsModule] with a new [ScalarsModule]
     * @param other The other [ScalarsModule] to merge.
     * @returns A new instance of the [ScalarsModule].
     */
    fun extend(other: ScalarsModule): ScalarsModule = (this + other).toMutableList() as ScalarsModule

    /**
     * Extends this [ScalarsModule] with a new [ScalarsModule] using a [ScalarsModuleBuilder].
     * @param builder The builder object
     * @returns A new instance of the [ScalarsModule].
     */
    @OptIn(ExperimentalContracts::class)
    fun extend(block: ScalarsModuleBuilder.() -> Unit): ScalarsModule {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        val mod = ScalarsModuleBuilder().apply(block).build()
        return extend(mod)
    }
}
