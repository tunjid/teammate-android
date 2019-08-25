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

import android.location.Address
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.FunctionalDiff
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class TeamGofer(
        model: Team,
        onError: (Throwable) -> Unit,
        private val getFunction: (Team) -> Flowable<Team>,
        private val upsertFunction: (Team) -> Single<Team>,
        private val deleteFunction: (Team) -> Single<Team>
) : TeamHostingGofer<Team>(model, onError) {

    private var state: Int = 0
    var isSettingAddress: Boolean = false

    init {

        items.addAll(model.asItems())
        state = if (model.isEmpty) CREATING else EDITING
    }

    fun canEditTeam(): Boolean = state == CREATING || hasPrivilegedRole()

    public override fun delete(): Completable = deleteFunction.invoke(model).ignoreElement()

    override fun fetch(): Flowable<DiffUtil.DiffResult> = when {
        isSettingAddress -> Flowable.empty()
        else -> FunctionalDiff.of(getFunction.invoke(model).map(Team::asDifferentiables), items) { _, updated -> updated }
    }

    override fun upsert(): Single<DiffUtil.DiffResult> =
            FunctionalDiff.of(upsertFunction.invoke(model).map(Team::asDifferentiables), items) { _, updated -> updated }
                    .doOnSuccess { state = EDITING }

    fun setAddress(address: Address): Single<DiffUtil.DiffResult> {
        isSettingAddress = true
        model.setAddress(address)

        val source = Single.just(model.asDifferentiables())
        return FunctionalDiff.of(source, items) { _, updated -> updated }.doFinally { isSettingAddress = false }
    }

    override fun getImageClickMessage(fragment: Fragment): String? = when {
        state == CREATING -> fragment.getString(R.string.create_team_first)
        !hasPrivilegedRole() -> fragment.getString(R.string.no_permission)
        else -> null
    }

    fun getToolbarTitle(fragment: Fragment): String =
            fragment.getString(if (state == CREATING) R.string.create_team else R.string.edit_team)

    companion object {

        private const val CREATING = 0
        private const val EDITING = 1
    }
}
