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

import androidx.recyclerview.widget.DiffUtil
import com.tunjid.androidx.functions.collections.replace
import com.tunjid.androidx.recyclerview.diff.Diff
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit

class InstantSearch<T, R>(searcher: (T) -> Single<out List<R>>, diffFunction: (R) -> Differentiable) {

    val currentItems: MutableList<R> = mutableListOf()
    private val searchProcessor: PublishProcessor<T> = PublishProcessor.create()
    private val searchFlowable: Flowable<DiffUtil.DiffResult> = searchProcessor
            .debounce(SEARCH_DEBOUNCE.toLong(), TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .switchMapSingle(searcher::invoke)
            .map { Diff.calculate(currentItems, it, { _, next -> next }, diffFunction::invoke) }
            .observeOn(mainThread())
            .doOnNext { diff -> currentItems.replace(diff.items) }
            .map { diff -> diff.result }

    fun postSearch(query: T) = searchProcessor.onNext(query)

    fun subscribe(): Flowable<DiffUtil.DiffResult> = searchFlowable

    companion object {

        private const val SEARCH_DEBOUNCE = 300
    }
}
