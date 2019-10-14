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
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout.MODE_FIXED
import com.google.android.material.tabs.TabLayout.MODE_SCROLLABLE
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.TournamentRoundAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.UserViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.databinding.FragmentGamesParentBinding
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.processEmoji
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer

class TournamentDetailFragment : TeammatesBaseFragment(R.layout.fragment_games_parent) {

    private lateinit var tournament: Tournament
    private lateinit var competitor: Competitor

    private var binding: FragmentGamesParentBinding? = null
    private var viewHolder: EmptyViewHolder? = null

    internal var gamesRecycledViewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
        private set

    override val showsFab: Boolean get() = localRoleViewModel.hasPrivilegedRole() && !tournament.hasCompetitors()

    internal fun pending(competitor: Competitor): TournamentDetailFragment = apply {
        arguments!!.putParcelable(ARG_COMPETITOR, competitor)
    }

    override val stableTag: String
        get() =
            Gofer.tag(super.stableTag, arguments!!.getParcelable(ARG_TOURNAMENT)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        tournament = args!!.getParcelable(ARG_TOURNAMENT)!!
        competitor = args.getParcelable(ARG_COMPETITOR) ?: Competitor.empty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = FragmentGamesParentBinding.bind(view).run {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.tournament_fixtures),
                toolBarMenu = R.menu.fragment_tournament_detail,
                fabIcon = R.drawable.ic_group_add_white_24dp,
                fabText = R.string.add_tournament_competitors,
                fabShows = showsFab
        )
        viewHolder = EmptyViewHolder(view, R.drawable.ic_score_white_24dp, R.string.tournament_games_desc)

        viewPager?.adapter = TournamentRoundAdapter(tournament, childFragmentManager)
        viewPager?.currentItem = tournament.currentRound

        binding = this

        setUpWinner(tournament.numRounds)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val hasPrivilegedRole = localRoleViewModel.hasPrivilegedRole()
        menu.findItem(R.id.action_edit)?.isVisible = hasPrivilegedRole
        menu.findItem(R.id.action_delete)?.isVisible = hasPrivilegedRole
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_edit -> navigator.push(TournamentEditFragment.newInstance(tournament))
        R.id.action_standings -> navigator.push(StatDetailFragment.newInstance(tournament))
        R.id.action_delete -> AlertDialog.Builder(requireContext()).setTitle(getString(R.string.delete_tournament_prompt))
                .setPositiveButton(R.string.yes) { _, _ -> deleteTournament() }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        checkCompetitor()
        watchForRoleChanges(tournament.host) { updateUi(fabShows = showsFab, toolbarInvalidated = true) }

        val rounds = tournament.numRounds
        disposables.add(tournamentViewModel.checkForWinner(tournament)
                .subscribe({ setUpWinner(rounds) }, defaultErrorHandler::invoke))
    }

    override fun onDestroyView() {
        binding = null
        viewHolder = null
        super.onDestroyView()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fab) navigator.push(CompetitorsFragment.newInstance(tournament))
    }

    private fun checkCompetitor() {
        if (competitor.isEmpty || competitor.isAccepted) return
        if (restoredFromBackStack)
        // Don't prompt for the same competitor multiple times.
            disposables.add(competitorViewModel.updateCompetitor(competitor).subscribe(this::promptForCompetitor, ErrorHandler.EMPTY::invoke))
        else
            promptForCompetitor()
    }

    private fun promptForCompetitor() {
        if (competitor.isEmpty || competitor.isAccepted) return
        transientBarDriver.showChoices { choiceBar ->
            choiceBar.setText(getString(R.string.tournament_accept))
                    .setPositiveText(getText(R.string.accept))
                    .setNegativeText(getText(R.string.decline))
                    .setPositiveClickListener(View.OnClickListener { respond(true) })
                    .setNegativeClickListener(View.OnClickListener { respond(false) })
        }
    }

    private fun respond(accept: Boolean) {
        transientBarDriver.toggleProgress(true)
        disposables.add(competitorViewModel.respond(competitor, accept)
                .subscribe({ transientBarDriver.toggleProgress(false) }, defaultErrorHandler::invoke))
    }

    private fun deleteTournament() {
        disposables.add(tournamentViewModel.delete(tournament).subscribe(this::onTournamentDeleted, defaultErrorHandler::invoke))
    }

    private fun onTournamentDeleted(deleted: Tournament) {
        transientBarDriver.showSnackBar(getString(R.string.deleted_team, deleted.name))
        removeEnterExitTransitions()
        requireActivity().onBackPressed()
    }

    private fun setUpWinner(prevAdapterCount: Int) = binding?.run {
        val winnerText = root.findViewById<TextView>(R.id.winner)
        val winnerView = root.findViewById<ViewGroup>(R.id.item_container)
        val adapter = root.findViewById<ViewPager>(R.id.view_pager).adapter

        if (prevAdapterCount != tournament.numRounds && adapter != null)
            adapter.notifyDataSetChanged()

        TransitionManager.beginDelayedTransition(innerCoordinator, AutoTransition()
                .addTarget(tabLayout)
                .addTarget(viewPager)
                .addTarget(winnerView)
                .addTarget(winnerText))

        val hasCompetitors = tournament.numCompetitors > 0
        tabLayout.tabMode = if (tournament.numRounds > 4) MODE_SCROLLABLE else MODE_FIXED
        tabLayout.visibility = if (hasCompetitors) View.VISIBLE else View.GONE
        tabLayout.setupWithViewPager(viewPager)
        viewHolder?.setColor(R.attr.alt_empty_view_holder_tint)
        viewHolder?.toggle(!hasCompetitors)

        val winner = tournament.winner
        if (winner.isEmpty) return

        when (val competitive = winner.entity) {
            is User -> UserViewHolder(winnerView, UserAdapter.AdapterListener.asSAM { showCompetitor(winner) }).apply { bind(competitive) }
            is Team -> TeamViewHolder(winnerView, TeamAdapter.AdapterListener.asSAM { showCompetitor(winner) }).apply { bind(competitive) }
            else -> return
        }

        winnerText.visibility = View.VISIBLE
        winnerView.visibility = View.VISIBLE

        winnerText.text = getString(R.string.tournament_winner).processEmoji()
    } ?: Unit

    companion object {

        private const val ARG_TOURNAMENT = "tournament"
        private const val ARG_COMPETITOR = "competitor"

        fun newInstance(tournament: Tournament): TournamentDetailFragment = TournamentDetailFragment().apply {
            arguments = bundleOf(ARG_TOURNAMENT to tournament)
            setEnterExitTransitions()
        }
    }
}
