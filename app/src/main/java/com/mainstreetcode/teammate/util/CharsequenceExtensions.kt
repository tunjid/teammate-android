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

fun String.asIntOrFalse(): Int {
    if (isBlank()) return 0
    try {
        return toInt()
    } catch (e: Exception) {
        Logger.log("ModelUtils", "Number Format Exception", e)
    }

    return 0
}

fun String.asFloatOrFalse(): Float {
    if (isBlank()) return 0F
    try {
        return toFloat()
    } catch (e: Exception) {
        Logger.log("ModelUtils", "Number Format Exception", e)
    }

    return 0F
}

fun String.asBooleanOrFalse(): Boolean {
    if (isBlank()) return false
    try {
        return toBoolean()
    } catch (e: Exception) {
        Logger.log("ModelUtils", "Number Format Exception", e)
    }

    return false
}