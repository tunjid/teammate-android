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


import android.content.Context
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.TeamSearchRequest
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.TeamDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io

class TeamRepo internal constructor() : ModelRepo<Team>() {

    private val app: App = App.instance
    private val api: TeammateApi = TeammateService.getApiInstance()
    private val teamDao: TeamDao = AppDatabase.instance.teamDao()

    val defaultTeam: Maybe<Team>
        get() {
            val preferences = app.getSharedPreferences(TEAM_REPOSITORY_KEY, Context.MODE_PRIVATE)
            val defaultTeamId = preferences.getString(DEFAULT_TEAM, "")

            return if (defaultTeamId.isNullOrBlank()) Maybe.empty() else teamDao.get(defaultTeamId).subscribeOn(io())
        }

    override fun dao(): EntityDao<in Team> = teamDao

    override fun createOrUpdate(model: Team): Single<Team> {
        var teamSingle = when {
            model.isEmpty -> api.createTeam(model).map(getLocalUpdateFunction(model))
                    .flatMap<List<Role>> { RepoProvider.forRepo(RoleRepo::class.java).myRoles.lastOrError() }
                    .map { model }

            else -> api.updateTeam(model.id, model).map(getLocalUpdateFunction(model))
                    .doOnError { throwable -> deleteInvalidModel(model, throwable) }
        }

        val body = getBody(model.headerItem.getValue(), Team.PHOTO_UPLOAD_KEY)
        if (body != null) teamSingle = teamSingle
                .flatMap { api.uploadTeamLogo(model.id, body).map(getLocalUpdateFunction(model)) }

        return teamSingle.map(saveFunction)
    }

    override fun get(id: String): Flowable<Team> {
        val local = teamDao.get(id).subscribeOn(io())
        val remote = api.getTeam(id).map(saveFunction).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Team): Single<Team> =
            api.deleteTeam(model.id)
                    .map { this.deleteLocally(it) }
                    .doOnError { throwable -> deleteInvalidModel(model, throwable) }

    override fun provideSaveManyFunction(): (List<Team>) -> List<Team> = { models ->
        teamDao.upsert(models)
        models
    }

    fun findTeams(request: TeamSearchRequest): Single<List<Team>> =
            api.findTeam(request.name, request.screenName, request.sport)

    fun saveDefaultTeam(team: Team) {
        val preferences = app.getSharedPreferences(TEAM_REPOSITORY_KEY, Context.MODE_PRIVATE)
        preferences.edit().putString(DEFAULT_TEAM, team.id).apply()
    }

    @Suppress("unused")
    private fun clearStaleTeamMembers(team: Team) {
        // Clear stale join requests and roles because the api version has the latest
        val database = AppDatabase.instance
        database.roleDao().deleteByTeam(team.id)
        database.joinRequestDao().deleteByTeam(team.id)
    }

    companion object {

        private const val TEAM_REPOSITORY_KEY = "TeamRepository"
        private const val DEFAULT_TEAM = "default.team"
    }
}
