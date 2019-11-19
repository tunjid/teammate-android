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
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.EventAdapterListener
import com.mainstreetcode.teammate.adapters.Shell
import com.mainstreetcode.teammate.adapters.eventAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.swap
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.recyclerview.diff.Differentiable

/**
 * Lists [events][com.mainstreetcode.teammate.model.Event]
 */

class EventsFragment : TeammatesBaseFragment(R.layout.fragment_list_with_refresh),
        Shell.TeamAdapterListener,
        EventAdapterListener {

    private var team by args<Team>()

    private val items: List<Differentiable>
        get() = eventViewModel.getModelList(team)

    override val showsFab: Boolean get() = localRoleViewModel.hasPrivilegedRole()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolBarMenu = R.menu.fragment_events,
                toolbarTitle = getString(R.string.events_title, team.name),
                fabText = R.string.event_add,
                fabIcon = R.drawable.ic_add_white_24dp,
                fabShows = showsFab
        )

        val refreshAction = { disposables.add(eventViewModel.refresh(team).subscribe(this@EventsFragment::onEventsUpdated, defaultErrorHandler::invoke)).let { Unit } }

        scrollManager = ScrollManager.with<RecyclerView.ViewHolder>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_event_white_24dp, R.string.no_events))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchEvents(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(eventAdapter(::items, this))
                .withLinearLayoutManager()
                .build()
    }

    override fun onResume() {
        super.onResume()

        watchForRoleChanges(team) { updateUi(fabShows = showsFab) }

        if (teamViewModel.defaultTeam != team) onTeamClicked(teamViewModel.defaultTeam)
        else fetchEvents(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_pick_team -> bottomSheetDriver.showBottomSheet(
                requestCode = R.id.request_event_team_pick,
                title = getString(R.string.pick_team),
                fragment = TeamsFragment.newInstance()
        ).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onTeamClicked(item: Team) = disposables.add(teamViewModel.swap(team, item, eventViewModel) {
        disposables.clear()
        bottomSheetDriver.hideBottomSheet()
        watchForRoleChanges(team) { updateUi(fabShows = showsFab) }
        updateUi(toolbarTitle = getString(R.string.events_title, team.name))
    }.subscribe(::onEventsUpdated, defaultErrorHandler::invoke)).let { Unit }

    override fun onEventClicked(item: Event) {
        navigator.push(EventEditFragment.newInstance(item))
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                val event = Event.empty().apply { updateTeam(teamViewModel.defaultTeam) }
                navigator.push(EventEditFragment.newInstance(event))
            }
        }
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) = when (incomingFragment) {
        is EventEditFragment ->
            transaction.listDetailTransition(EventEditFragment.ARG_EVENT, incomingFragment)

        else -> super.augmentTransaction(transaction, incomingFragment)
    }

    private fun fetchEvents(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else transientBarDriver.toggleProgress(true)

        disposables.add(eventViewModel.getMany(team, fetchLatest).subscribe(this::onEventsUpdated, defaultErrorHandler::invoke))
    }

    private fun onEventsUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        transientBarDriver.toggleProgress(false)
    }

    companion object {
        fun newInstance(team: Team): EventsFragment = EventsFragment().apply { this.team = team }
    }
}
