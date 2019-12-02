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
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.Shell
import com.mainstreetcode.teammate.adapters.gameAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.ListState
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.swap
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.recyclerview.diff.Differentiable

/**
 * Lists [tournaments][Event]
 */

class GamesFragment : TeammatesBaseFragment(R.layout.fragment_list_with_refresh),
        Shell.TeamAdapterListener {

    private var team by args<Team>()

    private val items: MutableList<Differentiable>
        get() = gameViewModel.getModelList(team)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.games_title, team.name),
                toolBarMenu = R.menu.fragment_tournaments,
                fabText = R.string.game_add,
                fabIcon = R.drawable.ic_add_white_24dp
        )

        val refreshAction = { disposables.add(gameViewModel.refresh(team).subscribe(this::onGamesUpdated, defaultErrorHandler::invoke)).let { Unit } }

        scrollManager = ScrollManager.with<RecyclerView.ViewHolder>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_score_white_24dp, R.string.no_games))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchGames(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(gameAdapter(::items, this::onGameClicked))
                .withLinearLayoutManager()
                .build()
    }

    override fun onResume() {
        super.onResume()

        watchForRoleChanges(team) { updateUi(fabShows = showsFab) }

        if (teamViewModel.defaultTeam != team) onTeamClicked(teamViewModel.defaultTeam)
        else fetchGames(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_pick_team -> bottomSheetDriver.showBottomSheet(
                requestCode = R.id.request_game_team_pick,
                title = getString(R.string.pick_team),
                fragment = TeamsFragment.newInstance()
        ).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override val showsFab: Boolean
        get() {
            val sport = team.sport
            val supportsTournaments = sport.supportsCompetitions()
            return if (sport.betweenUsers()) supportsTournaments else supportsTournaments && roleScopeViewModel.hasPrivilegedRole(team)
        }

    override fun onTeamClicked(item: Team) = disposables.add(teamViewModel.swap(team, item, gameViewModel) {
        disposables.clear()
        bottomSheetDriver.hideBottomSheet()

        watchForRoleChanges(team) { updateUi(fabShows = showsFab) }
        updateUi(toolbarTitle = getString(R.string.games_title, team.name))
    }.subscribe(::onGamesUpdated, defaultErrorHandler::invoke)).let { Unit }

    override fun onClick(view: View) = when (view.id) {
        R.id.fab -> {
            val game = Game.empty(team)
            val entity: Competitive =
                    if (User.COMPETITOR_TYPE == game.refPath) userViewModel.currentUser
                    else teamViewModel.defaultTeam

            game.home.updateEntity(entity)
            navigator.push(GameEditFragment.newInstance(game)).let { Unit }
        }
        else -> Unit
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) = when (incomingFragment) {
        is TournamentEditFragment -> transaction.listDetailTransition(TournamentEditFragment.ARG_TOURNAMENT, incomingFragment)
        else -> super.augmentTransaction(transaction, incomingFragment)
    }

    private fun onGameClicked(game: Game) {
        navigator.push(GameFragment.newInstance(game))
    }

    private fun fetchGames(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else transientBarDriver.toggleProgress(true)

        disposables.add(gameViewModel.getMany(team, fetchLatest).subscribe(this::onGamesUpdated, defaultErrorHandler::invoke))
    }

    private fun onGamesUpdated(result: DiffUtil.DiffResult) {
        transientBarDriver.toggleProgress(false)
        val supportsTournaments = team.sport.supportsCompetitions()
        scrollManager.onDiff(result)
        scrollManager.updateForEmptyList(ListState(R.drawable.ic_score_white_24dp,
                if (supportsTournaments) R.string.no_games
                else R.string.no_game_support))
    }

    companion object {
        fun newInstance(team: Team): GamesFragment = GamesFragment().apply { this.team = team }
    }
}
