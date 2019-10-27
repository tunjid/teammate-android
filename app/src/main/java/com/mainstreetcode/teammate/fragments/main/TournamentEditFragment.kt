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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TournamentEditAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.mainstreetcode.teammate.viewmodel.gofers.TournamentGofer
import com.tunjid.androidx.view.util.InsetFlags

/**
 * Edits a Team member
 */

class TournamentEditFragment : HeaderedFragment<Tournament>(R.layout.fragment_headered),
        TournamentEditAdapter.AdapterListener {

    private var showingPrompt: Boolean = false
    override lateinit var headeredModel: Tournament
        private set

    private lateinit var gofer: TournamentGofer

    private val fabStringResource: Int @StringRes get() = if (headeredModel.isEmpty) R.string.tournament_create else R.string.tournament_update

    private val toolbarTitle: CharSequence get() = gofer.getToolbarTitle(this)

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = gofer.canEditAfterCreation()

    override val sport: Sport get() = headeredModel.sport

    override val stableTag: String get() = Gofer.tag(super.stableTag, arguments!!.getParcelable(ARG_TOURNAMENT)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = arguments
        headeredModel = arguments!!.getParcelable(ARG_TOURNAMENT)!!
        gofer = tournamentViewModel.gofer(headeredModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = toolbarTitle,
                toolBarMenu = R.menu.fragment_tournament_edit,
                fabText = fabStringResource,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabShows = showsFab
        )
        scrollManager = ScrollManager.with<BaseViewHolder<*>>(view.findViewById(R.id.model_list))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(TournamentEditAdapter(gofer.items, this))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .onLayoutManager(this::setSpanSizeLookUp)
                .withGridLayoutManager(2)
                .build()
                .apply { recyclerView?.requestFocus() }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_rounds)?.isVisible = !headeredModel.isEmpty
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_rounds -> navigator.push(TournamentDetailFragment.newInstance(headeredModel))
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        tournamentViewModel.clearNotifications(headeredModel)
    }

    override fun gofer(): Gofer<Tournament> = gofer

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        updateUi(toolbarInvalidated = true)
        transientBarDriver.toggleProgress(false)
        scrollManager.onDiff(result)
        viewHolder?.bind(headeredModel)
        if (!headeredModel.isEmpty && headeredModel.numCompetitors == 0) promptForCompetitors()
    }

    override fun onPrepComplete() {
        scrollManager.notifyDataSetChanged()
        updateUi(toolbarInvalidated = true)
        super.onPrepComplete()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                val wasEmpty = headeredModel.isEmpty
                transientBarDriver.toggleProgress(true)
                disposables.add(gofer.save().subscribe({ diffResult ->
                    val stringRes = if (wasEmpty) R.string.added_user else R.string.updated_user
                    transientBarDriver.showSnackBar(getString(stringRes, headeredModel.name))

                    if (wasEmpty)
                        navigator.push(TournamentDetailFragment.newInstance(headeredModel))
                    else
                        onModelUpdated(diffResult)
                }, defaultErrorHandler::invoke))
            }
        }
    }

    override fun canEditBeforeCreation(): Boolean = gofer.canEditBeforeCreation()

    override fun canEditAfterCreation(): Boolean = gofer.canEditAfterCreation()

    private fun promptForCompetitors() {
        if (showingPrompt) return

        showingPrompt = true
        transientBarDriver.showSnackBar { snackbar ->
            snackbar.setText(getString(R.string.add_tournament_competitors_prompt))
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(bar: Snackbar?, event: Int) {
                            showingPrompt = false
                        }
                    })
                    .setAction(R.string.okay) { navigator.push(CompetitorsFragment.newInstance(headeredModel)) }
        }
    }

    private fun setSpanSizeLookUp(layoutManager: RecyclerView.LayoutManager) {
        (layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                    if (gofer.items[position] is Competitor) 1 else 2
        }
    }

    companion object {

        internal const val ARG_TOURNAMENT = "tournament"

        fun newInstance(tournament: Tournament): TournamentEditFragment = TournamentEditFragment().apply {
            arguments = bundleOf(ARG_TOURNAMENT to tournament)
        }
    }
}
