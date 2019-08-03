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

import androidx.arch.core.util.Function
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.TeammateException

import java.util.ArrayList

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class UserGofer(
        model: User,
                private val authUserFunction: Function<User, Boolean>,
                private val getFunction: Function<User, Flowable<User>>,
                private val updateFunction: Function<User, Single<User>>
) : Gofer<User>(model, ErrorHandler.EMPTY) {

    init {
        items.addAll(filter(ArrayList(model.asItems())))
    }

     override fun changeEmitter(): Flowable<Boolean> = Flowable.empty()

    override fun getImageClickMessage(fragment: Fragment): String? = null

    public override fun fetch(): Flowable<DiffUtil.DiffResult> =
            FunctionalDiff.of(getFunction.apply(model).map(User::asDifferentiables), items) { _, updated -> filter(updated) }

     override fun upsert(): Single<DiffUtil.DiffResult> =
             FunctionalDiff.of(updateFunction.apply(model).map(User::asDifferentiables), items) { _, updated -> updated }

    public override fun delete(): Completable =
            Completable.error(TeammateException("Cannot delete"))

    private fun filter(list: MutableList<Differentiable>): List<Differentiable> {
        val isAuthUser = authUserFunction.apply(model)
        if (isAuthUser) return list

        val it = list.iterator()

        while (it.hasNext()) {
            val next = it.next() as? Item<*> ?: continue
            if (next.stringRes == R.string.email) it.remove()
        }

        return list
    }
}
