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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.GameAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.yes
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Lists [tournaments][Event]
 */

class GamesChildFragment : MainActivityFragment(), GameAdapter.AdapterListener {

    private var round: Int = 0
    private lateinit var tournament: Tournament
    private lateinit var items: List<Differentiable>

    override fun getStableTag(): String {
        val superResult = super.getStableTag()
        val tempTournament = arguments!!.getParcelable<Tournament>(ARG_TOURNAMENT)
        val round = arguments!!.getInt(ARG_ROUND)

        return if (tempTournament != null)
            superResult + "-" + tempTournament.hashCode() + "-" + round
        else
            superResult
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        round = arguments!!.getInt(ARG_ROUND)
        tournament = arguments!!.getParcelable(ARG_TOURNAMENT)!!
        items = gameViewModel.getGamesForRound(tournament, round)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_games_child, container, false)
        val fragment = parentFragment
        val recycledViewPool = if (fragment is TournamentDetailFragment)
            fragment.gamesRecycledViewPool
        else
            RecyclerView.RecycledViewPool()

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(rootView, R.drawable.ic_trophy_white_24dp, R.string.no_tournaments))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout)) { this.onRefresh() }
                .withEndlessScroll { fetchTournaments(false) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(GameAdapter(items, this))
                .withRecycledViewPool(recycledViewPool)
                .withLinearLayoutManager()
                .build()

        scrollManager.setViewHolderColor(R.attr.alt_empty_view_holder_tint)

        return rootView
    }

    private fun onRefresh() {
        disposables.add(gameViewModel.fetchGamesInRound(tournament, round).subscribe(this::onGamesUpdated, defaultErrorHandler::accept))
    }

    override fun onResume() {
        super.onResume()
        fetchTournaments(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
         R.id.action_pick_team -> TeamPickerFragment.change(requireActivity(), R.id.request_tournament_team_pick).yes
        else -> super.onOptionsItemSelected(item)
     }

    override fun togglePersistentUi() = Unit /* Do nothing */

    override fun showsFab(): Boolean = false

    override fun onGameClicked(game: Game) {
        showFragment(GameFragment.newInstance(game))
    }

    private fun fetchTournaments(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else toggleProgress(true)

        onRefresh()
    }

    private fun onGamesUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        toggleProgress(false)
    }

    companion object {

        private const val ARG_TOURNAMENT = "tournament"
        private const val ARG_ROUND = "round"

        fun newInstance(tournament: Tournament, round: Int): GamesChildFragment {
            val fragment = GamesChildFragment()
            val args = Bundle()

            args.putParcelable(ARG_TOURNAMENT, tournament)
            args.putInt(ARG_ROUND, round)
            fragment.arguments = args
            return fragment
        }
    }
}
