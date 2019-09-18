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
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TournamentAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.ListState
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Lists [tournaments][Event]
 */

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class TournamentsFragment : MainActivityFragment(), TournamentAdapter.TournamentAdapterListener {

    private lateinit var team: Team
    private lateinit var items: List<Differentiable>

    override val toolbarMenu: Int get() = R.menu.fragment_tournaments

    override val fabStringResource: Int @StringRes get() = R.string.tournament_add

    override val fabIconResource: Int @DrawableRes get() = R.drawable.ic_add_white_24dp

    override val toolbarTitle: CharSequence get() = getString(R.string.tournaments)

    override val showsFab: Boolean get() = team.sport.supportsCompetitions() && localRoleViewModel.hasPrivilegedRole()

    override val stableTag: String
        get() {
            val superResult = super.stableTag
            val tempTeam = arguments!!.getParcelable<Team>(ARG_TEAM)

            return if (tempTeam != null) superResult + "-" + tempTeam.hashCode()
            else superResult
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        team = arguments!!.getParcelable(ARG_TEAM)!!
        items = tournamentViewModel.getModelList(team)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false)

        val refreshAction = { disposables.add(tournamentViewModel.refresh(team).subscribe(this::onTournamentsUpdated, defaultErrorHandler::invoke)).let { Unit } }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(rootView, R.drawable.ic_trophy_white_24dp, R.string.no_tournaments))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchTournaments(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(TournamentAdapter(items, this))
                .withLinearLayoutManager()
                .build()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        fetchTournaments(true)
        watchForRoleChanges(team, this::togglePersistentUi)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_pick_team -> TeamPickerFragment.change(requireActivity(), R.id.request_tournament_team_pick).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onTournamentClicked(tournament: Tournament) =
            navigator.show(TournamentDetailFragment.newInstance(tournament)).let { Unit }

    override fun onClick(view: View) = when {
        view.id == R.id.fab -> navigator.show(TournamentEditFragment.newInstance(Tournament.empty(team))).let { Unit }
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

        private const val ARG_TEAM = "team"

        fun newInstance(team: Team): TournamentsFragment = TournamentsFragment().apply { arguments = bundleOf(ARG_TEAM to team) }
    }
}
