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
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.TeamHost
import com.mainstreetcode.teammate.model.TeamMember
import com.mainstreetcode.teammate.model.UserHost
import com.mainstreetcode.teammate.model.toTeamMember
import com.mainstreetcode.teammate.model.unsafeCast
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.TeamMemberDao
import com.mainstreetcode.teammate.repository.RepoProvider.Companion.forModel
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.TeammateException
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Maybes
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class TeamMemberRepo<T> internal constructor() : TeamQueryRepo<TeamMember<T>>() where T : UserHost, T : TeamHost, T : Model<T> {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val dao: TeamMemberDao = AppDatabase.getInstance().teamMemberDao()

    override fun dao(): EntityDao<in TeamMember<*>> = dao

    override fun createOrUpdate(model: TeamMember<T>): Single<TeamMember<T>> = when (val wrapped = model.wrappedModel) {
        is Role -> forModel(Role::class.java).createOrUpdate(wrapped).unsafeCast()
        is JoinRequest ->
            if (wrapped.isEmpty) createJoinRequest(wrapped)
            else createRole(wrapped)
        else -> Single.error(TeammateException("Unimplemented"))
    }.map(getLocalUpdateFunction(model))

    override fun get(id: String): Flowable<TeamMember<T>> =
            Flowable.error(IllegalArgumentException("Unimplementable"))

    override fun delete(model: TeamMember<T>): Single<TeamMember<T>> = when (val wrapped = model.wrappedModel) {
        is Role -> forModel(Role::class.java).delete(wrapped).unsafeCast()
        is JoinRequest -> forModel(JoinRequest::class.java).delete(wrapped).unsafeCast()
        else -> Single.error(TeammateException("Unimplemented"))
    }

    override fun localModelsBefore(key: Team, pagination: Date?): Maybe<List<TeamMember<T>>> {
        val date = pagination ?: Date()

        val database = AppDatabase.getInstance()
        val teamId = key.id

        val rolesMaybe = database.roleDao().getRoles(key.id, date, DEF_QUERY_LIMIT).defaultIfEmpty(ArrayList())
        val requestsMaybe = database.joinRequestDao().getRequests(teamId, date, DEF_QUERY_LIMIT).defaultIfEmpty(ArrayList())

        val listMaybe = Maybes.zip<List<Role>, List<JoinRequest>, List<TeamMember<*>>>(rolesMaybe, requestsMaybe, { roles, requests ->
            val result = ArrayList<TeamMember<*>>(roles.size + requests.size)

            for (role in roles) result.add(role.toTeamMember())
            for (request in requests) result.add(request.toTeamMember())

            result
        }).subscribeOn(io())

        return listMaybe.unsafeCastList()
    }

    private fun createJoinRequest(request: JoinRequest): Single<TeamMember<T>> =
            forModel(JoinRequest::class.java).createOrUpdate(request).map { request.toTeamMember().unsafeCast<T>() }

    private fun createRole(request: JoinRequest): Single<TeamMember<T>> =
            if (request.isUserApproved) approveUser(request) else acceptInvite(request)

    private fun acceptInvite(request: JoinRequest): Single<TeamMember<T>> =
            invoke(request, api.acceptInvite(request.id))

    private fun approveUser(request: JoinRequest): Single<TeamMember<T>> =
            invoke(request, api.approveUser(request.id))

    private fun invoke(request: JoinRequest, apiSingle: Single<Role>): Single<TeamMember<T>> {
        val member = request.toTeamMember().unsafeCast<T>()
        return apiSingle
                .map(forModel(Role::class.java).saveFunction)
                .doOnSuccess { AppDatabase.getInstance().joinRequestDao().delete(request) }
                .map { member }
                .doOnError { throwable -> deleteInvalidModel(member, throwable) }
    }

    override fun remoteModelsBefore(key: Team, pagination: Date?): Maybe<List<TeamMember<T>>> {
        val maybe = api.getTeamMembers(key.id, pagination, DEF_QUERY_LIMIT).toMaybe().unsafeCastList<T>()
        return maybe.map(saveManyFunction)
    }

    override fun provideSaveManyFunction(): (List<TeamMember<T>>) -> List<TeamMember<T>> = { models ->
        models.split { roles, requests ->
            deleteStaleJoinRequests(roles)

            if (requests.isNotEmpty()) forModel(JoinRequest::class.java).saveAsNested().invoke((requests))
            if (roles.isNotEmpty()) forModel(Role::class.java).saveAsNested().invoke((roles))
        }
        models
    }

    private fun deleteStaleJoinRequests(roles: List<Role>?) {
        if (roles == null) return

        val teamId = roles[0].team.id
        val userIds = arrayOfNulls<String>(roles.size)

        for (i in roles.indices) userIds[i] = roles[i].user.id

        AppDatabase.getInstance().joinRequestDao().deleteRequestsFromTeam(teamId, userIds)
    }

}

private fun <S, R> Single<R>.unsafeCast(): Single<TeamMember<S>>
        where  S : UserHost, S : TeamHost, S : Model<S>, R : UserHost, R : TeamHost, R : Model<R> =
        map { it.toTeamMember() }.map { it.unsafeCast<S>() }

private fun <S> Maybe<List<TeamMember<*>>>.unsafeCastList(): Maybe<List<TeamMember<S>>>
        where  S : UserHost, S : TeamHost, S : Model<S> =
        map { it.unsafeCastList<TeamMember<S>>() }

@Suppress("UNCHECKED_CAST")
private fun <S : Model<S>> List<TeamMember<*>>.unsafeCastList(): List<S> =
        this.map { it as S }

fun List<TeamMember<*>>.split(listBiConsumer: (List<Role>, List<JoinRequest>) -> Unit) {
    val unwrapped = map { it.wrappedModel }
    val roles = unwrapped.filterIsInstance(Role::class.java)
    val requests = unwrapped.filterIsInstance(JoinRequest::class.java)

    listBiConsumer.invoke(roles, requests)
}