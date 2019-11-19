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
import androidx.core.os.bundleOf
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.StandingsAdapterListener
import com.mainstreetcode.teammate.adapters.standingsAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.StandingRowViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Standings
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.SyncedScrollManager
import com.mainstreetcode.teammate.util.SyncedScrollView

/**
 * Lists [tournaments][Event]
 */

class StandingsFragment : TeammatesBaseFragment(R.layout.fragment_standings),
        StandingsAdapterListener {

    private lateinit var tournament: Tournament
    private lateinit var standings: Standings
    private var viewHolder: StandingRowViewHolder? = null
    private val syncedScrollManager = SyncedScrollManager()

    override val showsFab: Boolean get() = false

    override val stableTag: String
        get() {
            val superResult = super.stableTag
            val tempTournament = arguments!!.getParcelable<Tournament>(ARG_TOURNAMENT)

            return if (tempTournament != null) superResult + "-" + tempTournament.hashCode()
            else superResult
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tournament = arguments!!.getParcelable(ARG_TOURNAMENT)!!
        standings = tournamentViewModel.getStandings(tournament)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val spacerToolbar = view.findViewById<View>(R.id.spacer_toolbar)

        val refreshAction = { fetchStandings(true) }

        scrollManager = ScrollManager.with<StandingRowViewHolder>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view.findViewById(R.id.empty_container), R.drawable.ic_table_24dp, R.string.tournament_no_standings))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchStandings(false) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(standingsAdapter(standings::table, this))
                .withLinearLayoutManager()
                .build()

        scrollManager.setViewHolderColor(R.attr.alt_empty_view_holder_tint)

        viewHolder = StandingRowViewHolder(spacerToolbar.findViewById(R.id.item_container), this)
        viewHolder?.thumbnail?.visibility = View.GONE
        viewHolder?.position?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        fetchStandings(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewHolder = null
        syncedScrollManager.clearClients()
    }

    override fun togglePersistentUi() = Unit /* Do nothing */

    override fun addScrollNotifier(notifier: SyncedScrollView) =
            syncedScrollManager.addScrollClient(notifier)

    override fun onCompetitorClicked(competitor: Competitor) =
            showCompetitor(competitor)

    private fun fetchStandings(isRefreshing: Boolean) {
        if (isRefreshing) scrollManager.setRefreshing()
        else transientBarDriver.toggleProgress(true)

        disposables.add(tournamentViewModel.fetchStandings(tournament)
                .subscribe(this::onTournamentsUpdated, defaultErrorHandler::invoke))
    }

    private fun onTournamentsUpdated() {
        scrollManager.notifyDataSetChanged()
        viewHolder?.bindColumns(standings.columnNames)
        transientBarDriver.toggleProgress(false)
        if (!restoredFromBackStack) syncedScrollManager.jog()
    }

    companion object {

        private const val ARG_TOURNAMENT = "team"

        fun newInstance(team: Tournament): StandingsFragment = StandingsFragment().apply { arguments = bundleOf(ARG_TOURNAMENT to team) }
    }
}
