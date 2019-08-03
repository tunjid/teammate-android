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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.CompetitorAdapter
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.functions.collections.Lists
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.util.*
import kotlin.math.min

class CompetitorsFragment : MainActivityFragment(), UserAdapter.AdapterListener, TeamAdapter.AdapterListener {

    private lateinit var tournament: Tournament
    private lateinit var entities: MutableList<Competitive>
    private lateinit var competitors: List<Competitor>
    private lateinit var competitorDifferentiables: MutableList<Differentiable>

    override val fabStringResource: Int
        @StringRes
        get() = R.string.save_tournament_competitors

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_check_white_24dp

    override val toolbarTitle: CharSequence
        get() = getString(R.string.add_tournament_competitors)

    override fun getStableTag(): String {
        val superResult = super.getStableTag()
        val tempTournament = arguments!!.getParcelable<Tournament>(ARG_TOURNAMENT)

        return if (tempTournament != null)
            superResult + "-" + tempTournament.hashCode()
        else
            superResult
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tournament = arguments!!.getParcelable(ARG_TOURNAMENT)!!
        entities = ArrayList()
        competitors = Lists.transform(entities, { Competitor.empty(it) }, { it.entity })
        competitorDifferentiables = Lists.transform(competitors, { identity -> identity as Differentiable }, { i -> i as Competitor })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_competitors, container, false)
        scrollManager = ScrollManager.with<CompetitorViewHolder>(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(rootView, R.drawable.ic_bracket_white_24dp, R.string.add_tournament_competitors_detail))
                .withAdapter(object : CompetitorAdapter(competitorDifferentiables, AdapterListener.asSAM {}) {
                    override fun getItemId(position: Int): Long =
                            entities[position].hashCode().toLong()

                    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CompetitorViewHolder {
                        val holder = super.onCreateViewHolder(viewGroup, viewType)
                        holder.dragHandle.visibility = View.VISIBLE
                        return holder
                    }
                })
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .withSwipeDragOptions(ScrollManager.swipeDragOptionsBuilder<CompetitorViewHolder>()
                        .setMovementFlagsFunction { ScrollManager.SWIPE_DRAG_ALL_DIRECTIONS }
                        .setSwipeConsumer { holder, _ -> removeCompetitor(holder) }
                        .setDragHandleFunction { it.dragHandle }
                        .setLongPressDragEnabledSupplier { false }
                        .setItemViewSwipeSupplier { true }
                        .setDragConsumer(this::moveCompetitor)
                        .build())
                .build()

        rootView.findViewById<View>(R.id.add_competitor).setOnClickListener { findCompetitor() }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        scrollManager.notifyDataSetChanged()
    }

    override fun onKeyBoardChanged(appeared: Boolean) {
        super.onKeyBoardChanged(appeared)
        if (!appeared && isBottomSheetShowing) hideBottomSheet()
    }

    override fun showsFab(): Boolean = !isBottomSheetShowing && competitors.isNotEmpty()

    override fun onUserClicked(item: User) {
        if (entities.contains(item)) showSnackbar(getString(R.string.competitor_exists))
        else addCompetitor(item)
    }

    override fun onTeamClicked(item: Team) {
        if (entities.contains(item)) showSnackbar(getString(R.string.competitor_exists))
        else addCompetitor(item)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> addCompetitors()
        }
    }

    private fun findCompetitor() {
        val isBetweenUsers = User.COMPETITOR_TYPE == tournament.refPath
        var fragment: BaseFragment? = null

        if (isBetweenUsers) fragment = TeamMembersFragment.newInstance(tournament.host)
        else if (Team.COMPETITOR_TYPE == tournament.refPath) fragment = TeamSearchFragment.newInstance(tournament.sport)

        fragment ?: return
        fragment.setTargetFragment(this, R.id.request_competitor_pick)

        val builder = BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(fragment)

        if (isBetweenUsers) builder.setTitle(getString(R.string.add_competitor))
        showBottomSheet(builder.build())
    }

    private fun addCompetitor(item: Competitive) {
        if (tournament.refPath != item.refType) return

        entities.add(item)
        scrollManager.notifyDataSetChanged()
        hideKeyboard()
    }

    private fun addCompetitors() {
        disposables.add(tournamentViewModel.addCompetitors(tournament, competitors).subscribe({ requireActivity().onBackPressed() }, defaultErrorHandler::accept))
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
