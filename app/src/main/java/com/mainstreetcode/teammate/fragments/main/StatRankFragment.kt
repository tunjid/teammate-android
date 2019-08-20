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
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.StatRankAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.enums.StatType
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.simpleAdapterListener
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

class StatRankFragment : MainActivityFragment() {

    private lateinit var type: StatType
    private lateinit var tournament: Tournament
    private lateinit var statRanks: List<Differentiable>

    override val showsFab: Boolean get() = false

    override fun getStableTag(): String {
        val superResult = super.getStableTag()
        val tempTournament = arguments!!.getParcelable<Tournament>(ARG_TOURNAMENT)

        return if (tempTournament != null) superResult + "-" + tempTournament.hashCode()
        else superResult
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tournament = arguments!!.getParcelable(ARG_TOURNAMENT)!!
        statRanks = tournamentViewModel.getStatRanks(tournament)
        type = tournament.sport.statTypeFromCode("")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_stat_rank, container, false)
        val spinner = root.findViewById<Spinner>(R.id.spinner)

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(root.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(root, R.drawable.ic_medal_24dp, R.string.no_stat_ranks))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(StatRankAdapter(
                        statRanks,
                        simpleAdapterListener { statRank -> showFragment(UserEditFragment.newInstance(statRank.user)) }))
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

        return root
    }

    override fun onResume() {
        super.onResume()
        fetchStandings()
    }

    override fun togglePersistentUi() = Unit /* Do nothing */

    private fun fetchStandings() {
        toggleProgress(true)
        disposables.add(tournamentViewModel.getStatRank(tournament, type).subscribe(this::onTournamentsUpdated, defaultErrorHandler::invoke))
    }

    private fun onTournamentsUpdated(diff: DiffUtil.DiffResult) {
        scrollManager.onDiff(diff)
        toggleProgress(false)
    }

    companion object {

        private const val ARG_TOURNAMENT = "tournament"

        fun newInstance(tournament: Tournament): StatRankFragment = StatRankFragment().apply { arguments = bundleOf(ARG_TOURNAMENT to tournament) }
    }
}
