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

import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
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

class TeamMemberViewModel : TeamMappedViewModel<TeamMember>() {

    private val repo: TeamMemberRepo = RepoProvider.forRepo(TeamMemberRepo::class.java)

    val allUsers: List<User>
        get() = allModels.filterIsInstance<UserHost>()
                .map(UserHost::user)
                .distinct()

    override fun sortsAscending(): Boolean = true

    override fun valueClass(): Class<TeamMember> = TeamMember::class.java

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

    override fun fetch(key: Team, fetchLatest: Boolean): Flowable<List<TeamMember>> =
            repo.modelsBefore(key, getQueryDate(fetchLatest, key, TeamMember::created))

    fun gofer(joinRequest: JoinRequest): JoinRequestGofer = JoinRequestGofer(
            joinRequest,
            onError(joinRequest.toTeamMember()),
            { RepoProvider.forModel(JoinRequest::class.java)[it] },
            this::processRequest)

    fun gofer(role: Role): RoleGofer = RoleGofer(
            role,
            onError(role.toTeamMember()),
            { RepoProvider.forRepo(RoleRepo::class.java)[it] },
            this::updateRole,
            this::deleteRole
    )

    private fun deleteRole(role: Role): Single<Role> =
            repo.delete(role.toTeamMember())
                    .doOnSuccess { getModelList(role.team).remove(it) }
                    .map { role }

    private fun updateRole(role: Role): Single<Role> =
            repo.createOrUpdate(role.toTeamMember()).map { role }

    private fun processRequest(request: JoinRequest, approved: Boolean): Single<JoinRequest> = when {
        approved -> repo.createOrUpdate(request.toTeamMember())
        else -> repo.delete(request.toTeamMember())
    }
            .doOnSuccess { onRequestProcessed(request, approved, request.team, it) }
            .map { request }

    private fun onRequestProcessed(request: JoinRequest, approved: Boolean, team: Team, processedMember: Differentiable) {
        pushModelAlert(Alert.requestProcessed(request))
        val list = getModelList(team)
        list.remove(request.toTeamMember())
        if (approved) list.add(processedMember)
    }

    private fun removeBlockedUser(blockedUser: BlockedUser) {
        val iterator = getModelList(blockedUser.team).iterator()

        while (iterator.hasNext()) {
            val identifiable = iterator.next() as? TeamMember ?: continue

            if (identifiable.user == blockedUser.user) iterator.remove()
        }
    }

    private fun filterJoinedMembers(source: MutableList<Differentiable>) {
        val userIds = mutableSetOf<String>()
        val iterator = source.listIterator(source.size)

        while (iterator.hasPrevious()) {
            val member: TeamMember = iterator.previous() as? TeamMember ?: continue

            val item = member.wrappedModel as Differentiable

            if (item is Role) userIds.add(member.user.getId())
            else if (item is JoinRequest) if (userIds.contains(member.user.getId())) iterator.remove()
        }
    }
}
