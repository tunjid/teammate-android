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
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.StatAggregateAdapter
import com.mainstreetcode.teammate.adapters.StatAggregateRequestAdapter
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.StatAggregate
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ExpandingToolbar
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

class StatAggregateFragment : TeammatesBaseFragment(),
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener,
        StatAggregateRequestAdapter.AdapterListener {

    private lateinit var request: StatAggregate.Request
    private var expandingToolbar: ExpandingToolbar? = null
    private var searchScrollManager: ScrollManager<*>? = null

    private lateinit var items: List<Differentiable>

    override val showsFab: Boolean get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        request = StatAggregate.Request.empty()
        items = statViewModel.statAggregates
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(toolbarShows = false)

        searchScrollManager = ScrollManager.with<BaseViewHolder<*>>(view.findViewById(R.id.search_options))
                .withAdapter(StatAggregateRequestAdapter(request, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build()

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_stat_white_24dp, R.string.stat_aggregate_empty))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this.fetchAggregates() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(StatAggregateAdapter(items))
                .withLinearLayoutManager()
                .build()

        expandingToolbar = ExpandingToolbar.create(view.findViewById(R.id.card_view_wrapper)) { this.fetchAggregates() }
        expandingToolbar?.setTitleIcon(false)
        expandingToolbar?.setTitle(R.string.stat_aggregate_get)

        scrollManager.notifyDataSetChanged()

        if (!restoredFromBackStack) expandingToolbar?.changeVisibility(false)
    }

    override fun onDestroyView() {
        expandingToolbar = null
        searchScrollManager = null
        super.onDestroyView()
    }

    override fun onKeyBoardChanged(appeared: Boolean) {
        super.onKeyBoardChanged(appeared)
        if (!appeared && bottomSheetDriver.isBottomSheetShowing) bottomSheetDriver.hideBottomSheet()
    }

    override fun onUserPicked(user: User) = pick(UserSearchFragment.newInstance())

    override fun onTeamPicked(team: Team) = pick(TeamSearchFragment.newInstance(request.sport))

    override fun onTeamClicked(item: Team) = updateEntity(item)

    override fun onUserClicked(item: User) = updateEntity(item)

    private fun fetchAggregates() {
        transientBarDriver.toggleProgress(true)
        disposables.add(statViewModel.aggregate(request).subscribe({ result ->
            transientBarDriver.toggleProgress(false)
            scrollManager.onDiff(result)
        }, defaultErrorHandler::invoke))
    }

    private fun updateEntity(item: Competitive) {
        when (item) {
            is User -> request.updateUser(item)
            is Team -> request.updateTeam(item)
            else -> return
        }

        searchScrollManager?.notifyDataSetChanged()
        bottomSheetDriver.hideBottomSheet()
        hideKeyboard()
    }

    private fun pick(caller: TeammatesBaseFragment) = bottomSheetDriver.showBottomSheet {
        caller.setTargetFragment(this@StatAggregateFragment, R.id.request_competitor_pick)
        menuRes = R.menu.empty
        fragment = caller
    }

    companion object {

        fun newInstance(): StatAggregateFragment = StatAggregateFragment().apply {
            arguments = Bundle()
            setEnterExitTransitions()
        }
    }

}
