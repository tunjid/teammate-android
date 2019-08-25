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
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.Standings
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.enums.StatType
import com.mainstreetcode.teammate.repository.CompetitorRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.TournamentRepo
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.asDifferentiables
import com.mainstreetcode.teammate.util.replaceList
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.gofers.TournamentGofer
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.*

/**
 * ViewModel for [tournaments][Tournament]
 */

class TournamentViewModel : TeamMappedViewModel<Tournament>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val repository: TournamentRepo = RepoProvider.forRepo(TournamentRepo::class.java)
    private val standingsMap = HashMap<Tournament, Standings>()
    private val ranksMap = HashMap<Tournament, List<Differentiable>>()

    fun gofer(tournament: Tournament): TournamentGofer {
        return TournamentGofer(
                tournament,
                onError(tournament),
                this::getTournament,
                this::createOrUpdateTournament,
                this::delete,
                { RepoProvider.forRepo(CompetitorRepo::class.java).modelsBefore(tournament, 0) })
    }

    override fun valueClass(): Class<Tournament> = Tournament::class.java

    fun addCompetitors(tournament: Tournament, competitors: List<Competitor>): Single<Tournament> =
            repository.addCompetitors(tournament, competitors).observeOn(mainThread())

    override fun fetch(key: Team, fetchLatest: Boolean): Flowable<List<Tournament>> =
            repository.modelsBefore(key, getQueryDate(fetchLatest, key, Tournament::created))

    override fun onErrorMessage(message: Message, key: Team, invalid: Differentiable) {
        super.onErrorMessage(message, key, invalid)
        val shouldRemove = message.isInvalidObject && invalid is Tournament
        if (shouldRemove) removeTournament(invalid as Tournament)
    }

    fun getStandings(tournament: Tournament): Standings =
            standingsMap.getOrPut(tournament) { Standings.forTournament(tournament) }

    fun getStatRanks(tournament: Tournament): List<Differentiable> =
            ranksMap.getOrPut(tournament) { mutableListOf() }

    fun fetchStandings(tournament: Tournament): Completable = api.getStandings(tournament.id)
            .observeOn(mainThread()).map<Standings>(getStandings(tournament)::update).ignoreElement()

    fun checkForWinner(tournament: Tournament): Flowable<Boolean> =
            if (tournament.isEmpty) Flowable.empty() else repository[tournament]
                    .map(Tournament::hasWinner).observeOn(mainThread())

    fun delete(tournament: Tournament): Single<Tournament> =
            repository.delete(tournament).doOnSuccess(this::removeTournament)

    fun getStatRank(tournament: Tournament, type: StatType): Single<DiffUtil.DiffResult> =
            FunctionalDiff.of(
                    api.getStatRanks(tournament.id, type)
                            .map( ::asDifferentiables),
                    getStatRanks(tournament),
                    ::replaceList
            ).observeOn(mainThread())

    private fun getTournament(tournament: Tournament): Flowable<Tournament> =
            if (tournament.isEmpty) Flowable.empty() else repository[tournament]

    private fun createOrUpdateTournament(tournament: Tournament): Single<Tournament> =
            repository.createOrUpdate(tournament)

    private fun removeTournament(tournament: Tournament) {
        for (list in modelListMap.values) list.remove(tournament)
        pushModelAlert(Alert.deletion(tournament))
    }
}
