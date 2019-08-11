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


import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.*

class BlockedUserRepo internal constructor() : TeamQueryRepo<BlockedUser>() {

    private val api: TeammateApi = TeammateService.getApiInstance()

    override fun dao(): EntityDao<in BlockedUser> = EntityDao.daDont()

    override fun createOrUpdate(model: BlockedUser): Single<BlockedUser> =
            api.blockUser(model.team.getId(), model)
                    .doOnSuccess { deleteBlockedUser(model.user, model.team) }

    override fun get(id: String): Flowable<BlockedUser> = Flowable.empty()

    override fun delete(model: BlockedUser): Single<BlockedUser> =
            api.unblockUser(model.team.getId(), model)

    override fun localModelsBefore(key: Team, pagination: Date?): Maybe<List<BlockedUser>> =
            Maybe.empty()

    override fun remoteModelsBefore(key: Team, pagination: Date?): Maybe<List<BlockedUser>> =
            api.blockedUsers(key.getId(), pagination, DEF_QUERY_LIMIT).map(saveManyFunction).toMaybe()

    override fun provideSaveManyFunction(): (List<BlockedUser>) -> List<BlockedUser> =
            { models -> models }

    private fun deleteBlockedUser(user: User, team: Team) {
        val userId = user.getId()
        val teamId = team.getId()
        val database = AppDatabase.instance
        database.roleDao().deleteUsers(userId, teamId)
        database.guestDao().deleteUsers(userId, teamId)
        database.joinRequestDao().deleteUsers(userId, teamId)
    }
}
