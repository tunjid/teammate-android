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
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.GameAdapter
import com.mainstreetcode.teammate.adapters.StatAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.GameViewHolder
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
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
import com.mainstreetcode.teammate.util.simpleAdapterListener
import com.mainstreetcode.teammate.viewmodel.gofers.GameGofer
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lists [games][Event]
 */

class GameFragment : MainActivityFragment(), UserAdapter.AdapterListener {

    private lateinit var game: Game
    private lateinit var items: List<Differentiable>

    private var hasChoiceBar: Boolean = false
    private val editableStatus: AtomicBoolean = AtomicBoolean()
    private val privilegeStatus: AtomicBoolean = AtomicBoolean()
    private var gameViewHolder: GameViewHolder? = null

    private lateinit var gofer: GameGofer
    private var refereeChip: Chip? = null

    override val fabStringResource: Int
        @StringRes
        get() = R.string.stat_add

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_add_white_24dp

    override val toolbarMenu: Int
        get() = R.menu.fragment_game

    override val toolbarTitle: CharSequence
        get() = getString(R.string.game_stats)

    override fun getStableTag(): String {
        val superResult = super.getStableTag()
        val tempGame = arguments!!.getParcelable<Game>(ARG_GAME)

        return if (tempGame != null)
            superResult + "-" + tempGame.hashCode()
        else
            superResult
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = arguments
        game = arguments!!.getParcelable(ARG_GAME)!!
        items = statViewModel.getModelList(game)
        gofer = gameViewModel.gofer(game)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_game, container, false)
        val appBar = rootView.findViewById<View>(R.id.app_bar)
        refereeChip = rootView.findViewById(R.id.referee_chip)
        gameViewHolder = GameViewHolder(appBar, GameAdapter.AdapterListener.asSAM { })
        gameViewHolder?.bind(game)

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(rootView.findViewById(R.id.model_list))
                .withPlaceholder(EmptyViewHolder(rootView, R.drawable.ic_stat_white_24dp, R.string.no_stats))
                .withAdapter(StatAdapter(items, simpleAdapterListener { showFragment(StatEditFragment.newInstance(it)) }))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withEndlessScroll { fetchStats(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build()

        scrollManager.setViewHolderColor(R.attr.alt_empty_view_holder_tint)

        refereeChip?.setCloseIconResource(R.drawable.ic_close_24dp)
        refereeChip?.setOnCloseIconClickListener { onRemoveRefereeClicked() }
        refereeChip?.setOnClickListener { onRefereeClicked() }

        rootView.findViewById<View>(R.id.home_thumbnail).setOnClickListener { showCompetitor(game.home) }
        rootView.findViewById<View>(R.id.away_thumbnail).setOnClickListener { showCompetitor(game.away) }
        rootView.findViewById<View>(R.id.score).setOnClickListener { showFragment(GameEditFragment.newInstance(game)) }
        rootView.findViewById<View>(R.id.date).setOnClickListener { showFragment(EventEditFragment.newInstance(game)) }
        rootView.findViewById<AppBarLayout>(R.id.app_bar).apply { AppBarListener(this) { gameViewHolder?.animate(it) } }

        return rootView
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_delete_game)?.isVisible = gofer.canDelete(userViewModel.currentUser)
        menu.findItem(R.id.action_end_game)?.isVisible = showsFab()
        menu.findItem(R.id.action_event)?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_end_game -> endGameRequest().let { true }
        R.id.action_event -> showFragment(EventEditFragment.newInstance(game))
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
        refereeChip = null
    }

    override fun onKeyBoardChanged(appeared: Boolean) {
        super.onKeyBoardChanged(appeared)
        if (!appeared && isBottomSheetShowing) hideBottomSheet()
    }

