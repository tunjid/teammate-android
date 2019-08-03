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

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.EventAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.EventViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil

import com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName

/**
 * Lists [events][com.mainstreetcode.teammate.model.Event]
 */

class EventsFragment : MainActivityFragment(), EventAdapter.EventAdapterListener {

    private lateinit var team: Team
    private lateinit var items: List<Differentiable>

    override val toolbarMenu: Int
        get() = R.menu.fragment_events

    override val fabStringResource: Int
        @StringRes
        get() = R.string.event_add

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_add_white_24dp

    override val toolbarTitle: CharSequence
        get() = getString(R.string.events_title, team.name)

    override fun getStableTag(): String {
        val superResult = super.getStableTag()
        val tempTeam = arguments!!.getParcelable<Team>(ARG_TEAM)

        return if (tempTeam != null) superResult + "-" + tempTeam.hashCode()
        else superResult
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        team = arguments!!.getParcelable(ARG_TEAM)!!
        items = eventViewModel.getModelList(team)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false)

        val refreshAction = Runnable{ disposables.add(eventViewModel.refresh(team).subscribe(this@EventsFragment::onEventsUpdated, defaultErrorHandler::accept)) }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(rootView, R.drawable.ic_event_white_24dp, R.string.no_events))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchEvents(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(EventAdapter(items, this))
                .withLinearLayoutManager()
                .build()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        fetchEvents(true)
        watchForRoleChanges(team) { this.togglePersistentUi() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_pick_team -> {
                TeamPickerFragment.change(requireActivity(), R.id.request_event_team_pick)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showsFab(): Boolean {
        return localRoleViewModel.hasPrivilegedRole()
    }

    override fun onEventClicked(item: Event) {
        showFragment(EventEditFragment.newInstance(item))
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                val event = Event.empty()
                event.team = teamViewModel.defaultTeam
                showFragment(EventEditFragment.newInstance(event))
            }
        }
    }

    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? {
        val superResult = super.provideFragmentTransaction(fragmentTo)

        if (fragmentTo.stableTag.contains(EventEditFragment::class.java.simpleName)) {
            val args = fragmentTo.arguments ?: return superResult

            val event = args.getParcelable<Event>(EventEditFragment.ARG_EVENT) ?: return superResult

            val viewHolder = scrollManager.findViewHolderForItemId(event.hashCode().toLong()) as? EventViewHolder
                    ?: return superResult

            return beginTransaction()
                    .addSharedElement(viewHolder.itemView, getTransitionName(event, R.id.fragment_header_background))
                    .addSharedElement(viewHolder.image, getTransitionName(event, R.id.fragment_header_thumbnail))

        }
        return superResult
    }

    private fun fetchEvents(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else toggleProgress(true)

        disposables.add(eventViewModel.getMany(team, fetchLatest).subscribe(this::onEventsUpdated, defaultErrorHandler::accept))
    }

    private fun onEventsUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        toggleProgress(false)
    }

    companion object {

        private const val ARG_TEAM = "team"

        fun newInstance(team: Team): EventsFragment {
            val fragment = EventsFragment()
            val args = Bundle()

            args.putParcelable(ARG_TEAM, team)
            fragment.arguments = args
            return fragment
        }
    }
}
