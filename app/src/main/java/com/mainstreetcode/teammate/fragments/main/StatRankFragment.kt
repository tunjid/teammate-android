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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.statRankAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.enums.StatType
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidx.recyclerview.diff.Differentiable

class StatRankFragment : TeammatesBaseFragment(R.layout.fragment_stat_rank) {

    private lateinit var type: StatType
    private lateinit var tournament: Tournament
    private lateinit var statRanks: List<Differentiable>

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
        statRanks = tournamentViewModel.getStatRanks(tournament)
        type = StatType.empty().apply { update(tournament.sport.statTypeFromCode("")) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val spinner = view.findViewById<Spinner>(R.id.spinner)

        scrollManager = ScrollManager.with<RecyclerView.ViewHolder>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_medal_24dp, R.string.no_stat_ranks))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(statRankAdapter(
                        ::statRanks
                ) { statRank -> navigator.push(UserEditFragment.newInstance(statRank.user)) })
                .withLinearLayoutManager()
                .build()

        scrollManager.setViewHolderColor(R.attr.alt_empty_view_holder_tint)

        val statTypes = tournament.sport.stats.toTypedArray()
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, statTypes)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                type.update(statTypes[position])
                fetchStandings()
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }

    override fun onResume() {
        super.onResume()
        fetchStandings()
    }

    override fun togglePersistentUi() = Unit /* Do nothing */

    private fun fetchStandings() {
        transientBarDriver.toggleProgress(true)
        disposables.add(tournamentViewModel.getStatRank(tournament, type).subscribe(this::onTournamentsUpdated, defaultErrorHandler::invoke))
    }

    private fun onTournamentsUpdated(diff: DiffUtil.DiffResult) {
        scrollManager.onDiff(diff)
        transientBarDriver.toggleProgress(false)
    }

    companion object {

        private const val ARG_TOURNAMENT = "tournament"

        fun newInstance(tournament: Tournament): StatRankFragment = StatRankFragment().apply { arguments = bundleOf(ARG_TOURNAMENT to tournament) }
    }
}
