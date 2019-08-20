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

import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.TeamMember
import com.mainstreetcode.teammate.model.toTeamMember
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

class JoinRequestGofer(
        model: JoinRequest,
        onError: (Throwable) -> Unit,
        private val getFunction: (JoinRequest) -> Flowable<JoinRequest>,
        private val joinCompleter: (JoinRequest, Boolean) -> Single<JoinRequest>
) : TeamHostingGofer<JoinRequest>(model, onError) {

    @get:JoinRequestState
    var state: Int = 0
        private set

    private val index: Int

    val isRequestOwner: Boolean
        get() = signedInUser == model.user

    val fabTitle: Int
        @StringRes
        get() = when (state) {
            JOINING -> R.string.join_team
            INVITING -> R.string.invite
            APPROVING -> R.string.approve
            WAITING, ACCEPTING -> R.string.accept
            else -> R.string.accept
        }


    @Retention(AnnotationRetention.SOURCE)
    @IntDef(INVITING, JOINING, APPROVING, ACCEPTING, WAITING)
    annotation class JoinRequestState

    init {
        index = getIndex(model)
        updateState()
        items.addAll(filteredItems(model))
    }

    fun showsFab(): Boolean = when (state) {
        INVITING, JOINING -> true
        APPROVING -> hasPrivilegedRole()
        ACCEPTING -> isRequestOwner
        else -> false
    }

    fun canEditFields(): Boolean = state == INVITING

    fun canEditRole(): Boolean = state == INVITING || state == JOINING

    private fun updateState() {
        val isEmpty = model.isEmpty
        val isRequestOwner = isRequestOwner
        val isUserEmpty = model.user.isEmpty
        val isUserApproved = model.isUserApproved
        val isTeamApproved = model.isTeamApproved

        state = when {
            isEmpty && isUserEmpty && isTeamApproved -> INVITING
            isEmpty && isUserApproved && isRequestOwner -> JOINING
            !isEmpty && isUserApproved && isRequestOwner || !isEmpty && isTeamApproved && !isRequestOwner -> WAITING
            isTeamApproved && isRequestOwner -> ACCEPTING
            else -> APPROVING
        }
    }

    fun getToolbarTitle(fragment: Fragment): String = fragment.getString(when (state) {
        JOINING -> R.string.join_team
        INVITING -> R.string.invite_user
        WAITING -> R.string.pending_request
        APPROVING -> R.string.approve_request
        else -> R.string.accept_request
    })

    override fun getImageClickMessage(fragment: Fragment): String? =
            fragment.getString(R.string.no_permission)

    public override fun fetch(): Flowable<DiffUtil.DiffResult> {
        val source = getFunction.invoke(model).map { it.asDifferentiables() }
        return FunctionalDiff.of(source, items) { _, _ -> filteredItems(model) }
    }

    override fun upsert(): Single<DiffUtil.DiffResult> {
        val single = if (model.isEmpty) joinTeam() else approveRequest()
        val source = single.map { it.asDifferentiables() }
                .doOnSuccess { updateState() }
        return FunctionalDiff.of(source, items) { _, _ -> filteredItems(model) }
    }

    public override fun delete(): Completable =
            joinCompleter.invoke(model, false)
                    .ignoreElement()
                    .observeOn(mainThread())

    private fun joinTeam(): Single<JoinRequest> =
            RepoProvider.forModel(TeamMember::class.java).createOrUpdate(model.toTeamMember()).map { model }

    private fun approveRequest(): Single<JoinRequest> =
            Single.defer { joinCompleter.invoke(model, true) }

    private fun filter(item: Item): Boolean {
        val isEmpty = model.isEmpty
        val sortPosition = item.sortPosition

        if (item.itemType == Item.ROLE) return true

        // Joining a team
        val joining = state == JOINING || state == ACCEPTING || state == WAITING && isRequestOwner
        if (joining) return sortPosition > index

        // Inviting a user
        val ignoreTeam = sortPosition <= index

        val stringRes = item.stringRes

        // About field should not show when inviting a user, email field should not show when trying
        // to join a team.
        return if (isEmpty) ignoreTeam && stringRes != R.string.user_about
        else ignoreTeam && stringRes != R.string.email
    }

    private fun getIndex(model: JoinRequest): Int = (0 until model.asItems().size)
            .toList().firstOrNull { index -> model.asItems()[index].itemType == Item.ROLE } ?: 0

    private fun filteredItems(request: JoinRequest): List<Differentiable> =
            request.asItems().filter(this::filter)

    companion object {

        const val INVITING = 0
        const val JOINING = 1
        const val APPROVING = 2
        const val ACCEPTING = 3
        const val WAITING = 4
    }
}
