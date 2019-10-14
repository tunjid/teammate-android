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
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.CompetitorAdapter
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Item.Companion.COMPETITOR
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

/**
 * Lists [tournaments][Event]
 */

class DeclinedCompetitionsFragment : TeammatesBaseFragment(R.layout.fragment_list_with_refresh),
        CompetitorAdapter.AdapterListener {

    private lateinit var items: List<Differentiable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        items = competitorViewModel.getModelList(User::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle =  getString(R.string.competitors_declined),
                fabShows = showsFab
        )

        val refreshAction = {
            disposables.add(competitorViewModel.refresh(User::class.java)
                    .subscribe(this@DeclinedCompetitionsFragment::onCompetitorsUpdated, defaultErrorHandler::invoke)).let { Unit }
        }

        scrollManager = ScrollManager.with<CompetitorViewHolder>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_thumb_down_24dp, R.string.no_competitors_declined))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchCompetitions(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(object : CompetitorAdapter(items, this) {
                    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CompetitorViewHolder =
                            if (viewType != COMPETITOR) super.onCreateViewHolder(viewGroup, viewType)
                            else CompetitorViewHolder(viewGroup.inflate(R.layout.viewholder_list_item), delegate)
                })
                .withLinearLayoutManager()
                .build()
    }

    override fun onResume() {
        super.onResume()
        fetchCompetitions(true)
    }

    override fun onCompetitorClicked(competitor: Competitor) {
        AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.accept_competition))
                .setPositiveButton(R.string.yes) { _, _ -> accept(competitor) }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .setNeutralButton(R.string.event_details) { _, _ ->
                    val fragment = if (!competitor.game.isEmpty)
                        GameFragment.newInstance(competitor.game)
                    else if (!competitor.tournament.isEmpty)
                        TournamentDetailFragment.newInstance(competitor.tournament).pending(competitor)
                    else
                        null
                    if (fragment != null) navigator.push(fragment)
                }
                .show()
    }

    private fun accept(competitor: Competitor) {
        transientBarDriver.toggleProgress(true)
        disposables.add(competitorViewModel.respond(competitor, true)
                .subscribe({ diffResult ->
                    transientBarDriver.toggleProgress(false)
                    scrollManager.onDiff(diffResult)
                }, defaultErrorHandler::invoke))
    }

    private fun fetchCompetitions(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else transientBarDriver.toggleProgress(true)

        disposables.add(competitorViewModel.getMany(User::class.java, fetchLatest).subscribe(this::onCompetitorsUpdated, defaultErrorHandler::invoke))
    }

    private fun onCompetitorsUpdated(result: DiffUtil.DiffResult) {
        transientBarDriver.toggleProgress(false)
        scrollManager.onDiff(result)
    }

    companion object {

        fun newInstance(): DeclinedCompetitionsFragment = DeclinedCompetitionsFragment().apply { arguments = Bundle() }
    }
}
