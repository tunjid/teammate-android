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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.StatEditAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
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

class StatEditFragment : HeaderedFragment<Stat>(), UserAdapter.AdapterListener, StatEditAdapter.AdapterListener {

    override lateinit var headeredModel: Stat
        private set

    private lateinit var gofer: StatGofer

    override val stat: Stat get() = headeredModel

    override val fabStringResource: Int @StringRes get() = if (headeredModel.isEmpty) R.string.stat_create else R.string.stat_update

    override val fabIconResource: Int @DrawableRes get() = R.drawable.ic_check_white_24dp

    override val toolbarMenu: Int get() = R.menu.fragment_stat_edit

    override val toolbarTitle: CharSequence get() = getString(if (headeredModel.isEmpty) R.string.stat_add else R.string.stat_edit)

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = !isBottomSheetShowing && gofer.canEdit()

    override val staticViews: IntArray get() = EXCLUDED_VIEWS

    override fun getStableTag(): String =
            Gofer.tag(super.getStableTag(), arguments!!.getParcelable(ARG_STAT)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_STAT)!!
        gofer = statViewModel.gofer(headeredModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_headered, container, false)

        scrollManager = ScrollManager.with<BaseViewHolder<*>>(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(StatEditAdapter(gofer.items, this))
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
        menu.findItem(R.id.action_delete)?.isVisible = gofer.canEdit() && !headeredModel.game.isEnded && !headeredModel.isEmpty
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                val context = context ?: return true
                AlertDialog.Builder(context).setTitle(getString(R.string.delete_stat_prompt))
                        .setPositiveButton(R.string.yes) { _, _ -> deleteStat() }
                        .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                        .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        statViewModel.clearNotifications(headeredModel)
    }

    override fun gofer(): Gofer<Stat> = gofer

    override fun canExpandAppBar(): Boolean = false

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        toggleProgress(false)
        scrollManager.onDiff(result)
        viewHolder.bind(headeredModel)
        activity?.invalidateOptionsMenu()
    }

    override fun onUserClicked(item: User) {
        disposables.add(gofer.chooseUser(item).subscribe(this::onModelUpdated, defaultErrorHandler::invoke))
        hideBottomSheet()
    }

    override fun onUserClicked() {
        when {
            headeredModel.game.isEnded -> showSnackbar(getString(R.string.stat_game_ended))
            !headeredModel.isEmpty -> showSnackbar(getString(R.string.stat_already_added))
            else -> pickStatUser()
        }
    }

    override fun onTeamClicked() {
        when {
            headeredModel.game.isEnded -> showSnackbar(getString(R.string.stat_game_ended))
            !headeredModel.isEmpty -> showSnackbar(getString(R.string.stat_already_added))
            else -> switchStatTeam()
        }
    }

    override fun canChangeStat(): Boolean = headeredModel.isEmpty

    override fun onPrepComplete() {
        scrollManager.notifyDataSetChanged()
        requireActivity().invalidateOptionsMenu()
        super.onPrepComplete()
    }

    override fun onClick(view: View) {
        if (view.id != R.id.fab) return

        toggleProgress(true)
        disposables.add(gofer.save().subscribe({ requireActivity().onBackPressed() }, defaultErrorHandler::invoke))
    }

    private fun deleteStat() {
        disposables.add(gofer.remove().subscribe(this::onStatDeleted, defaultErrorHandler::invoke))
    }

    private fun onStatDeleted() {
        showSnackbar(getString(R.string.deleted_team, headeredModel.statType))
        removeEnterExitTransitions()

        activity?.onBackPressed()
    }

    private fun pickStatUser() {
        val fragment = TeamMembersFragment.newInstance(headeredModel.team)
        fragment.setTargetFragment(this, R.id.request_stat_edit_pick)

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setTitle(getString(R.string.pick_user))
                .setFragment(fragment)
                .build())
    }

    private fun switchStatTeam() {
        disposables.add(gofer.switchTeams().subscribe(this::onModelUpdated, defaultErrorHandler::invoke))
    }

    companion object {

        private const val ARG_STAT = "stat"
        private val EXCLUDED_VIEWS = intArrayOf(R.id.model_list)

        fun newInstance(stat: Stat): StatEditFragment = StatEditFragment().apply {
            arguments = bundleOf(ARG_STAT to stat)
            setEnterExitTransitions()
        }
    }
}
