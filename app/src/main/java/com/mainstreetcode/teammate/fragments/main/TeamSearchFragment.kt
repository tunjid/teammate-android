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
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.TeamSearchAdapter
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.TeamSearchRequest
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.util.InstantSearch
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Searches for teams
 */

class TeamSearchFragment : MainActivityFragment(), View.OnClickListener, SearchView.OnQueryTextListener, TeamAdapter.AdapterListener {

    private lateinit var request: TeamSearchRequest
    private lateinit var instantSearch: InstantSearch<TeamSearchRequest, Team>

    private var createTeam: View? = null
    private var searchView: SearchView? = null

    override val staticViews: IntArray get() = EXCLUDED_VIEWS

    override val showsFab: Boolean get() = false

    override val showsToolBar: Boolean get() = false

    override val stableTag: String 
        get() {val superResult = super.stableTag
        val sportCode = arguments!!.getString(ARG_SPORT)

        return if (sportCode == null) superResult else "$superResult-$sportCode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        request = TeamSearchRequest.from(arguments!!.getString(ARG_SPORT))
        instantSearch = teamViewModel.instantSearch()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_team_search, container, false)
        searchView = root.findViewById(R.id.searchView)
        createTeam = root.findViewById(R.id.create_team)

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(root.findViewById(R.id.list_layout))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(TeamSearchAdapter(instantSearch.currentItems, this))
                .withGridLayoutManager(2)
                .build()

        searchView?.setOnQueryTextListener(this)
        searchView?.setIconifiedByDefault(false)
        searchView?.isIconified = false
        createTeam?.setOnClickListener(this)

        if (targetRequestCode != 0) {
            val items = instantSearch.currentItems
            items.clear()
            items.addAll(teamViewModel.getModelList(Team::class.java)
                    .filterIsInstance(Team::class.java)
                    .filter(this::isEligibleTeam))
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        subscribeToSearch()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView?.clearFocus()
        searchView = null
        createTeam = null
    }

    override fun onTeamClicked(item: Team) {
        val target = targetFragment
        val canPick = target is TeamAdapter.AdapterListener

        if (canPick) (target as TeamAdapter.AdapterListener).onTeamClicked(item)
        else navigator.show(JoinRequestFragment.joinInstance(item, userViewModel.currentUser))
    }

    override fun onClick(view: View) {
        if (view.id == R.id.create_team) navigator.show(TeamEditFragment.newCreateInstance())
    }

    override fun onQueryTextSubmit(s: String): Boolean = false

    override fun onQueryTextChange(queryText: String): Boolean {
        if (view == null || TextUtils.isEmpty(queryText)) return true
        instantSearch.postSearch(request.query(queryText))
        return true
    }

    private fun subscribeToSearch() {
        disposables.add(instantSearch.subscribe()
                .subscribe(scrollManager::onDiff, defaultErrorHandler::invoke))
    }

    private fun isEligibleTeam(team: Differentiable): Boolean =
            TextUtils.isEmpty(request.sport) || team !is Team || team.sport.code == request.sport

    companion object {

        private val EXCLUDED_VIEWS = intArrayOf(R.id.list_layout)
        private const val ARG_SPORT = "sport-code"

        fun newInstance(): TeamSearchFragment = TeamSearchFragment().apply { arguments = Bundle() }

        fun newInstance(sport: Sport): TeamSearchFragment {
            val fragment = newInstance()
            fragment.arguments!!.putString(ARG_SPORT, sport.code)

            return fragment
        }
    }
}
