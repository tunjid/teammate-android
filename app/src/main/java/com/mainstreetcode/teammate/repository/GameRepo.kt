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
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.GameDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class GameRepo internal constructor() : TeamQueryRepo<Game>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val gameDao: GameDao = AppDatabase.getInstance().gameDao()

    override fun dao(): EntityDao<in Game> = gameDao

    override fun createOrUpdate(model: Game): Single<Game> = when {
        model.isEmpty -> api.createGame(model.host.id, model).map(getLocalUpdateFunction(model))
        else -> api.updateGame(model.id, model)
                .doOnError { throwable -> deleteInvalidModel(model, throwable) }
                .map(getLocalUpdateFunction(model))
                .map(saveFunction)
    }

    override fun get(id: String): Flowable<Game> {
        val local = gameDao.get(id).subscribeOn(io())
        val remote = api.getGame(id).map(saveFunction).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Game): Single<Game> {
        return api.deleteGame(model.id)
                .map { this.deleteLocally(it) }
                .doOnError { throwable -> deleteInvalidModel(model, throwable) }
    }

    override fun localModelsBefore(key: Team, pagination: Date?): Maybe<List<Game>> {
        var date = pagination
        if (date == null) date = futureDate
        return gameDao.getGames(key.id, date, DEF_QUERY_LIMIT).subscribeOn(io())
    }

    override fun remoteModelsBefore(key: Team, pagination: Date?): Maybe<List<Game>> =
            api.getGames(key.id, pagination, DEF_QUERY_LIMIT).map(saveManyFunction).toMaybe()

    override fun provideSaveManyFunction(): (List<Game>) -> List<Game> = { models ->
        val teams = ArrayList<Team>(models.size)
        val users = ArrayList<User>(models.size)
        val events = ArrayList<Event>(models.size)
        val tournaments = ArrayList<Tournament>(models.size)
        val competitors = ArrayList<Competitor>(models.size)

        for (game in models) {
            val referee = game.referee
            val team = game.team
            val event = game.event
            val home = game.home
            val away = game.away
            val tournament = game.tournament

            if (!referee.isEmpty) users.add(referee)
            if (!team.isEmpty) teams.add(team)
            if (!event.isEmpty && !event.team.isEmpty) events.add(event)
            if (!tournament.isEmpty && !team.isEmpty) {
                tournament.updateHost(team)
                tournaments.add(tournament)
            }

            addIfValid(home, users, teams)
            addIfValid(away, users, teams)
            if (!home.isEmpty) competitors.add(home)
            if (!away.isEmpty) competitors.add(away)
        }

        RepoProvider.forModel(User::class.java).saveAsNested().invoke((users))
        RepoProvider.forModel(Team::class.java).saveAsNested().invoke((teams))
        RepoProvider.forModel(Event::class.java).saveAsNested().invoke((events))
        RepoProvider.forModel(Tournament::class.java).saveAsNested().invoke((tournaments))
        RepoProvider.forModel(Competitor::class.java).saveAsNested().invoke((competitors))

        gameDao.upsert(models)

        models
    }

    override fun deleteLocally(model: Game): Game {
        AppDatabase.getInstance().eventDao().delete(model.event)
        return super.deleteLocally(model)
    }

    private fun addIfValid(competitor: Competitor, users: MutableList<User>, teams: MutableList<Team>) {
        val entity = competitor.entity
        if (entity is User) users.add(entity)
        if (entity is Team) teams.add(entity)
    }
}
