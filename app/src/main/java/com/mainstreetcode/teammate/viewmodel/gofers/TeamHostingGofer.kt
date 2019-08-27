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

package com.mainstreetcode.teammate.viewmodel.gofers

import com.mainstreetcode.teammate.model.ListableModel
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.TeamHost
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.RoleRepo
import com.mainstreetcode.teammate.repository.UserRepo
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

/**
 * Interface for liaisons between a ViewModel and a single instance of it's Model
 */
abstract class TeamHostingGofer<T> internal constructor(
        model: T,
        onError: (Throwable) -> Unit
) : Gofer<T>(model, onError) where T: TeamHost, T : Model<T>, T : ListableModel<T> {

    private val currentRole: Role = Role.empty()
    private val userRepository: UserRepo = RepoProvider.forRepo(UserRepo::class.java)
    private val roleRepository: RoleRepo = RepoProvider.forRepo(RoleRepo::class.java)

    internal val signedInUser: User
        get() = userRepository.currentUser

    init {
        startPrep()
    }

     override fun changeEmitter(): Flowable<Boolean> =
             roleRepository.getRoleInTeam(userRepository.currentUser.id, model.team.id)
                     .map(this::onRoleFound).observeOn(mainThread())

    fun hasRole(): Boolean = !currentRole.isEmpty

    fun hasPrivilegedRole(): Boolean = currentRole.isPrivilegedRole

    private fun onRoleFound(foundRole: Role): Boolean {
        val changed = currentRole.position != foundRole.position
        currentRole.update(foundRole)
        return changed
    }
}
