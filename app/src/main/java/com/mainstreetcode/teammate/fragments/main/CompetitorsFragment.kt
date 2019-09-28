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
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.CompetitorAdapter
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidx.functions.collections.transform
import com.tunjid.androidx.recyclerview.ListManager
import com.tunjid.androidx.recyclerview.SwipeDragOptions
import com.tunjid.androidx.recyclerview.diff.Differentiable
import java.util.*
import kotlin.math.min

class CompetitorsFragment : TeammatesBaseFragment(R.layout.fragment_competitors),
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener {

    private lateinit var tournament: Tournament
    private lateinit var entities: MutableList<Competitive>
    private lateinit var competitors: MutableList<Competitor>
    private lateinit var competitorDifferentiables: MutableList<Differentiable>

    override val showsFab: Boolean get() = !bottomSheetDriver.isBottomSheetShowing && competitors.isNotEmpty()

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
        entities = ArrayList()
        competitors = entities.transform(Competitor.Companion::empty, Competitor::entity)
        competitorDifferentiables = competitors.transform({ it as Differentiable }, { it as Competitor })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.add_tournament_competitors),
                fabShows = showsFab,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabText = R.string.save_tournament_competitors
        )

        scrollManager = ScrollManager.with<CompetitorViewHolder>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_bracket_white_24dp, R.string.add_tournament_competitors_detail))
                .withAdapter(object : CompetitorAdapter(competitorDifferentiables, AdapterListener.asSAM {}) {
                    override fun getItemId(position: Int): Long = entities[position].hashCode().toLong()

                    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CompetitorViewHolder {
                        val holder = super.onCreateViewHolder(viewGroup, viewType)
                        holder.dragHandle.visibility = View.VISIBLE
                        return holder
                    }
                })
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .withSwipeDragOptions(SwipeDragOptions(
                        movementFlagFunction = { ListManager.SWIPE_DRAG_ALL_DIRECTIONS },
                        swipeConsumer = { holder, _ -> removeCompetitor(holder) },
                        dragHandleFunction = CompetitorViewHolder::dragHandle,
                        longPressDragSupplier = { false },
                        itemViewSwipeSupplier = { true },
                        dragConsumer = this::moveCompetitor
                ))
                .build()

        view.findViewById<View>(R.id.add_competitor).setOnClickListener { findCompetitor() }
    }

    override fun onResume() {
        super.onResume()
        scrollManager.notifyDataSetChanged()
    }

    override fun onKeyBoardChanged(appeared: Boolean) {
        super.onKeyBoardChanged(appeared)
        if (!appeared && bottomSheetDriver.isBottomSheetShowing) bottomSheetDriver.hideBottomSheet()
    }

    override fun onUserClicked(item: User) =
            if (entities.contains(item)) transientBarDriver.showSnackBar(getString(R.string.competitor_exists))
            else addCompetitor(item)

    override fun onTeamClicked(item: Team) =
            if (entities.contains(item)) transientBarDriver.showSnackBar(getString(R.string.competitor_exists))
            else addCompetitor(item)

    override fun onClick(view: View) = when (view.id) {
        R.id.fab -> addCompetitors()
        else -> Unit
    }

    private fun findCompetitor() = bottomSheetDriver.showBottomSheet {
        val isBetweenUsers = User.COMPETITOR_TYPE == tournament.refPath

        menuRes = R.menu.empty
        if (isBetweenUsers) title = getString(R.string.add_competitor)

        if (isBetweenUsers) fragment = TeamMembersFragment.newInstance(tournament.host)
        else if (Team.COMPETITOR_TYPE == tournament.refPath) fragment = TeamSearchFragment.newInstance(tournament.sport)

        fragment?.setTargetFragment(this@CompetitorsFragment, R.id.request_competitor_pick)
    }

    private fun addCompetitor(item: Competitive) {
        if (tournament.refPath != item.refType) return

        entities.add(item)
        scrollManager.notifyDataSetChanged()
        bottomSheetDriver.hideBottomSheet()
        hideKeyboard()
    }

    private fun addCompetitors() {
        disposables.add(tournamentViewModel.addCompetitors(tournament, competitors).subscribe({ requireActivity().onBackPressed() }, defaultErrorHandler::invoke))
    }

    private fun moveCompetitor(start: CompetitorViewHolder, end: CompetitorViewHolder) {
        val from = start.adapterPosition
        val to = end.adapterPosition

        swap(from, to)
        scrollManager.notifyItemMoved(from, to)
        scrollManager.notifyItemChanged(from)
        scrollManager.notifyItemChanged(to)
    }

    private fun removeCompetitor(viewHolder: CompetitorViewHolder) {
        val position = viewHolder.adapterPosition
        val minMax = remove(position)

        scrollManager.notifyItemRemoved(position)
        // Only necessary to rebind views lower so they have the right position
        scrollManager.notifyItemRangeChanged(minMax.first, minMax.second)
    }

    private fun swap(from: Int, to: Int) {
        if (from < to) for (i in from until to) Collections.swap(competitorDifferentiables, i, i + 1)
        else for (i in from downTo to + 1) Collections.swap(competitorDifferentiables, i, i - 1)
    }

    private fun remove(position: Int): Pair<Int, Int> {
        competitorDifferentiables.removeAt(position)

        val lastIndex = competitorDifferentiables.size - 1
        return Pair(min(position, lastIndex), lastIndex)
    }

    companion object {

        private const val ARG_TOURNAMENT = "tournament"

        fun newInstance(tournament: Tournament): CompetitorsFragment = CompetitorsFragment().apply { arguments = bundleOf(ARG_TOURNAMENT to tournament) }
    }
}
