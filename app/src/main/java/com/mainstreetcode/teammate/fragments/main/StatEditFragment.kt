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
import com.mainstreetcode.teammate.adapters.StatEditAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.mainstreetcode.teammate.viewmodel.gofers.StatGofer
import com.tunjid.androidbootstrap.view.util.InsetFlags

/**
 * Edits a Team member
 */

class StatEditFragment : HeaderedFragment<Stat>(R.layout.fragment_headered),
        UserAdapter.AdapterListener,
        StatEditAdapter.AdapterListener {

    override lateinit var headeredModel: Stat
        private set

    private lateinit var gofer: StatGofer

    override val stat: Stat get() = headeredModel

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = !bottomSheetDriver.isBottomSheetShowing && gofer.canEdit()

    override val stableTag: String get() = Gofer.tag(super.stableTag, arguments!!.getParcelable(ARG_STAT)!!)

    private val toolbarTitle get() = getString(if (headeredModel.isEmpty) R.string.stat_add else R.string.stat_edit)

    private val fabText get() = if (headeredModel.isEmpty) R.string.stat_create else R.string.stat_update

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_STAT)!!
        gofer = statViewModel.gofer(headeredModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = toolbarTitle,
                toolBarMenu = R.menu.fragment_stat_edit,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabText = fabText,
                fabShows = showsFab
        )
        scrollManager = ScrollManager.with<BaseViewHolder<*>>(view.findViewById(R.id.model_list))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(StatEditAdapter(gofer.items, this))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build().apply { recyclerView?.requestFocus() }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_delete)?.isVisible = gofer.canEdit() && !headeredModel.game.isEnded && !headeredModel.isEmpty
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_delete -> context?.run {
            AlertDialog.Builder(this).setTitle(getString(R.string.delete_stat_prompt))
                    .setPositiveButton(R.string.yes) { _, _ -> deleteStat() }
                    .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                    .show()
            true
        } ?: true
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        statViewModel.clearNotifications(headeredModel)
    }

    override fun gofer(): Gofer<Stat> = gofer

    override fun canExpandAppBar(): Boolean = false

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        updateUi(toolbarTitle = toolbarTitle, fabText = fabText, toolbarInvalidated = true)
        transientBarDriver.toggleProgress(false)
        scrollManager.onDiff(result)
        viewHolder?.bind(headeredModel)
    }

    override fun onUserClicked(item: User) {
        disposables.add(gofer.chooseUser(item).subscribe(this::onModelUpdated, defaultErrorHandler::invoke))
        bottomSheetDriver.hideBottomSheet()
    }

    override fun onUserClicked() = when {
        headeredModel.game.isEnded -> transientBarDriver.showSnackBar(getString(R.string.stat_game_ended))
        !headeredModel.isEmpty -> transientBarDriver.showSnackBar(getString(R.string.stat_already_added))
        else -> pickStatUser()
    }

    override fun onTeamClicked() = when {
        headeredModel.game.isEnded -> transientBarDriver.showSnackBar(getString(R.string.stat_game_ended))
        !headeredModel.isEmpty -> transientBarDriver.showSnackBar(getString(R.string.stat_already_added))
        else -> switchStatTeam()
    }

    override fun canChangeStat(): Boolean = headeredModel.isEmpty

    override fun onPrepComplete() {
        scrollManager.notifyDataSetChanged()
        updateUi(toolbarInvalidated = true)
        super.onPrepComplete()
    }

    override fun onClick(view: View) {
        if (view.id != R.id.fab) return

        transientBarDriver.toggleProgress(true)
        disposables.add(gofer.save().subscribe({ requireActivity().onBackPressed() }, defaultErrorHandler::invoke))
    }

    private fun deleteStat() {
        disposables.add(gofer.remove().subscribe(this::onStatDeleted, defaultErrorHandler::invoke))
    }

    private fun onStatDeleted() {
        transientBarDriver.showSnackBar(getString(R.string.deleted_team, headeredModel.statType))
        removeEnterExitTransitions()

        activity?.onBackPressed()
    }

    private fun pickStatUser() = bottomSheetDriver.showBottomSheet {
        val caller = TeamMembersFragment.newInstance(headeredModel.team)
        caller.setTargetFragment(this@StatEditFragment, R.id.request_stat_edit_pick)

        menuRes = R.menu.empty
        title = getString(R.string.pick_user)
        fragment = caller
    }

    private fun switchStatTeam() {
        disposables.add(gofer.switchTeams().subscribe(this::onModelUpdated, defaultErrorHandler::invoke))
    }

    companion object {

        private const val ARG_STAT = "stat"

        fun newInstance(stat: Stat): StatEditFragment = StatEditFragment().apply {
            arguments = bundleOf(ARG_STAT to stat)
            setEnterExitTransitions()
        }
    }
}
