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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Searches for teams
 */

class TeamsFragment : MainActivityFragment(R.layout.fragment_list_with_refresh),
        TeamAdapter.AdapterListener {

    private lateinit var roles: List<Differentiable>

    private val isTeamPicker: Boolean get() = targetRequestCode != 0

    override val staticViews: IntArray get() = EXCLUDED_VIEWS

    override val showsFab: Boolean get() = !isTeamPicker || roles.isEmpty()

    private val emptyDrawable: Int
        @DrawableRes
        get() = when (targetRequestCode) {
            R.id.request_chat_team_pick -> R.drawable.ic_message_black_24dp
            R.id.request_game_team_pick -> R.drawable.ic_score_white_24dp
            R.id.request_event_team_pick -> R.drawable.ic_event_white_24dp
            R.id.request_media_team_pick -> R.drawable.ic_video_library_black_24dp
            R.id.request_tournament_team_pick -> R.drawable.ic_trophy_white_24dp
            else -> R.drawable.ic_group_black_24dp
        }

    private val emptyText: Int
        @StringRes
        get() = when (targetRequestCode) {
            R.id.request_event_team_pick -> R.string.no_team_event
            R.id.request_chat_team_pick -> R.string.no_team_chat
            R.id.request_media_team_pick -> R.string.no_team_media
            R.id.request_tournament_team_pick -> R.string.no_team_tournament
            else -> R.string.no_team
        }

    override val stableTag: String
        get() {
            var superResult = super.stableTag
            val target = targetFragment
            if (target != null) superResult += "-" + target.tag + "-" + targetRequestCode
            return superResult
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roles = roleViewModel.getModelList(Role::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.my_teams),
                fabIcon = R.drawable.ic_search_white_24dp,
                fabText = R.string.team_search_create,
                fabShows = showsFab
        )
        val refreshAction = {
            disposables.add(roleViewModel.refresh(Role::class.java)
                    .subscribe(this::onTeamsUpdated, defaultErrorHandler::invoke)).let { Unit }
        }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, emptyDrawable, emptyText))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout), refreshAction)
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(TeamAdapter(roles, this))
                .withStaggeredGridLayoutManager(2)
                .build()
    }

    override fun onResume() {
        super.onResume()
        fetchTeams()
    }

    override fun onTeamClicked(item: Team) {
        val target = targetFragment
        val canPick = target is TeamAdapter.AdapterListener

        if (canPick) (target as TeamAdapter.AdapterListener).onTeamClicked(item)
        else {
            teamViewModel.updateDefaultTeam(item)
            navigator.show(TeamMembersFragment.newInstance(item))
        }
    }

    override fun onClick(view: View) = when (view.id) {
        R.id.fab -> {
            bottomSheetDriver.hideBottomSheet()
            navigator.show(TeamSearchFragment.newInstance()).let { Unit }
        }
        else -> Unit
    }

    private fun fetchTeams() {
        scrollManager.setRefreshing()
        disposables.add(roleViewModel.getMore(Role::class.java).subscribe(this::onTeamsUpdated, defaultErrorHandler::invoke))
    }

    private fun onTeamsUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        updateUi(fabShows = showsFab)
    }

    companion object {

        private val EXCLUDED_VIEWS = intArrayOf(R.id.list_layout)

        fun newInstance(): TeamsFragment = TeamsFragment().apply { arguments = Bundle() }
    }
}
