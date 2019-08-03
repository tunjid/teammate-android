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
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.TeammateException
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer

class GuestGofer(
        model: Guest,
        onError: Consumer<Throwable>,
        private val getFunction: Function<Guest, Flowable<Guest>>
) : TeamHostingGofer<Guest>(model, onError) {

    init {
        items.addAll(model.asItems())
    }

    override fun getImageClickMessage(fragment: Fragment): String? =
            fragment.getString(R.string.no_permission)

    fun canBlockUser(): Boolean = hasPrivilegedRole() && signedInUser != model.user

    public override fun fetch(): Flowable<DiffUtil.DiffResult> {
        val source = getFunction.apply(model).map(Guest::asDifferentiables)
        return FunctionalDiff.of(source, items) { _, updated -> updated }
    }

    override fun upsert(): Single<DiffUtil.DiffResult> =
            Single.error(TeammateException("Cannot upsert"))

    public override fun delete(): Completable =
            Completable.error(TeammateException("Cannot delete"))
}