    override fun showsFab(): Boolean {
        return !isBottomSheetShowing && editableStatus.get() && !game.isEnded
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fab) showFragment(StatEditFragment.newInstance(Stat.empty(game)))
    }

    override fun onUserClicked(item: User) {
        hideKeyboard()
        refereeChip?.postDelayed({
            hideBottomSheet()
            refereeChip?.postDelayed({ addRefereeRequest(item) }, 150)
        }, 200)
    }

    private fun updateGame() {
        disposables.add(gofer.save().subscribe({ onGameUpdated() }, defaultErrorHandler::accept))
    }

    private fun fetchGame() {
        disposables.add(gofer.fetch().subscribe({ onGameUpdated() }, defaultErrorHandler::accept))
    }

    private fun refresh() {
        fetchGame()
        disposables.add(statViewModel.refresh(game).subscribe(this::onStatsFetched, defaultErrorHandler::accept))
    }

    private fun updateStatuses() {
        disposables.add(statViewModel.isPrivilegedInGame(game)
                .subscribe(privilegeStatus::set, defaultErrorHandler::accept))

        disposables.add(statViewModel.canEditGameStats(game).doOnSuccess(editableStatus::set)
                .subscribe({ togglePersistentUi() }, defaultErrorHandler::accept))

        when {
            game.competitorsDeclined() -> scrollManager.updateForEmptyList(ListState(R.drawable.ic_stat_white_24dp, R.string.no_competitor_declined))
            game.competitorsNotAccepted() -> scrollManager.updateForEmptyList(ListState(R.drawable.ic_stat_white_24dp, R.string.no_competitor_acceptance))
            else -> scrollManager.updateForEmptyList(ListState(R.drawable.ic_stat_white_24dp, R.string.no_stats))
        }
    }

    private fun onRemoveRefereeClicked() {
        if (!privilegeStatus.get()) return
        game.referee.update(User.empty())
        toggleProgress(true)
        updateGame()
    }

    private fun onRefereeClicked() {
        val referee = game.referee
        val hasReferee = !referee.isEmpty

        if (hasReferee) showFragment(UserEditFragment.newInstance(referee))
        else if (privilegeStatus.get()) {
            val fragment = UserSearchFragment.newInstance()
            fragment.setTargetFragment(this, R.id.request_user_pick)
            showBottomSheet(BottomSheetController.Args.builder()
                    .setMenuRes(R.menu.empty)
                    .setFragment(fragment)
                    .build())
        }
    }

    private fun endGameRequest() {
        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.game_end_request)
                .setMessage(R.string.game_end_prompt)
                .setPositiveButton(R.string.yes) { _, _ ->
                    game.isEnded = true
                    toggleProgress(true)
                    disposables.add(gofer.save().subscribe({ onGameUpdated() }, defaultErrorHandler::accept))
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
                    toggleProgress(true)
                    updateGame()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun fetchStats(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else toggleProgress(true)

        disposables.add(statViewModel.getMany(game, fetchLatest).subscribe(this::onStatsFetched, defaultErrorHandler::accept))
        updateStatuses()
    }

    private fun onStatsFetched(result: DiffUtil.DiffResult) {
        toggleProgress(false)
        scrollManager.onDiff(result)
    }

    private fun onGameUpdated() {
        disposables.add(gofer.watchForChange().subscribe({ requireActivity().invalidateOptionsMenu() }, ErrorHandler.EMPTY::accept))
        gameViewHolder?.bind(game)
        toggleProgress(false)
        checkCompetitor()
        updateStatuses()
        bindReferee()
    }

    private fun respond(accept: Boolean, competitor: Competitor) {
        toggleProgress(true)
        disposables.add(competitorViewModel.respond(competitor, accept)
                .subscribe({
                    if (accept) fetchGame()
                    else toggleProgress(false)
                }, defaultErrorHandler::accept))
    }

    private fun deleteGame() {
        disposables.add(gofer.remove().subscribe(this::onGameDeleted, defaultErrorHandler::accept))
    }

    private fun onGameDeleted() {
        showSnackbar(getString(R.string.game_deleted))
        removeEnterExitTransitions()
        requireActivity().onBackPressed()
    }

    private fun checkCompetitor() {
        if (hasChoiceBar) return
        disposables.add(roleViewModel.hasPendingCompetitor(game).subscribe({ competitor ->
            hasChoiceBar = true
            showChoices { choiceBar ->
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
        }, ErrorHandler.EMPTY::accept))
    }

    private fun bindReferee() {
        val root = view as? ViewGroup ?: return

        val size = resources.getDimensionPixelSize(R.dimen.double_margin)
        val referee = game.referee
        TransitionManager.beginDelayedTransition(root, AutoTransition().addTarget(refereeChip))

        refereeChip?.isCloseIconVisible = !referee.isEmpty && privilegeStatus.get()
        refereeChip?.text = when {
            referee.isEmpty -> getString(when {
                privilegeStatus.get() && !game.isEnded -> R.string.game_choose_referee
                else -> R.string.game_no_referee
            })
            else -> getString(R.string.game_referee, referee.name)
        }

        disposables.add(fetchRoundedDrawable(requireContext(), referee.imageUrl, size)
                .subscribe({ refereeChip?.chipIcon = it }, ErrorHandler.EMPTY::accept))
    }

    companion object {

        private const val ARG_GAME = "game"

        fun newInstance(game: Game): GameFragment = GameFragment().apply { arguments = bundleOf(ARG_GAME to game) }
    }
}
