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
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.ModelUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class TournamentGofer @SuppressLint("CheckResult")
constructor(
        model: Tournament,
        onError: (Throwable) -> Unit,
        private val getFunction: (Tournament) -> Flowable<Tournament>,
        private val updateFunction: (Tournament) -> Single<Tournament>,
        private val deleteFunction: (Tournament) -> Single<Tournament>,
        private val competitorsFunction: (Tournament) -> Flowable<List<Competitor>>
) : TeamHostingGofer<Tournament>(model, onError) {

    private val state: Int

    init {
        items.addAll(model.asItems())
        state = if (model.isEmpty) CREATING else EDITING
    }

    fun canEditBeforeCreation(): Boolean = canEditAfterCreation() && model.isEmpty

    fun canEditAfterCreation(): Boolean = state == CREATING || hasPrivilegedRole()

    fun getToolbarTitle(fragment: Fragment): String = when {
        model.isEmpty -> fragment.getString(R.string.create_tournament)
        else -> fragment.getString(R.string.edit_tournament, model.name)
    }

    override fun getImageClickMessage(fragment: Fragment): String? = when {
        state == CREATING -> fragment.getString(R.string.create_tournament_first)
        !hasPrivilegedRole() -> fragment.getString(R.string.no_permission)
        else -> null
    }

    override fun fetch(): Flowable<DiffUtil.DiffResult> {
        val eventFlowable = getFunction.invoke(model).map(Tournament::asDifferentiables)
        val competitorsFlowable = competitorsFunction.invoke(model).map(ModelUtils::asDifferentiables)
        val sourceFlowable = Flowable.mergeDelayError(eventFlowable, competitorsFlowable)
        return FunctionalDiff.of(sourceFlowable, items, this::preserveItems)
    }

    override fun upsert(): Single<DiffUtil.DiffResult> =
            FunctionalDiff.of(updateFunction.invoke(model).map(Tournament::asDifferentiables), items, this::preserveItems)

    override fun delete(): Completable = deleteFunction.invoke(model).ignoreElement()

    companion object {

        private const val CREATING = 0
        private const val EDITING = 1
    }
}
