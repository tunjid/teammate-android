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

package com.mainstreetcode.teammate.fragments.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.JoinRequestAdapter
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.mainstreetcode.teammate.viewmodel.gofers.JoinRequestGofer
import com.mainstreetcode.teammate.viewmodel.gofers.JoinRequestGofer.Companion.ACCEPTING
import com.mainstreetcode.teammate.viewmodel.gofers.JoinRequestGofer.Companion.APPROVING
import com.mainstreetcode.teammate.viewmodel.gofers.JoinRequestGofer.Companion.INVITING
import com.mainstreetcode.teammate.viewmodel.gofers.JoinRequestGofer.Companion.JOINING
import com.mainstreetcode.teammate.viewmodel.gofers.JoinRequestGofer.Companion.WAITING
import com.mainstreetcode.teammate.viewmodel.gofers.TeamHostingGofer
import com.tunjid.androidx.view.util.InsetFlags

/**
 * Invites a Team member
 */

class JoinRequestFragment : HeaderedFragment<JoinRequest>(R.layout.fragment_headered),
        JoinRequestAdapter.AdapterListener {

    override lateinit var headeredModel: JoinRequest
        private set

    private lateinit var gofer: JoinRequestGofer

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = gofer.showsFab()

    override val stableTag: String
        get() =
            Gofer.tag(super.stableTag, arguments!!.getParcelable(ARG_JOIN_REQUEST)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_JOIN_REQUEST)!!
        gofer = teamMemberViewModel.gofer(headeredModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = gofer.getToolbarTitle(this),
                toolBarMenu =R.menu.fragment_user_edit,
                fabText = gofer.fabTitle,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabShows = showsFab
        )

        scrollManager = ScrollManager.with<InputViewHolder>(view.findViewById(R.id.model_list))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(JoinRequestAdapter(gofer.items, this))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build().apply { recyclerView?.requestFocus() }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val isEmpty = headeredModel.isEmpty
        val canBlockUser = gofer.hasPrivilegedRole()
        val canDeleteRequest = canBlockUser || gofer.isRequestOwner

        val blockItem = menu.findItem(R.id.action_block)
        val deleteItem = menu.findItem(R.id.action_kick)

        blockItem?.isVisible = !isEmpty && canBlockUser
        deleteItem?.isVisible = !isEmpty && canDeleteRequest
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_kick -> showDeletePrompt().let { true }
        R.id.action_block -> blockUser(headeredModel.user, headeredModel.team).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onImageClick() = Unit

    override fun gofer(): TeamHostingGofer<JoinRequest> = gofer

    override fun onPrepComplete() {
        updateUi(toolbarInvalidated = true)
        super.onPrepComplete()
    }

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        viewHolder?.bind(headeredModel)
        scrollManager.onDiff(result)
        transientBarDriver.toggleProgress(false)
    }

    override fun canEditFields(): Boolean = gofer.canEditFields()

    override fun canEditRole(): Boolean = gofer.canEditRole()

    override fun onClick(view: View) {
        if (view.id != R.id.fab) return

        @JoinRequestGofer.JoinRequestState
        val state = gofer.state

        if (state == WAITING) return

        when {
            state == APPROVING || state == ACCEPTING -> saveRequest()
            headeredModel.position.isInvalid -> transientBarDriver.showSnackBar(getString(R.string.select_role))
            state == JOINING || state == INVITING -> createJoinRequest()
        }
    }

    private fun createJoinRequest() {
        transientBarDriver.toggleProgress(true)
        disposables.add(gofer.save().subscribe(this::onJoinRequestSent, defaultErrorHandler::invoke))
    }

    private fun saveRequest() {
        transientBarDriver.toggleProgress(true)
        disposables.add(gofer.save().subscribe({ onRequestSaved() }, defaultErrorHandler::invoke))
    }

    private fun deleteRequest() {
        transientBarDriver.toggleProgress(true)
        disposables.add(gofer.remove().subscribe(this::onRequestDeleted, defaultErrorHandler::invoke))
    }

    private fun onJoinRequestSent(result: DiffUtil.DiffResult) {
        updateUi(fabShows = showsFab)
        scrollManager.onDiff(result)
        bottomSheetDriver.hideBottomSheet()
        transientBarDriver.toggleProgress(false)
        transientBarDriver.showSnackBar(getString(
                if (headeredModel.isTeamApproved) R.string.user_invite_sent
                else R.string.team_submitted_join_request))
    }

    private fun onRequestDeleted() {
        val name = headeredModel.user.firstName
        if (!gofer.isRequestOwner) transientBarDriver.showSnackBar(getString(R.string.removed_user, name))
        requireActivity().onBackPressed()
    }

    private fun onRequestSaved() {
        val name = headeredModel.user.firstName
        if (!gofer.isRequestOwner) transientBarDriver.showSnackBar(getString(R.string.added_user, name))
        requireActivity().onBackPressed()
    }

    private fun showDeletePrompt() {
        val requestUser = headeredModel.user
        val prompt =
                if (gofer.isRequestOwner) getString(R.string.confirm_request_leave, headeredModel.team.name)
                else getString(R.string.confirm_request_drop, requestUser.firstName)

        AlertDialog.Builder(requireActivity()).setTitle(prompt)
                .setPositiveButton(R.string.yes) { _, _ -> deleteRequest() }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    companion object {

        internal const val ARG_JOIN_REQUEST = "join-request"

        internal fun inviteInstance(team: Team): JoinRequestFragment {
            val fragment = newInstance(JoinRequest.invite(team))
            fragment.setEnterExitTransitions()

            return fragment
        }

        fun joinInstance(team: Team, user: User): JoinRequestFragment {
            val fragment = newInstance(JoinRequest.join(team, user))
            fragment.setEnterExitTransitions()

            return fragment
        }

        internal fun viewInstance(request: JoinRequest): JoinRequestFragment {
            val fragment = newInstance(request)
            fragment.setEnterExitTransitions()

            return fragment
        }

        private fun newInstance(joinRequest: JoinRequest): JoinRequestFragment = JoinRequestFragment().apply {
            arguments = bundleOf(ARG_JOIN_REQUEST to joinRequest)
            setEnterExitTransitions()
        }
    }
}
