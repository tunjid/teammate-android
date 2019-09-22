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
import com.mainstreetcode.teammate.adapters.RoleEditAdapter
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.mainstreetcode.teammate.viewmodel.gofers.RoleGofer
import com.mainstreetcode.teammate.viewmodel.gofers.TeamHostingGofer
import com.tunjid.androidbootstrap.view.util.InsetFlags

/**
 * Edits a Team member
 */

class RoleEditFragment : HeaderedFragment<Role>(R.layout.fragment_headered), RoleEditAdapter.RoleEditAdapterListener {

    override lateinit var headeredModel: Role
        private set

    private lateinit var gofer: RoleGofer

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = gofer.canChangeRoleFields()

    override val stableTag: String
        get() =
            Gofer.tag(super.stableTag, arguments!!.getParcelable(ARG_ROLE)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_ROLE)!!
        gofer = teamMemberViewModel.gofer(headeredModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        defaultUi(
                toolbarTitle = getString(R.string.role_edit),
                toolBarMenu = R.menu.fragment_user_edit,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabText = R.string.role_update
        )

        scrollManager = ScrollManager.with<InputViewHolder>(view.findViewById(R.id.model_list))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(RoleEditAdapter(gofer.items, this))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build().apply { recyclerView?.requestFocus() }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val kickItem = menu.findItem(R.id.action_kick)
        val blockItem = menu.findItem(R.id.action_block)

        kickItem?.isVisible = gofer.canChangeRoleFields()
        blockItem?.isVisible = canChangeRolePosition()
    }

    override fun gofer(): TeamHostingGofer<Role> = gofer

    override fun onPrepComplete() {
        requireActivity().invalidateOptionsMenu()
        super.onPrepComplete()
    }

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        viewHolder.bind(headeredModel)
        scrollManager.onDiff(result)
        transientBarDriver.toggleProgress(false)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> if (headeredModel.position.isInvalid)
                transientBarDriver.showSnackBar(getString(R.string.select_role))
            else
                updateRole()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_kick -> showDropRolePrompt().let { true }
        R.id.action_block -> blockUser(headeredModel.user, headeredModel.team).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun canChangeRolePosition(): Boolean = gofer.hasPrivilegedRole()

    override fun canChangeRoleFields(): Boolean = gofer.canChangeRoleFields()

    private fun showDropRolePrompt() {
        AlertDialog.Builder(requireActivity()).setTitle(gofer.getDropRolePrompt(this))
                .setPositiveButton(R.string.yes) { _, _ -> disposables.add(gofer.remove().subscribe(this::onRoleDropped, defaultErrorHandler::invoke)) }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun updateRole() {
        transientBarDriver.toggleProgress(true)
        disposables.add(gofer.save().subscribe(this::onRoleUpdated, defaultErrorHandler::invoke))
    }

    private fun onRoleUpdated(result: DiffUtil.DiffResult) {
        updateUi(fabShows = showsFab, toolbarInvalidated = true)

        viewHolder.bind(headeredModel)
        scrollManager.onDiff(result)
        transientBarDriver.toggleProgress(false)
        transientBarDriver.showSnackBar(getString(R.string.updated_user, headeredModel.user.firstName))
    }

    private fun onRoleDropped() {
        transientBarDriver.showSnackBar(getString(R.string.dropped_user, headeredModel.user.firstName))
        removeEnterExitTransitions()
        requireActivity().onBackPressed()
    }

    companion object {

        internal const val ARG_ROLE = "role"

        fun newInstance(role: Role): RoleEditFragment = RoleEditFragment().apply {
            arguments = bundleOf(ARG_ROLE to role)
            setEnterExitTransitions()
        }
    }
}
