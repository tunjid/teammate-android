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
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.TeamSearchRequest
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.TeamRepo
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.InstantSearch
import com.mainstreetcode.teammate.util.replaceList
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.events.matches
import com.mainstreetcode.teammate.viewmodel.gofers.TeamGofer
import com.tunjid.androidx.functions.collections.transform
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.atomic.AtomicReference


/**
 * ViewModel for team
 */

class TeamViewModel : MappedViewModel<Class<Team>, Team>() {

    private val defaultTeamRef = AtomicReference(Team.empty())

    private val disposable = CompositeDisposable()
    private val repository: TeamRepo = RepoProvider.forRepo(TeamRepo::class.java)
    private val teamChangeProcessor: PublishProcessor<Team> = PublishProcessor.create()

    val teamChangeFlowable: Flowable<Team>

    val isOnATeam: Boolean
        get() = teams.isNotEmpty() || !defaultTeamRef.get().isEmpty

    val defaultTeam: Team
        get() = defaultTeamRef.get()

    override fun onCleared() = disposable.clear().run { super.onCleared() }

    override fun valueClass(): Class<Team> = Team::class.java

    override fun getModelList(key: Class<Team>): MutableList<Differentiable> = teams

    override fun fetch(key: Class<Team>, fetchLatest: Boolean): Flowable<List<Team>> =
            Flowable.empty()

    init {
        teamChangeFlowable = repository.defaultTeam.flatMapPublisher { team ->
            updateDefaultTeam(team)
            Flowable.fromCallable<Team>(defaultTeamRef::get).concatWith(teamChangeProcessor)
        }.observeOn(mainThread())

        disposable.add(teamChangeFlowable.subscribe({}, ErrorHandler.EMPTY::invoke))
    }

    fun gofer(team: Team): TeamGofer = TeamGofer(team,
            { throwable -> checkForInvalidObject(throwable, team, Team::class.java) },
            this::getTeam,
            this::createOrUpdate,
            this::deleteTeam)

    fun instantSearch(): InstantSearch<TeamSearchRequest, Team> =
            InstantSearch(repository::findTeams) { it }

    private fun getTeam(team: Team): Flowable<Team> = repository[team].doOnNext(this::onTeamChanged)

    private fun createOrUpdate(team: Team): Single<Team> =
            repository.createOrUpdate(team).doOnSuccess(this::onTeamChanged)

    fun deleteTeam(team: Team): Single<Team> =
            repository.delete(team).doOnSuccess { deleted -> pushModelAlert(Alert.deletion(deleted)) }

    fun nonDefaultTeams(sink: MutableList<Team>): Single<DiffUtil.DiffResult> = FunctionalDiff.of(
            Flowable.fromIterable(teams)
                    .filter { item -> item is Team && item != defaultTeamRef }
                    .cast(Team::class.java)
                    .toList(), sink, ::replaceList)

    fun updateDefaultTeam(newDefault: Team) {
        val copy = Team.empty()
        copy.update(newDefault)

        defaultTeamRef.set(copy)
        repository.saveDefaultTeam(copy)
        teamChangeProcessor.onNext(copy)
    }

    private fun onTeamChanged(updated: Team) {
        val currentDefault = defaultTeamRef.get()
        if (currentDefault == updated && !currentDefault.areContentsTheSame(updated))
            updateDefaultTeam(updated)
    }

    override fun onModelAlert(alert: Alert<*>) = alert.matches(Alert.of(Alert.Deletion::class.java, Team::class.java) { team ->
        teams.remove(team)
        repository.queueForLocalDeletion(team)
        if (team == defaultTeamRef.get()) defaultTeamRef.set(Team.empty())
    })

    companion object {
        internal val teams: MutableList<Differentiable> = RoleViewModel.roles.transform(
                { item -> if (item is Role) item.team else item },
                { if (it is Team) RoleViewModel.roles.first { item -> item is Role && item.team == it } else it }
        )
    }
}
