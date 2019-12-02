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

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class GameGofer(
        model: Game,
        onError: (Throwable) -> Unit,
        private val getFunction: (Game) -> Flowable<Game>,
        private val upsertFunction: (Game) -> Single<Game>,
        private val deleteFunction: (Game) -> Single<Game>,
        private val eligibleTeamSource: (Game) -> Flowable<Team>
) : Gofer<Game>(model, onError) {

    private val eligibleTeams: MutableList<Team> = mutableListOf()

    init {
        this.items.addAll(when {
            model.isEmpty -> listOf(model.home, model.away)
            else -> model.asItems()
        })
    }

    fun canEdit(): Boolean {
        val canEdit = !model.isEnded && eligibleTeams.isNotEmpty() && !model.competitorsNotAccepted()
        return model.isEmpty || canEdit
    }

    fun canDelete(user: User): Boolean {
        if (!model.tournament.isEmpty) return false
        if (model.home.isEmpty) return true
        if (model.away.isEmpty) return true

        val entity = model.home.entity
        if (entity == user) return true
        for (team in eligibleTeams) if (entity == team) return true

        return false
    }

    override fun changeEmitter(): Flowable<Boolean> {
        val count = eligibleTeams.size
        eligibleTeams.clear()
        return eligibleTeamSource.invoke(model)
                .toList()
                .map { eligibleTeams.addAll(it); eligibleTeams }
                .map { count != it.size }
                .toFlowable()
    }

    public override fun fetch(): Flowable<DiffUtil.DiffResult> =
            FunctionalDiff.of(getFunction.invoke(model).map(Game::asDifferentiables), items, this::preserveItems)

    public override fun delete(): Completable =
            Single.defer { deleteFunction.invoke(model) }.ignoreElement()

    override fun getImageClickMessage(fragment: Fragment): String? = null

    override fun upsert(): Single<DiffUtil.DiffResult> {
        val source = upsertFunction.invoke(model).map(Game::asDifferentiables)
        return FunctionalDiff.of(source, items, this::preserveItems)
    }

    override fun preserveItems(old: List<Differentiable>, fetched: List<Differentiable>): List<Differentiable> {
        val result = super.preserveItems(old, fetched).toMutableList()
        val iterator = result.iterator()
        val filter = { item: Differentiable -> item is Competitor && item.isEmpty }

        val currentSize = result.size
        while (iterator.hasNext()) if (filter.invoke(iterator.next())) iterator.remove()

        if (currentSize == result.size || model.isEmpty) return result
        if (currentSize != model.asItems().size) return result

        result.add(model.home)
        result.add(model.away)

        return result
    }
}
