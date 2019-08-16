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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamMemberAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.adapters.viewholders.ModelCardViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.getTransitionName
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Displays a [team&#39;s][Team] [members][User].
 */

class TeamMembersFragment : MainActivityFragment(), TeamMemberAdapter.UserAdapterListener {

    private lateinit var team: Team
    private lateinit var teamModels: List<Differentiable>

    override val fabStringResource: Int
        @StringRes
        get() = R.string.invite_user

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_group_add_white_24dp

    override val toolbarMenu: Int
        get() = R.menu.fragment_team_detail

    override val toolbarTitle: CharSequence
        get() = if (targetFragment != null) "" else getString(R.string.team_name_prefix, team.name)

    override fun getStableTag(): String {
        val superResult = super.getStableTag()
        val tempTeam = arguments!!.getParcelable<Team>(ARG_TEAM)

        return if (tempTeam == null) superResult else superResult + "-" + tempTeam.hashCode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        team = arguments!!.getParcelable(ARG_TEAM)!!
        teamModels = teamMemberViewModel.getModelList(team)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false)

        val refreshAction = Runnable { disposables.add(teamMemberViewModel.refresh(team).subscribe(this::onTeamUpdated, defaultErrorHandler::accept)) }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(rootView.findViewById(R.id.list_layout))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withAdapter(TeamMemberAdapter(teamModels, this))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withStaggeredGridLayoutManager(2)
                .build()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        fetchTeamMembers(true)
        watchForRoleChanges(team) { this.togglePersistentUi() }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val visible = showsFab()

        val editItem = menu.findItem(R.id.action_edit)
        val deleteItem = menu.findItem(R.id.action_delete)
        val blockedItem = menu.findItem(R.id.action_blocked)
        val tournamentItem = menu.findItem(R.id.action_team_tournaments)

        editItem?.isVisible = visible
        deleteItem?.isVisible = visible
        blockedItem?.isVisible = visible
        tournamentItem?.isVisible = team.sport.supportsCompetitions()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
         when (item.itemId) {
            R.id.action_edit -> showFragment(TeamEditFragment.newEditInstance(team))

            R.id.action_team_tournaments -> showFragment(TournamentsFragment.newInstance(team))

            R.id.action_blocked -> showFragment(BlockedUsersFragment.newInstance(team))

            R.id.action_delete -> {
                AlertDialog.Builder(requireContext()).setTitle(getString(R.string.delete_team_prompt, team.name))
                        .setMessage(R.string.delete_team_prompt_body)
                        .setPositiveButton(R.string.yes) { _, _ -> deleteTeam() }
                        .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                        .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun showsFab(): Boolean =
            targetRequestCode == 0 && localRoleViewModel.hasPrivilegedRole()

    override fun onRoleClicked(role: Role) {
        val target = targetFragment
        val canPick = target is UserAdapter.AdapterListener

        if (canPick) (target as UserAdapter.AdapterListener).onUserClicked(role.user)
        else showFragment(RoleEditFragment.newInstance(role))
    }

    override fun onJoinRequestClicked(request: JoinRequest) {
        val target = targetFragment
        val canPick = target is UserAdapter.AdapterListener

        if (canPick) showSnackbar(getString(R.string.stat_user_not_on_team))
        else showFragment(JoinRequestFragment.viewInstance(request))
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> if (localRoleViewModel.hasPrivilegedRole())
                showFragment(JoinRequestFragment.inviteInstance(team))
        }
    }

    @SuppressLint("CommitTransaction")
    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? {
        if (fragmentTo.stableTag.contains(RoleEditFragment::class.java.simpleName)) {
            val role = fragmentTo.arguments!!.getParcelable<Role>(RoleEditFragment.ARG_ROLE)
                    ?: return null

            val holder = scrollManager.findViewHolderForItemId(role.hashCode().toLong()) as? ModelCardViewHolder<*, *>
                    ?: return null

            return beginTransaction()
                    .addSharedElement(holder.itemView, role.getTransitionName(R.id.fragment_header_background))
                    .addSharedElement(holder.thumbnail, role.getTransitionName(R.id.fragment_header_thumbnail))
        }
        if (fragmentTo.stableTag.contains(JoinRequestFragment::class.java.simpleName)) {
            val request = fragmentTo.arguments!!.getParcelable<JoinRequest>(JoinRequestFragment.ARG_JOIN_REQUEST)
                    ?: return null

            val holder = scrollManager.findViewHolderForItemId(request.hashCode().toLong()) as? ModelCardViewHolder<*, *>
                    ?: return null

            return beginTransaction()
                    .addSharedElement(holder.itemView, request.getTransitionName(R.id.fragment_header_background))
                    .addSharedElement(holder.thumbnail, request.getTransitionName(R.id.fragment_header_thumbnail))
        }
        return super.provideFragmentTransaction(fragmentTo)
    }

    private fun fetchTeamMembers(fetchLatest: Boolean) {
        if (fetchLatest)
            scrollManager.setRefreshing()
        else
            toggleProgress(true)

        disposables.add(teamMemberViewModel.getMany(team, fetchLatest).subscribe(this::onTeamUpdated, defaultErrorHandler::accept))
    }

    private fun deleteTeam() {
        disposables.add(teamViewModel.deleteTeam(team).subscribe({ onTeamDeleted() }, defaultErrorHandler::accept))
    }

    private fun onTeamUpdated(diffResult: DiffUtil.DiffResult) {
        scrollManager.onDiff(diffResult)
        requireActivity().invalidateOptionsMenu()
    }

    private fun onTeamDeleted() {
        showSnackbar(getString(R.string.deleted_team, team.name))
        removeEnterExitTransitions()

        val activity = activity
        activity?.onBackPressed()
    }

    companion object {

        private const val ARG_TEAM = "team"

        fun newInstance(team: Team): TeamMembersFragment = TeamMembersFragment().apply { arguments = bundleOf(ARG_TEAM to team) }
    }
}
