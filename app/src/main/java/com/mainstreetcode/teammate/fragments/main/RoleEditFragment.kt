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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.util.yes

/**
 * Edits a Team member
 */

class RoleEditFragment : HeaderedFragment<Role>(), RoleEditAdapter.RoleEditAdapterListener {

    override lateinit var headeredModel: Role
        private set

    private lateinit var gofer: RoleGofer

    override val toolbarMenu: Int
        get() = R.menu.fragment_user_edit

    override val fabStringResource: Int
        @StringRes
        get() = R.string.role_update

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_check_white_24dp

    override val toolbarTitle: CharSequence
        get() = getString(R.string.role_edit)

    override fun getStableTag(): String =
            Gofer.tag(super.getStableTag(), arguments!!.getParcelable(ARG_ROLE)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_ROLE)!!
        gofer = teamMemberViewModel.gofer(headeredModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_headered, container, false)

        scrollManager = ScrollManager.with<InputViewHolder<*>>(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(RoleEditAdapter(gofer.items, this))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build()

        scrollManager.recyclerView.requestFocus()
        return rootView
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val kickItem = menu.findItem(R.id.action_kick)
        val blockItem = menu.findItem(R.id.action_block)

        kickItem?.isVisible = gofer.canChangeRoleFields()
        blockItem?.isVisible = canChangeRolePosition()
    }

    override fun insetFlags(): InsetFlags = NO_TOP

    override fun showsFab(): Boolean = gofer.canChangeRoleFields()

    override fun gofer(): TeamHostingGofer<Role> = gofer

    override fun onPrepComplete() {
        requireActivity().invalidateOptionsMenu()
        super.onPrepComplete()
    }

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        viewHolder.bind(headeredModel)
        scrollManager.onDiff(result)
        toggleProgress(false)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> if (headeredModel.position.isInvalid)
                showSnackbar(getString(R.string.select_role))
            else
                updateRole()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_kick -> showDropRolePrompt().yes
        R.id.action_block -> blockUser(headeredModel.user, headeredModel.team).yes
        else -> super.onOptionsItemSelected(item)
    }

    override fun canChangeRolePosition(): Boolean = gofer.hasPrivilegedRole()

    override fun canChangeRoleFields(): Boolean = gofer.canChangeRoleFields()

    private fun showDropRolePrompt() {
        AlertDialog.Builder(requireActivity()).setTitle(gofer.getDropRolePrompt(this))
                .setPositiveButton(R.string.yes) { _, _ -> disposables.add(gofer.remove().subscribe(this::onRoleDropped, defaultErrorHandler::accept)) }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun updateRole() {
        toggleProgress(true)
        disposables.add(gofer.save().subscribe(this::onRoleUpdated, defaultErrorHandler::accept))
    }

    private fun onRoleUpdated(result: DiffUtil.DiffResult) {
        viewHolder.bind(headeredModel)
        scrollManager.onDiff(result)
        togglePersistentUi()
        toggleProgress(false)
        requireActivity().invalidateOptionsMenu()
        showSnackbar(getString(R.string.updated_user, headeredModel.user.firstName))
    }

    private fun onRoleDropped() {
        showSnackbar(getString(R.string.dropped_user, headeredModel.user.firstName))
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
