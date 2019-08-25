/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
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

package com.mainstreetcode.teammate.util

class SingletonCache<O, T> @SafeVarargs
constructor(private val function: (Class<out O>) -> Class<out T>,
            private val defaultFunction: (Class<out T>) -> T,
            vararg pairs: Pair<Class<out T>, T>) {

    private val instanceMap = HashMap<Class<out T>, T>()

    init {
        for (pair in pairs) instanceMap[pair.first] = pair.second
    }

    fun forInstance(itemClass: Class<out T>): T {
        val result = instanceMap[itemClass]
        return result ?: defaultFunction.invoke(itemClass)
    }

    fun forModel(itemClass: Class<out O>): T {
        val aClass = function.invoke(itemClass)
        val result = instanceMap[aClass]
        return result ?: forInstance(aClass)
    }

}
