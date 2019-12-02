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
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.RoleRepo
import com.mainstreetcode.teammate.util.ErrorHandler
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Maybe

/**
 * ViewModel for roles in a team
 */

class RoleViewModel @SuppressLint("CheckResult")
constructor() : MappedViewModel<Class<Role>, Role>() {

    private val roleRepository: RoleRepo = RepoProvider.forRepo(RoleRepo::class.java)

    init {
        getMore(Role::class.java).subscribe({ }, ErrorHandler.EMPTY::invoke)
    }

    override fun sortsAscending(): Boolean = true

    override fun valueClass(): Class<Role> = Role::class.java

    override fun fetch(key: Class<Role>, fetchLatest: Boolean): Flowable<List<Role>> =
            roleRepository.myRoles

    override fun getModelList(key: Class<Role>): MutableList<Differentiable> = roles

    fun hasPendingCompetitor(game: Game): Maybe<Competitor> {
        val competitor: Competitor? = when {
            game.home.hasNotResponded() -> game.home
            game.away.hasNotResponded() -> game.away
            else -> null
        }

        if (competitor == null || competitor.isEmpty || competitor.isAccepted) return Maybe.empty()

        for (identifiable in roles) {
            if (identifiable !is Role) continue
            val entity = competitor.entity
            if (matches(identifiable, entity)) return Maybe.just(competitor)
        }

        return Maybe.empty()
    }

    private fun matches(role: Role, entity: Competitive): Boolean = when (entity) {
        is User -> role.user == entity
        !is Team -> false
        else -> role.team == entity && role.isPrivilegedRole
    }

    companion object {

        internal val roles: MutableList<Differentiable> = mutableListOf()
    }

}
