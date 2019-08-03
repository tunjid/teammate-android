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
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.TeammateException
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class StatGofer(
        model: Stat,
        onError: Consumer<Throwable>,
        private val teamUserFunction: Function<Team, User>,
        private val getFunction: Function<Stat, Flowable<Stat>>,
        private val upsertFunction: Function<Stat, Single<Stat>>,
        private val deleteFunction: Function<Stat, Single<Stat>>,
        private val eligibleTeamSource: Function<Stat, Flowable<Team>>) : Gofer<Stat>(model, onError) {

    private val eligibleTeams: MutableList<Team> = mutableListOf()


    init {
        items.addAll(model.asItems())
        items.add(model.team)
        items.add(model.user)
    }

    fun canEdit(): Boolean = eligibleTeams.isNotEmpty()

    override fun changeEmitter(): Flowable<Boolean> {
        val count = eligibleTeams.size
        eligibleTeams.clear()
        return eligibleTeamSource.apply(model)
                .toList()
                .map { eligibleTeams.addAll(it); eligibleTeams }
                .flatMapPublisher(this::updateDefaultTeam)
                .map { count != eligibleTeams.size }
    }

    override fun getImageClickMessage(fragment: Fragment): String? = null

    public override fun fetch(): Flowable<DiffUtil.DiffResult> {
        val source = getFunction.apply(model).map(Stat::asDifferentiables)
        return FunctionalDiff.of(source, items, this::preserveItems)
    }

    override fun upsert(): Single<DiffUtil.DiffResult> {
        val source = upsertFunction.apply(model).map(Stat::asDifferentiables)
        return FunctionalDiff.of(source, items, this::preserveItems)
    }

    fun chooseUser(otherUser: User): Single<DiffUtil.DiffResult> =
            swap(otherUser, { model.user }, User::update)

    fun switchTeams(): Flowable<DiffUtil.DiffResult> {
        if (eligibleTeams.size <= 1)
            return Flowable.error(TeammateException(App.getInstance().getString(R.string.stat_only_team)))

        val toSwap = if (eligibleTeams[0] == model.team) eligibleTeams[1] else eligibleTeams[0]
        return swap(toSwap, { model.team }, Team::update)
                .concatWith(updateDefaultUser())
    }

    public override fun delete(): Completable = deleteFunction.apply(model).ignoreElement()

    private fun updateDefaultTeam(teams: List<Team>): Flowable<DiffUtil.DiffResult> {
        val hasNoDefaultTeam = !model.team.isEmpty || teams.isEmpty()
        if (hasNoDefaultTeam) return Flowable.empty()

        val toSwap = teams[0]
        return swap(toSwap, { model.team }, Team::update).concatWith(updateDefaultUser())
    }

    private fun updateDefaultUser(): Single<DiffUtil.DiffResult> =
            chooseUser(teamUserFunction.apply(model.team))

    private fun <T : Differentiable> swap(item: Differentiable,
                                          swapDestination: () -> T,
                                          onSwapComplete: (T, T) -> Unit): Single<DiffUtil.DiffResult> {

        val cache = AtomicReference<T>()
        val swapSource = Single.just(listOf(item))
        return FunctionalDiff.of(swapSource, items) { sourceCopy, fetched ->
            @Suppress("UNCHECKED_CAST") val toSwap = fetched[0] as T

            sourceCopy.remove(swapDestination.invoke())
            sourceCopy.add(toSwap)
            cache.set(toSwap)

            Collections.sort(sourceCopy, FunctionalDiff.COMPARATOR)
            sourceCopy
        }.doOnSuccess { onSwapComplete.invoke(swapDestination.invoke(), cache.get()) }
    }
}
