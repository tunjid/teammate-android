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

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import io.reactivex.Completable
import io.reactivex.disposables.Disposable

import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

class Deferrer(private val delay: Long, private val toRun: () -> Unit) {

    private val ref: AtomicReference<Disposable> = AtomicReference()

    fun advanceDeadline() {
        val disposable = ref.get()
        disposable?.dispose()
        ref.set(Completable.timer(delay, TimeUnit.MILLISECONDS).observeOn(mainThread()).subscribe(toRun::invoke))
    }

}
