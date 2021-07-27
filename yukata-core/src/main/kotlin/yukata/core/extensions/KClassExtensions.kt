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

package yukata.core.extensions

import kotlin.reflect.KClass

/**
 * Creates a new instance of this [KClass] with one constructor argument
 * @param arity1 The first constructor argument when creating
 * @return The object constructed
 */
inline fun <T: Any, reified U: Any> KClass<T>.createInstance(arity1: U): T {
    val constructor = this.constructors.find {
        it.parameters.size == 1 && it.parameters[0] is U
    } ?: error("No constructors with exactly 1 parameter wasn't found or the first parameter is not ${U::class}")

    return constructor.callBy(mapOf(
        constructor.parameters[0] to arity1
    ))
}

/**
 * Creates a new instance of this [KClass] with n amount of arguments
 * @param args The arguments to call
 * @return The object constructed
 */
fun <T: Any> KClass<T>.createInstance(vararg args: Any): T {
    val constructor = this.constructors.find {
        it.parameters.size == args.size
    } ?: error("No constructors with exactly ${args.size} amount of parameters wasn't found.")

    return constructor.call(args)
}
