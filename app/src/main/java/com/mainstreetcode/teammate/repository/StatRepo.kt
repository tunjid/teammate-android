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


import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.StatDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class StatRepo internal constructor() : QueryRepo<Stat, Game, Date>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val statDao: StatDao = AppDatabase.instance.statDao()

    override fun dao(): EntityDao<in Stat> = statDao

    override fun createOrUpdate(model: Stat): Single<Stat> = when {
        model.isEmpty -> api.createStat(model.game.id, model).map(getLocalUpdateFunction(model))
        else -> api.updateStat(model.id, model)
                .map(getLocalUpdateFunction(model))
                .doOnError { throwable -> deleteInvalidModel(model, throwable) }
    }.map(saveFunction)

    override fun get(id: String): Flowable<Stat> {
        val local = statDao.get(id).subscribeOn(io())
        val remote = api.getStat(id).subscribeOn(io()).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Stat): Single<Stat> {
        return api.deleteStat(model.id)
                .map { this.deleteLocally(it) }
                .doOnError { throwable -> deleteInvalidModel(model, throwable) }
    }

    override fun localModelsBefore(key: Game, pagination: Date?): Maybe<List<Stat>> {
        var date = pagination
        if (date == null) date = futureDate
        return statDao.getStats(key.id, date, DEF_QUERY_LIMIT).subscribeOn(io())
    }

    override fun remoteModelsBefore(key: Game, pagination: Date?): Maybe<List<Stat>> =
            api.getStats(key.id, pagination, DEF_QUERY_LIMIT).map(saveManyFunction)
                    .doOnSuccess { stats -> for (stat in stats) stat.game.update(key) }
                    .toMaybe()

    override fun provideSaveManyFunction(): (List<Stat>) -> List<Stat> = { models ->
        val users = ArrayList<User>(models.size)
        val teams = ArrayList<Team>(models.size)
        val games = ArrayList<Game>(models.size)

        for (stat in models) {
            users.add(stat.user)
            teams.add(stat.team)
            games.add(stat.game)
        }

        RepoProvider.forModel(User::class.java).saveAsNested().invoke(users)
        RepoProvider.forModel(Team::class.java).saveAsNested().invoke(teams)
        RepoProvider.forModel(Game::class.java).saveAsNested().invoke(games)
        statDao.upsert(models)

        models
    }
}
