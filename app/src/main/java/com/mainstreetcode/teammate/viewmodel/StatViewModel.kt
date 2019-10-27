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

package com.mainstreetcode.teammate.viewmodel

import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.model.StatAggregate
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.StatRepo
import com.mainstreetcode.teammate.repository.UserRepo
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.replaceList
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.gofers.StatGofer
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.*

/**
 * ViewModel for [tournaments][Stat]
 */

class StatViewModel : MappedViewModel<Game, Stat>() {

    private val api = TeammateService.getApiInstance()

    private val modelListMap = HashMap<String, MutableList<Differentiable>>()
    val statAggregates: MutableList<Differentiable> = ArrayList()

    private val repository: StatRepo = RepoProvider.forRepo(StatRepo::class.java)

    fun gofer(stat: Stat): StatGofer {
        val onError = { throwable: Throwable -> checkForInvalidObject(throwable, stat, stat.game) }
        val userFunction = { _: Team -> RepoProvider.forRepo(UserRepo::class.java).currentUser }
        val eligibleTeamSource = { _: Stat -> GameViewModel.getEligibleTeamsForGame(stat.game) }
        return StatGofer(
                stat,
                onError,
                userFunction,
                repository::get,
                repository::createOrUpdate,
                this::delete,
                eligibleTeamSource)
    }

    override fun valueClass(): Class<Stat> = Stat::class.java

    override fun fetch(key: Game, fetchLatest: Boolean): Flowable<List<Stat>> =
            repository.modelsBefore(key, getQueryDate(fetchLatest, key) { it.created })

    override fun getModelList(key: Game): MutableList<Differentiable> =
            modelListMap.getOrPut(key.id) { mutableListOf() }

    override fun onInvalidKey(key: Game) {
        super.onInvalidKey(key)
        pushModelAlert(Alert.deletion(key))
    }

    fun delete(stat: Stat): Single<Stat> =
            repository.delete(stat).doOnSuccess { deleted -> getModelList(stat.game).remove(deleted) }

    fun canEditGameStats(game: Game): Single<Boolean> =
            isPrivilegedInGame(game).map { isPrivileged -> isPrivilegedOrIsReferee(isPrivileged, RepoProvider.forRepo(UserRepo::class.java).currentUser, game) }

    fun isPrivilegedInGame(game: Game): Single<Boolean> = when {
        game.betweenUsers() -> Single.just(game.isCompeting(RepoProvider.forRepo(UserRepo::class.java).currentUser))
        else -> GameViewModel.getEligibleTeamsForGame(game).count().map { value -> value > 0 }
    }

    fun aggregate(request: StatAggregate.Request): Single<DiffUtil.DiffResult> = FunctionalDiff.of(
            api.statsAggregate(request).map(StatAggregate.Result::getAggregates),
            statAggregates,
            ::replaceList
    ).observeOn(mainThread())

    private fun isPrivilegedOrIsReferee(isPrivileged: Boolean, current: User, game: Game): Boolean {
        val referee = game.referee
        return (if (referee.isEmpty) isPrivileged else current == referee) && !game.competitorsNotAccepted()
    }
}
