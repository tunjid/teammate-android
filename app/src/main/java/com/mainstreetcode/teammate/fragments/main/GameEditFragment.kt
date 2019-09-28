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
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.GameEditAdapter
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.GameGofer
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.tunjid.androidx.view.util.InsetFlags

/**
 * Edits a Team member
 */

class GameEditFragment : HeaderedFragment<Game>(R.layout.fragment_headered),
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener,
        GameEditAdapter.AdapterListener {

    override lateinit var headeredModel: Game
        private set

    private lateinit var gofer: GameGofer

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = gofer.canEdit() && !bottomSheetDriver.isBottomSheetShowing

    override val stableTag: String get() = Gofer.tag(super.stableTag, arguments!!.getParcelable(ARG_GAME)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_GAME)!!
        gofer = gameViewModel.gofer(headeredModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(if (headeredModel.isEmpty) R.string.game_add else R.string.game_edit),
                fabIcon = R.drawable.ic_check_white_24dp,
                fabText = if (headeredModel.isEmpty) R.string.game_create else R.string.game_update,
                fabShows = showsFab
        )

        scrollManager = ScrollManager.with<BaseViewHolder<*>>(view.findViewById(R.id.model_list))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(GameEditAdapter(gofer.items, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build()

        scrollManager.recyclerView?.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        statViewModel.clearNotifications(headeredModel)
    }

    override fun gofer(): Gofer<Game> = gofer

    override fun canExpandAppBar(): Boolean = false

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        viewHolder?.bind(headeredModel)

        transientBarDriver.toggleProgress(false)
        updateUi(toolbarInvalidated = true)
    }

    override fun canEditGame(): Boolean = gofer.canEdit()

    override fun onUserClicked(item: User) = updateCompetitor(item)

    override fun onTeamClicked(item: Team) = updateCompetitor(item)

    override fun onAwayClicked(away: Competitor) {
        if (!headeredModel.isEmpty) return
        if (headeredModel.home === away) transientBarDriver.showSnackBar(getString(R.string.game_create_prompt))
        else pickAwaySide()
    }

    override fun onPrepComplete() {
        scrollManager.notifyDataSetChanged()
        updateUi(toolbarInvalidated = true)
        super.onPrepComplete()
    }

    override fun onClick(view: View) {
        if (view.id != R.id.fab) return

        if (headeredModel.isEmpty) createGame()
        else AlertDialog.Builder(requireActivity())
                .setTitle(R.string.game_manual_score_request)
                .setMessage(R.string.game_end_prompt)
                .setPositiveButton(R.string.yes) { _, _ ->
                    transientBarDriver.toggleProgress(true)
                    transientBarDriver.toggleProgress(true)
                    disposables.add(gameViewModel.endGame(headeredModel).subscribe({ requireActivity().onBackPressed() }, defaultErrorHandler::invoke))
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun createGame() {
        transientBarDriver.toggleProgress(true)
        disposables.add(gofer.save().subscribe({ navigator.show(GameFragment.newInstance(headeredModel)) }, defaultErrorHandler::invoke))
    }

    private fun updateCompetitor(item: Competitive) {
        headeredModel.away.updateEntity(item)
        scrollManager.notifyDataSetChanged()
        bottomSheetDriver.hideBottomSheet()
        hideKeyboard()
    }

    private fun pickAwaySide() = bottomSheetDriver.showBottomSheet {
        val refPath = headeredModel.refPath
        val isBetweenUsers = User.COMPETITOR_TYPE == refPath

        menuRes = R.menu.empty

        if (isBetweenUsers) fragment = UserSearchFragment.newInstance()
        else if (Team.COMPETITOR_TYPE == refPath) fragment = TeamSearchFragment.newInstance(headeredModel.sport)

        fragment?.setTargetFragment(this@GameEditFragment, R.id.request_competitor_pick)
    }

    companion object {

        private const val ARG_GAME = "game"

        fun newInstance(game: Game): GameEditFragment = GameEditFragment().apply {
            arguments = bundleOf(ARG_GAME to game)
            setEnterExitTransitions()
        }
    }
}
