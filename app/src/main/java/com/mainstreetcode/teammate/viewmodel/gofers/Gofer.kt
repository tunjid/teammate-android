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

package com.mainstreetcode.teammate.viewmodel.gofers

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.model.ListableModel
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.ModelUtils
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.*

abstract class Gofer<T> internal constructor(
        protected val model: T,
        private val onError: (Throwable) -> Unit
) where T : Model<T>, T : ListableModel<T> {

    val items: MutableList<Differentiable>

    init {
        items = ArrayList()
    }

    abstract fun getImageClickMessage(fragment: Fragment): String?

    internal abstract fun delete(): Completable

    internal abstract fun changeEmitter(): Flowable<Boolean>

    internal abstract fun upsert(): Single<DiffUtil.DiffResult>

    internal abstract fun fetch(): Flowable<DiffUtil.DiffResult>

    fun clear() {}

    fun remove(): Completable = delete().doOnError(onError).observeOn(mainThread())

    fun watchForChange(): Flowable<Any> =
            changeEmitter().filter { changed -> changed }.cast(Any::class.java).observeOn(mainThread())

    fun save(): Single<DiffUtil.DiffResult> = upsert().doOnError(onError)

    fun get(): Flowable<DiffUtil.DiffResult> =
            if (model.isEmpty) Flowable.empty() else fetch().doOnError(onError)

    @SuppressLint("CheckResult")
    internal fun startPrep() {
        watchForChange().subscribe({ }, ErrorHandler.EMPTY::accept)
    }

    internal open fun preserveItems(old: MutableList<Differentiable>, fetched: MutableList<Differentiable>): MutableList<Differentiable> {
        ModelUtils.preserveAscending(old, fetched)
        return old
    }

    companion object {

        fun tag(seed: String, model: Model<*>): String {
            val uuid = if (model.isEmpty) UUID.randomUUID().toString() else model.id
            return "$seed-$uuid"
        }
    }
}
