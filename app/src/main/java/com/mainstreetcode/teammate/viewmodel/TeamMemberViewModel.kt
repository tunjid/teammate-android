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

import android.annotation.SuppressLint
import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.TeamHost
import com.mainstreetcode.teammate.model.TeamMember
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.UserHost
import com.mainstreetcode.teammate.model.toTeamMember
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.RoleRepo
import com.mainstreetcode.teammate.repository.TeamMemberRepo
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.events.matches
import com.mainstreetcode.teammate.viewmodel.gofers.JoinRequestGofer
import com.mainstreetcode.teammate.viewmodel.gofers.RoleGofer
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Single

class TeamMemberViewModel : TeamMappedViewModel<TeamMember<*>>() {

//    private val repository: TeamMemberRepo<*> = RepoProvider.forRepo(TeamMemberRepo::class.java)

    val allUsers: List<User>
        get() = allModels.filterIsInstance<UserHost>()
                .map(UserHost::getUser)
                .distinct()

    override fun sortsAscending(): Boolean = true

    override fun valueClass(): Class<TeamMember<*>> = TeamMember::class.java

    @SuppressLint("CheckResult")
    override fun onModelAlert(alert: Alert<*>) {
        super.onModelAlert(alert)

        alert.matches(Alert.of(Alert.Creation::class.java, BlockedUser::class.java, this::removeBlockedUser))
    }

    override fun afterPullToRefreshDiff(source: MutableList<Differentiable>) {
        super.afterPullToRefreshDiff(source)
        filterJoinedMembers(source)
    }

    override fun afterPreserveListDiff(source: MutableList<Differentiable>) {
        super.afterPreserveListDiff(source)
        filterJoinedMembers(source)
    }

    override fun fetch(key: Team, fetchLatest: Boolean): Flowable<List<TeamMember<*>>> {
        val  p = RepoProvider.forRepo(TeamMemberRepo::class.java)
        val  l = RepoProvider.forModel(TeamMember::class.java)

        val repo = RepoProvider.forRepo(TeamMemberRepo::class.java)
        return repo.modelsBefore(key, getQueryDate(fetchLatest, key, { it.getCreated() }))
    }

    fun gofer(joinRequest: JoinRequest): JoinRequestGofer = JoinRequestGofer(
            joinRequest,
            onError(joinRequest.toTeamMember()),
            { RepoProvider.forModel(it)[it] },
            this::processRequest)

    fun gofer(role: Role): RoleGofer = RoleGofer(
            role,
            onError(role.toTeamMember()),
            { RepoProvider.forRepo(RoleRepo::class.java)[it] },
            this::updateRole,
            this::deleteRole
    )

    private fun deleteRole(role: Role): Single<Role> =
            asTypedTeamMember(role, { member, repository ->
                repository.delete(member)
                        .doOnSuccess { getModelList(role.team).remove(it) }
                        .map { role }
            })

    private fun updateRole(role: Role): Single<Role> =
            asTypedTeamMember(role, { member, repository -> repository.createOrUpdate(member).map { role } })

    private fun processRequest(request: JoinRequest, approved: Boolean): Single<JoinRequest> = asTypedTeamMember(request, { member, repository ->
        when {
            approved -> repository.createOrUpdate(member)
            else -> repository.delete(member)
        }
                .doOnSuccess { processedMember -> onRequestProcessed(request, approved, request.team, processedMember) }
                .map { request }
    })

    private fun onRequestProcessed(request: JoinRequest, approved: Boolean, team: Team, processedMember: Differentiable) {
        pushModelAlert(Alert.requestProcessed(request))
        val list = getModelList(team)
        list.remove(request.toTeamMember())
        if (approved) list.add(processedMember)
    }

    private fun <S> repository(): TeamMemberRepo<S> where S : UserHost, S : TeamHost, S : Model<S> =
            RepoProvider.forRepo(TeamMemberRepo::class.java) as TeamMemberRepo<S>

    private fun <T, S> asTypedTeamMember(model: T, function: (TeamMember<T>, TeamMemberRepo<T>) -> S): S
            where T : UserHost, T : TeamHost, T : Model<T> {
        try {
            return function.invoke(model.toTeamMember(), repository())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun removeBlockedUser(blockedUser: BlockedUser) {
        val iterator = getModelList(blockedUser.team).iterator()

        while (iterator.hasNext()) {
            val identifiable = iterator.next() as? TeamMember<*> ?: continue

            if (identifiable.user == blockedUser.user) iterator.remove()
        }
    }

    private fun filterJoinedMembers(source: MutableList<Differentiable>) {
        val userIds = mutableSetOf<String>()
        val iterator = source.listIterator(source.size)

        while (iterator.hasPrevious()) {
            val member: TeamMember<*> = iterator.previous() as? TeamMember<*> ?: continue

            val item = member.wrappedModel as Differentiable

            if (item is Role) userIds.add(member.user.id)
            else if (item is JoinRequest) if (userIds.contains(member.user.id)) iterator.remove()
        }
    }
}
