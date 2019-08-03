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

import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.JoinRequestDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class JoinRequestRepo internal constructor() : ModelRepo<JoinRequest>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val joinRequestDao: JoinRequestDao = AppDatabase.getInstance().joinRequestDao()

    override fun dao(): EntityDao<in JoinRequest> = joinRequestDao

    override fun createOrUpdate(model: JoinRequest): Single<JoinRequest> {
        val call = if (model.isUserApproved) api.joinTeam(model) else api.inviteUser(model)
        return call.map(getLocalUpdateFunction(model)).map(saveFunction)
    }

    override fun get(id: String): Flowable<JoinRequest> {
        val local = joinRequestDao.get(id).subscribeOn(io())
        val remote = api.getJoinRequest(id).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: JoinRequest): Single<JoinRequest> = api.deleteJoinRequest(model.id)
            .map { this.deleteLocally(it) }
            .doOnError { throwable -> deleteInvalidModel(model, throwable) }

    override fun provideSaveManyFunction(): (List<JoinRequest>) -> List<JoinRequest> = { models ->
        val teams = ArrayList<Team>(models.size)
        val users = ArrayList<User>(models.size)

        for (request in models) {
            teams.add(request.team)
            users.add(request.user)
        }

        if (teams.isNotEmpty()) RepoProvider.forModel(Team::class.java).saveAsNested().invoke((teams))
        if (users.isNotEmpty()) RepoProvider.forModel(User::class.java).saveAsNested().invoke((users))

        joinRequestDao.upsert(models)

        models
    }
}
