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

package com.mainstreetcode.teammate.repository


import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.TournamentDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class TournamentRepo internal constructor() : TeamQueryRepo<Tournament>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val tournamentDao: TournamentDao = AppDatabase.instance.tournamentDao()

    override fun dao(): EntityDao<in Tournament> = tournamentDao

    fun addCompetitors(tournament: Tournament, competitors: List<Competitor>): Single<Tournament> =
            api.addCompetitors(tournament.id, competitors)
                    .map(getLocalUpdateFunction(tournament))
                    .map(saveFunction)

    override fun createOrUpdate(model: Tournament): Single<Tournament> {
        var tournamentSingle = when {
            model.isEmpty -> api.createTournament(model.host.id, model).map(getLocalUpdateFunction(model))
            else -> api.updateTournament(model.id, model)
                    .map(getLocalUpdateFunction(model))
                    .doOnError { throwable -> deleteInvalidModel(model, throwable) }
        }

        val body = getBody(model.headerItem.getValue(), Tournament.PHOTO_UPLOAD_KEY)
        if (body != null) tournamentSingle = tournamentSingle
                .flatMap { api.uploadTournamentPhoto(model.id, body) }

        return tournamentSingle.map(saveFunction)
    }

    override fun get(id: String): Flowable<Tournament> {
        val local = tournamentDao.get(id).subscribeOn(io())
        val remote = api.getTournament(id).map(saveFunction).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Tournament): Single<Tournament> = api.deleteTournament(model.id)
            .map { this.deleteLocally(it) }
            .doOnError { throwable -> deleteInvalidModel(model, throwable) }

    override fun localModelsBefore(key: Team, pagination: Date?): Maybe<List<Tournament>> {
        var date = pagination
        if (date == null) date = futureDate
        // To concatenate team to account for the way the id is stored in the db to accommodate users and teams
        val teamId = key.id
        return tournamentDao.getTournaments(teamId, date, DEF_QUERY_LIMIT).subscribeOn(io())
    }

    override fun remoteModelsBefore(key: Team, pagination: Date?): Maybe<List<Tournament>> =
            api.getTournaments(key.id, pagination, DEF_QUERY_LIMIT).map(saveManyFunction).toMaybe()

    override fun provideSaveManyFunction(): (List<Tournament>) -> List<Tournament> = { models ->
        val size = models.size
        val users = ArrayList<User>(size)
        val teams = ArrayList<Team>(size)
        val competitors = ArrayList<Competitor>(size)

        for (tournament in models) {
            teams.add(tournament.team)
            val competitor = tournament.winner

            if (competitor.isEmpty) continue
            competitors.add(competitor)

            val competitive = competitor.entity
            if (competitive.isEmpty) continue

            if (competitive is User) users.add(competitive)
            if (competitive is Team) teams.add(competitive)
        }

        RepoProvider.forModel(User::class.java).saveAsNested().invoke((users))
        RepoProvider.forModel(Team::class.java).saveAsNested().invoke((teams))

        tournamentDao.upsert(models)
        RepoProvider.forModel(Competitor::class.java).saveAsNested().invoke((competitors))

        models
    }

    override fun deleteLocally(model: Tournament): Tournament {
        tournamentDao.deleteTournamentEvents(model.id)
        return super.deleteLocally(model)
    }
}
