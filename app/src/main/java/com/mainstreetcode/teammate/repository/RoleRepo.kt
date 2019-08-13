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

import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.RoleDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class RoleRepo internal constructor() : ModelRepo<Role>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val roleDao: RoleDao = AppDatabase.instance.roleDao()

    val myRoles: Flowable<List<Role>>
        get() {
            val userId = RepoProvider.forRepo(UserRepo::class.java).currentUser.id
            val local = roleDao.userRoles(userId).subscribeOn(io())
            val remote = api.myRoles.map(saveManyFunction).toMaybe()

            return fetchThenGet(local, remote)
        }

    override fun dao(): EntityDao<in Role> = roleDao

    override fun createOrUpdate(model: Role): Single<Role> {
        var roleSingle = api.updateRole(model.id, model)
                .map(getLocalUpdateFunction(model))
                .doOnError { throwable -> deleteInvalidModel(model, throwable) }

        val body = getBody(model.headerItem.getValue(), Role.PHOTO_UPLOAD_KEY)
        if (body != null) roleSingle = roleSingle
                .flatMap { api.uploadRolePhoto(model.id, body).map(getLocalUpdateFunction(model)) }

        return roleSingle.map(getLocalUpdateFunction(model)).map(saveFunction)
    }

    override fun get(id: String): Flowable<Role> {
        val local = roleDao.get(id).subscribeOn(io())
        val remote = api.getRole(id).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Role): Single<Role> =
            api.deleteRole(model.id)
                    .map(this::deleteLocally)
                    .doOnError { throwable -> deleteInvalidModel(model, throwable) }

    override fun provideSaveManyFunction(): (List<Role>) -> List<Role> = { models ->
        val size = models.size
        val teams = ArrayList<Team>(size)
        val users = ArrayList<User>(size)

        for (i in 0 until size) {
            val role = models[i]
            users.add(role.user)
            teams.add(role.team)
        }

        if (teams.isNotEmpty()) RepoProvider.forModel(Team::class.java).saveAsNested().invoke((teams))
        if (users.isNotEmpty()) RepoProvider.forModel(User::class.java).saveAsNested().invoke((users))

        roleDao.upsert(models)

        models
    }

    fun getRoleInTeam(userId: String, teamId: String): Flowable<Role> =
            roleDao.getRoleInTeam(userId, teamId).subscribeOn(io())
                    .flatMapPublisher { role: Role ->
                        Maybe.concatDelayError(listOf(Maybe.just(role), api.getRole(role.id).toMaybe()))
                    }
}
