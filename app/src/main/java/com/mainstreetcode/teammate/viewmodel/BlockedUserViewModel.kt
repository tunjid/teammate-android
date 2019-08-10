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
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.repository.BlockedUserRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.gofers.BlockedUserGofer

import io.reactivex.Flowable
import io.reactivex.Single

class BlockedUserViewModel : TeamMappedViewModel<BlockedUser>() {

    private val repository: BlockedUserRepo = RepoProvider.forRepo(BlockedUserRepo::class.java)

    fun gofer(blockedUser: BlockedUser): BlockedUserGofer =
            BlockedUserGofer(blockedUser, onError(blockedUser), this::unblockUser)

    override fun valueClass(): Class<BlockedUser> = BlockedUser::class.java

    override fun hasNativeAds(): Boolean = false

    override fun fetch(key: Team, fetchLatest: Boolean): Flowable<List<BlockedUser>> =
            repository.modelsBefore(key, getQueryDate(fetchLatest, key, BlockedUser::getCreated))

    fun blockUser(blockedUser: BlockedUser): Single<BlockedUser> {
        return RepoProvider.forRepo(BlockedUserRepo::class.java).createOrUpdate(blockedUser)
                .doOnSuccess { pushModelAlert(Alert.creation(blockedUser)) }
    }

    private fun unblockUser(blockedUser: BlockedUser): Single<BlockedUser> =
            repository.delete(blockedUser).doOnSuccess { getModelList(blockedUser.team).remove(it) }
}
