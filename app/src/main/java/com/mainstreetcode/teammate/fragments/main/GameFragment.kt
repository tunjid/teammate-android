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
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.Shell
import com.mainstreetcode.teammate.adapters.statAdapter
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.GameViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.baseclasses.removeSharedElementTransitions
import com.mainstreetcode.teammate.databinding.FragmentGameBinding
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.ListState
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.AppBarListener
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.fetchRoundedDrawable
import com.mainstreetcode.teammate.viewmodel.gofers.GameGofer
import com.tunjid.androidx.recyclerview.diff.Differentiable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lists [games][Event]
 */

class GameFragment : TeammatesBaseFragment(R.layout.fragment_game), Shell.UserAdapterListener {

    private lateinit var game: Game
    private lateinit var items: List<Differentiable>

    private var hasChoiceBar: Boolean = false
    private val editableStatus: AtomicBoolean = AtomicBoolean()
    private val privilegeStatus: AtomicBoolean = AtomicBoolean()
    private var gameViewHolder: GameViewHolder? = null
    private var binding: FragmentGameBinding? = null

    private lateinit var gofer: GameGofer

    override val showsFab: Boolean get() = !bottomSheetDriver.isBottomSheetShowing && editableStatus.get() && !game.isEnded

    override val stableTag: String
        get() {
            val superResult = super.stableTag
            val tempGame = arguments!!.getParcelable<Game>(ARG_GAME)

            return if (tempGame != null) superResult + "-" + tempGame.hashCode()
            else superResult
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = arguments
        game = arguments!!.getParcelable(ARG_GAME)!!
        items = statViewModel.getModelList(game)
        gofer = gameViewModel.gofer(game)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = FragmentGameBinding.bind(view).run {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.game_stats),
                toolBarMenu = R.menu.fragment_game,
                fabText = R.string.stat_add,
                fabIcon = R.drawable.ic_add_white_24dp
        )

        gameViewHolder = GameViewHolder(appBar) {}
        gameViewHolder?.bind(game)

