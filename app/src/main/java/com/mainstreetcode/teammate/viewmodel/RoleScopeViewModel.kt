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

import androidx.lifecycle.ViewModel
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.RoleRepo
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

/**
 * ViewModel for checking a role in local contexts
 */

class RoleScopeViewModel : ViewModel() {

    private val roleMap = mutableMapOf<String, Role>()

    private val repository: RoleRepo = RepoProvider.forRepo(RoleRepo::class.java)

    fun hasPrivilegedRole(team: Team): Boolean = roleMap.getOrPut(team.id, Role.Companion::empty).isPrivilegedRole

    fun watchRoleChanges(user: User, team: Team): Flowable<Any> = matchInUserRoles(user, team)
            .map { this.checkChanged(it) }
            .filter { flag -> flag }
            .observeOn(mainThread())
            .cast(Any::class.java)

    private fun checkChanged(foundRole: Role): Boolean {
        val role = roleMap.getOrPut(foundRole.team.id, Role.Companion::empty)
        val changed = role.position != foundRole.position
        role.update(foundRole)
        return changed
    }

    private fun matchInUserRoles(user: User, team: Team): Flowable<Role> {
        val inMemory = Flowable.fromIterable(RoleViewModel.roles)
                .filter { role -> role is Role }
                .cast(Role::class.java)
                .filter { role -> user == role.user && team == role.team }

        val fromIo = repository.getRoleInTeam(user.id, team.id)

        return Flowable.concatDelayError(listOf(inMemory, fromIo))
    }
}
