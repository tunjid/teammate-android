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

import com.tunjid.androidbootstrap.functions.collections.Lists
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.util.*

val yes: Boolean get() = true

fun asDifferentiables(subTypeList: List<Differentiable>): List<Differentiable> =
        ArrayList(subTypeList)

fun replaceStringList(sourceList: List<String>, updatedList: List<String>) {
    val source = Lists.transform<String, Differentiable>(sourceList, { s -> Differentiable.fromCharSequence { s } }, { it.id })
    val updated = Lists.transform<String, Differentiable>(updatedList, { s -> Differentiable.fromCharSequence { s } }, { it.id })
    replaceList(source, updated)
}

fun <T : Differentiable> preserveAscending(source: MutableList<T>, additions: List<T>) {
    concatenateList(source, additions)
    source.sortWith(FunctionalDiff.COMPARATOR)
}

fun <T : Differentiable> preserveDescending(source: MutableList<T>, additions: List<T>) {
    concatenateList(source, additions)
    source.sortWith(FunctionalDiff.DESCENDING_COMPARATOR)
}

fun <T : Differentiable> replaceList(source: MutableList<T>, additions: MutableList<T>): MutableList<T> {
    Lists.replace(source, additions)
    source.sortWith(FunctionalDiff.COMPARATOR)
    return source
}

private fun <T : Differentiable> concatenateList(source: MutableList<T>, additions: List<T>) {
    val set = HashSet(additions)
    set.addAll(source)
    source.clear()
    source.addAll(set)
}