        scrollManager = ScrollManager.with<RecyclerView.ViewHolder>(view.findViewById(R.id.model_list))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_stat_white_24dp, R.string.no_stats))
                .withAdapter(statAdapter(::items) { navigator.push(StatEditFragment.newInstance(it)) })
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this@GameFragment.refresh() }
                .withEndlessScroll { fetchStats(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this@GameFragment::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build()

        scrollManager.setViewHolderColor(R.attr.alt_empty_view_holder_tint)

        refereeChip.setCloseIconResource(R.drawable.ic_close_24dp)
        refereeChip.setOnCloseIconClickListener { onRemoveRefereeClicked() }
        refereeChip.setOnClickListener { onRefereeClicked() }

        homeThumbnail.setOnClickListener { showCompetitor(game.home) }
        awayThumbnail.setOnClickListener { showCompetitor(game.away) }
        score.setOnClickListener { navigator.push(GameEditFragment.newInstance(game)) }
        date.setOnClickListener { navigator.push(EventEditFragment.newInstance(game)) }
        appBar.apply { AppBarListener(this) { gameViewHolder?.animate(it) } }

        binding = this
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_delete_game)?.isVisible = gofer.canDelete(userViewModel.currentUser)
        menu.findItem(R.id.action_end_game)?.isVisible = showsFab
        menu.findItem(R.id.action_event)?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_end_game -> endGameRequest().let { true }
        R.id.action_event -> navigator.push(EventEditFragment.newInstance(game))
        R.id.action_delete_game -> AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.game_delete_prompt))
                .setPositiveButton(R.string.yes) { _, _ -> deleteGame() }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        fetchGame()
        fetchStats(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameViewHolder = null
        binding = null
    }

    override fun onKeyBoardChanged(appeared: Boolean) {
        super.onKeyBoardChanged(appeared)
        if (!appeared && bottomSheetDriver.isBottomSheetShowing) bottomSheetDriver.hideBottomSheet()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fab) navigator.push(StatEditFragment.newInstance(Stat.empty(game)))
    }

    override fun onUserClicked(item: User) {
        hideKeyboard()
        binding?.refereeChip?.postDelayed({
            bottomSheetDriver.hideBottomSheet()
            binding?.refereeChip?.postDelayed({ addRefereeRequest(item) }, 150)
        }, 200)
    }

    private fun updateGame() {
        disposables.add(gofer.save().subscribe({ onGameUpdated() }, defaultErrorHandler::invoke))
    }

    private fun fetchGame() {
        disposables.add(gofer.fetch().subscribe({ onGameUpdated() }, defaultErrorHandler::invoke))
    }

    private fun refresh() {
        fetchGame()
        disposables.add(statViewModel.refresh(game).subscribe(this::onStatsFetched, defaultErrorHandler::invoke))
    }

    private fun updateStatuses() {
        disposables.add(statViewModel.isPrivilegedInGame(game)
                .subscribe(privilegeStatus::set, defaultErrorHandler::invoke))

        disposables.add(statViewModel.canEditGameStats(game).doOnSuccess(editableStatus::set)
                .subscribe({ updateUi(fabShows = showsFab) }, defaultErrorHandler::invoke))

        when {
            game.competitorsDeclined() -> scrollManager.updateForEmptyList(ListState(R.drawable.ic_stat_white_24dp, R.string.no_competitor_declined))
            game.competitorsNotAccepted() -> scrollManager.updateForEmptyList(ListState(R.drawable.ic_stat_white_24dp, R.string.no_competitor_acceptance))
            else -> scrollManager.updateForEmptyList(ListState(R.drawable.ic_stat_white_24dp, R.string.no_stats))
        }
    }

    private fun onRemoveRefereeClicked() {
        if (!privilegeStatus.get()) return
        game.referee.update(User.empty())
        transientBarDriver.toggleProgress(true)
        updateGame()
    }

    private fun onRefereeClicked() {
        val referee = game.referee
        val hasReferee = !referee.isEmpty

        if (hasReferee) navigator.push(UserEditFragment.newInstance(referee))
        else if (privilegeStatus.get()) bottomSheetDriver.showBottomSheet(
                requestCode = R.id.request_user_pick,
                fragment = UserSearchFragment.newInstance()
        )
    }

    private fun endGameRequest() {
        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.game_end_request)
                .setMessage(R.string.game_end_prompt)
                .setPositiveButton(R.string.yes) { _, _ ->
                    game.isEnded = true
                    transientBarDriver.toggleProgress(true)
                    disposables.add(gofer.save().subscribe({ onGameUpdated() }, defaultErrorHandler::invoke))
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun addRefereeRequest(user: User) {
        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.game_add_referee_title)
                .setMessage(R.string.game_add_referee_prompt)
                .setPositiveButton(R.string.yes) { _, _ ->
                    game.referee.update(user)
                    transientBarDriver.toggleProgress(true)
                    updateGame()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun fetchStats(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else transientBarDriver.toggleProgress(true)

        disposables.add(statViewModel.getMany(game, fetchLatest).subscribe(this::onStatsFetched, defaultErrorHandler::invoke))
        updateStatuses()
    }

    private fun onStatsFetched(result: DiffUtil.DiffResult) {
        transientBarDriver.toggleProgress(false)
        scrollManager.onDiff(result)
    }

    private fun onGameUpdated() {
        disposables.add(gofer.watchForChange().subscribe({ updateUi(toolbarInvalidated = true) }, ErrorHandler.EMPTY::invoke))
        gameViewHolder?.bind(game)
        transientBarDriver.toggleProgress(false)
        checkCompetitor()
        updateStatuses()
        bindReferee()
    }

    private fun respond(accept: Boolean, competitor: Competitor) {
        transientBarDriver.toggleProgress(true)
        disposables.add(competitorViewModel.respond(competitor, accept)
                .subscribe({
                    if (accept) fetchGame()
                    else transientBarDriver.toggleProgress(false)
                }, defaultErrorHandler::invoke))
    }

    private fun deleteGame() {
        disposables.add(gofer.remove().subscribe(this::onGameDeleted, defaultErrorHandler::invoke))
    }

    private fun onGameDeleted() {
        transientBarDriver.showSnackBar(getString(R.string.game_deleted))
        removeSharedElementTransitions()
        requireActivity().onBackPressed()
    }

    private fun checkCompetitor() {
        if (hasChoiceBar) return
        disposables.add(roleViewModel.hasPendingCompetitor(game).subscribe({ competitor ->
            hasChoiceBar = true
            transientBarDriver.showChoices { choiceBar ->
                choiceBar.setText(getString(R.string.game_accept))
                        .setPositiveText(getText(R.string.accept))
                        .setNegativeText(getText(R.string.decline))
                        .setPositiveClickListener(View.OnClickListener { respond(true, competitor) })
                        .setNegativeClickListener(View.OnClickListener { respond(false, competitor) })
                        .addCallback(object : BaseTransientBottomBar.BaseCallback<ChoiceBar>() {
                            override fun onDismissed(shown: ChoiceBar?, event: Int) {
                                hasChoiceBar = false
                            }
                        })
            }
        }, ErrorHandler.EMPTY::invoke))
    }

    private fun bindReferee() = binding?.apply {
        val size = resources.getDimensionPixelSize(R.dimen.double_margin)
        val referee = game.referee
        TransitionManager.beginDelayedTransition(innerCoordinator, AutoTransition().addTarget(refereeChip))

        refereeChip.isCloseIconVisible = !referee.isEmpty && privilegeStatus.get()
        refereeChip.text = when {
            referee.isEmpty -> getString(when {
                privilegeStatus.get() && !game.isEnded -> R.string.game_choose_referee
                else -> R.string.game_no_referee
            })
            else -> getString(R.string.game_referee, referee.name)
        }

        disposables.add(fetchRoundedDrawable(requireContext(), referee.imageUrl, size)
                .subscribe({ refereeChip?.chipIcon = it }, ErrorHandler.EMPTY::invoke))
    }

    companion object {

        private const val ARG_GAME = "game"

        fun newInstance(game: Game): GameFragment = GameFragment().apply { arguments = bundleOf(ARG_GAME to game) }
    }
}
