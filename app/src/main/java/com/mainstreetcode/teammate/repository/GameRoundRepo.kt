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
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.GameDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

import io.reactivex.schedulers.Schedulers.io

class GameRoundRepo internal constructor() : QueryRepo<Game, Tournament, Int>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val gameDao: GameDao = AppDatabase.getInstance().gameDao()

    override fun dao(): EntityDao<in Game> = gameDao

    override fun createOrUpdate(model: Game): Single<Game> =
            RepoProvider.forRepo(GameRepo::class.java).createOrUpdate(model)

    override fun get(id: String): Flowable<Game> =
            RepoProvider.forRepo(GameRepo::class.java)[id]

    override fun delete(model: Game): Single<Game> =
            RepoProvider.forRepo(GameRepo::class.java).delete(model)

    override fun localModelsBefore(key: Tournament, pagination: Int?): Maybe<List<Game>> {
        var round = pagination
        if (round == null) round = 0
        return gameDao.getGames(key.id, round, 30).subscribeOn(io())
    }

    override fun remoteModelsBefore(key: Tournament, pagination: Int?): Maybe<List<Game>> =
            api.getGamesForRound(key.id, pagination ?: 0, 30).map(saveManyFunction).toMaybe()

    override fun provideSaveManyFunction(): (List<Game>) -> List<Game> =
            { list -> RepoProvider.forRepo(GameRepo::class.java).provideSaveManyFunction().invoke(list) }
}
