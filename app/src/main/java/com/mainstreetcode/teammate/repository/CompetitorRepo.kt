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
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.CompetitorDao
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.TeammateException
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class CompetitorRepo internal constructor() : QueryRepo<Competitor, Tournament, Int>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val competitorDao: CompetitorDao = AppDatabase.instance.competitorDao()

    override fun dao(): EntityDao<in Competitor> = competitorDao

    override fun createOrUpdate(model: Competitor): Single<Competitor> = when {
        model.isEmpty -> Single.error(TeammateException(""))
        else -> api.updateCompetitor(model.id, model)
                .map(getLocalUpdateFunction(model))
                .doOnError { throwable -> deleteInvalidModel(model, throwable) }
    }.map(saveFunction)

    override fun get(id: String): Flowable<Competitor> {
        val local = competitorDao.get(id).subscribeOn(io())
        val remote = api.getCompetitor(id).map(saveFunction).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Competitor): Single<Competitor> =
            Single.error(TeammateException(""))

    fun getDeclined(date: Date?): Single<List<Competitor>> =
            api.getDeclinedCompetitors(date, DEF_QUERY_LIMIT).map(saveManyFunction)

    override fun localModelsBefore(key: Tournament, pagination: Int?): Maybe<List<Competitor>> =
            competitorDao.getCompetitors(key.id).subscribeOn(io())

    override fun remoteModelsBefore(key: Tournament, pagination: Int?): Maybe<List<Competitor>> =
            api.getCompetitors(key.id).map(saveManyFunction).toMaybe()

    override fun provideSaveManyFunction(): (List<Competitor>) -> List<Competitor> = { models ->
        val teams = ArrayList<Team>(models.size)
        val users = ArrayList<User>(models.size)
        val games = ArrayList<Game>(models.size)

        for (competitor in models) {
            val game = competitor.game
            val entity = competitor.entity

            if (entity is Team) teams.add(entity)
            else if (entity is User) users.add(entity)

            if (!game.isEmpty) games.add(game)
        }

        RepoProvider.forModel(User::class.java).saveAsNested().invoke((users))
        RepoProvider.forModel(Team::class.java).saveAsNested().invoke((teams))
        RepoProvider.forModel(Game::class.java).saveAsNested().invoke((games))
        competitorDao.upsert(models)

        models
    }
}
