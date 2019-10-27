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
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.TournamentAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.ListState
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.swap
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.recyclerview.InteractiveViewHolder
import com.tunjid.androidx.recyclerview.diff.Differentiable

/**
 * Lists [tournaments][Event]
 */

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class TournamentsFragment : TeammatesBaseFragment(R.layout.fragment_list_with_refresh),
        TeamAdapter.AdapterListener,
        TournamentAdapter.TournamentAdapterListener {

    private var team by args<Team>()

    private val items: MutableList<Differentiable>
        get() = tournamentViewModel.getModelList(team)

    override val showsFab: Boolean get() = team.sport.supportsCompetitions() && localRoleViewModel.hasPrivilegedRole()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.tournaments_title, team.name),
                toolBarMenu = R.menu.fragment_tournaments,
                fabIcon = R.drawable.ic_add_white_24dp,
                fabText = R.string.tournament_add,
                fabShows = showsFab
        )
        val refreshAction = {
            disposables.add(tournamentViewModel.refresh(team)
                    .subscribe(this::onTournamentsUpdated, defaultErrorHandler::invoke)).let { Unit }
        }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_trophy_white_24dp, R.string.no_tournaments))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchTournaments(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(TournamentAdapter(::items, this))
                .withLinearLayoutManager()
                .build()
    }

    override fun onResume() {
        super.onResume()

        watchForRoleChanges(team) { updateUi(fabShows = showsFab) }

        if (teamViewModel.defaultTeam != team) onTeamClicked(teamViewModel.defaultTeam)
        else fetchTournaments(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_pick_team -> bottomSheetDriver.showBottomSheet(
                requestCode = R.id.request_tournament_team_pick,
                title = getString(R.string.pick_team),
                fragment = TeamsFragment.newInstance()
        ).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onTeamClicked(item: Team) = disposables.add(teamViewModel.swap(team, item, tournamentViewModel) {
        disposables.clear()
        bottomSheetDriver.hideBottomSheet()

        watchForRoleChanges(team) { updateUi(fabShows = showsFab) }
        updateUi(toolbarTitle = getString(R.string.tournaments_title, team.name))
    }.subscribe(::onTournamentsUpdated, defaultErrorHandler::invoke)).let { Unit }

    override fun onTournamentClicked(tournament: Tournament) =
            navigator.push(TournamentDetailFragment.newInstance(tournament)).let { Unit }

    override fun onClick(view: View) = when {
        view.id == R.id.fab -> navigator.push(TournamentEditFragment.newInstance(Tournament.empty(team))).let { Unit }
        else -> Unit
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) = when (incomingFragment) {
        is TournamentEditFragment ->
            transaction.listDetailTransition(TournamentEditFragment.ARG_TOURNAMENT, incomingFragment)
        else -> super.augmentTransaction(transaction, incomingFragment)
    }

    private fun fetchTournaments(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else transientBarDriver.toggleProgress(true)

        disposables.add(tournamentViewModel.getMany(team, fetchLatest).subscribe(this::onTournamentsUpdated, defaultErrorHandler::invoke))
    }

    private fun onTournamentsUpdated(result: DiffUtil.DiffResult) {
        transientBarDriver.toggleProgress(false)
        val supportsTournaments = team.sport.supportsCompetitions()
        scrollManager.onDiff(result)
        scrollManager.updateForEmptyList(ListState(
                R.drawable.ic_trophy_white_24dp,
                if (supportsTournaments) R.string.no_tournaments
                else R.string.no_tournament_support))
    }

    companion object {

        fun newInstance(team: Team): TournamentsFragment = TournamentsFragment().apply { this.team = team }
    }
}
