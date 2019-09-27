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

package com.mainstreetcode.teammate.baseclasses

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.activities.MainActivity
import com.mainstreetcode.teammate.activities.signOut
import com.mainstreetcode.teammate.adapters.viewholders.ModelCardViewHolder
import com.mainstreetcode.teammate.fragments.main.AddressPickerFragment
import com.mainstreetcode.teammate.fragments.main.JoinRequestFragment
import com.mainstreetcode.teammate.fragments.main.UserEditFragment
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.getTransitionName
import com.mainstreetcode.teammate.viewmodel.BlockedUserViewModel
import com.mainstreetcode.teammate.viewmodel.ChatViewModel
import com.mainstreetcode.teammate.viewmodel.CompetitorViewModel
import com.mainstreetcode.teammate.viewmodel.EventViewModel
import com.mainstreetcode.teammate.viewmodel.FeedViewModel
import com.mainstreetcode.teammate.viewmodel.GameViewModel
import com.mainstreetcode.teammate.viewmodel.LocalRoleViewModel
import com.mainstreetcode.teammate.viewmodel.LocationViewModel
import com.mainstreetcode.teammate.viewmodel.MediaViewModel
import com.mainstreetcode.teammate.viewmodel.PrefsViewModel
import com.mainstreetcode.teammate.viewmodel.RoleViewModel
import com.mainstreetcode.teammate.viewmodel.StatViewModel
import com.mainstreetcode.teammate.viewmodel.TeamMemberViewModel
import com.mainstreetcode.teammate.viewmodel.TeamViewModel
import com.mainstreetcode.teammate.viewmodel.TournamentViewModel
import com.mainstreetcode.teammate.viewmodel.UserViewModel
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import kotlin.math.abs

/**
 * Class for Fragments in [com.mainstreetcode.teammate.activities.MainActivity]
 */

open class MainActivityFragment(layoutRes: Int = 0) : TeammatesBaseFragment(layoutRes), BottomSheetController {

    protected val feedViewModel by activityViewModels<FeedViewModel>()
    protected val roleViewModel by activityViewModels<RoleViewModel>()
    protected val userViewModel by activityViewModels<UserViewModel>()
    protected val teamViewModel by activityViewModels<TeamViewModel>()
    protected val chatViewModel by activityViewModels<ChatViewModel>()
    protected val gameViewModel by activityViewModels<GameViewModel>()
    protected val statViewModel by activityViewModels<StatViewModel>()
    protected val prefsViewModel by activityViewModels<PrefsViewModel>()
    protected val eventViewModel by activityViewModels<EventViewModel>()
    protected val mediaViewModel by activityViewModels<MediaViewModel>()
    protected val locationViewModel by activityViewModels<LocationViewModel>()
    protected val teamMemberViewModel by activityViewModels<TeamMemberViewModel>()
    protected val competitorViewModel by activityViewModels<CompetitorViewModel>()
    protected val tournamentViewModel by activityViewModels<TournamentViewModel>()
    protected val blockedUserViewModel by activityViewModels<BlockedUserViewModel>()

    protected val localRoleViewModel by viewModels<LocalRoleViewModel>()

    private var spacer: View? = null
    protected lateinit var scrollManager: ScrollManager<out InteractiveViewHolder<*>>

    override val bottomSheetDriver: BottomSheetDriver
        get() = requireActivity().run { (this as BottomSheetController).bottomSheetDriver }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userViewModel
        roleViewModel.apply {  }
        teamViewModel
        defaultErrorHandler.addAction { if (::scrollManager.isInitialized) scrollManager.reset() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spacer = view.findViewById(R.id.spacer_toolbar)
        if (spacer == null || (view.parent as? View)?.id != R.id.bottom_sheet_view) return

        spacer?.setBackgroundResource(R.drawable.bg_round_top_toolbar)
        spacer?.clipToOutline = true
    }

    override fun onResume() {
        super.onResume()
        if (!restoredFromBackStack && ::scrollManager.isInitialized) uiState = uiState.copy(fabExtended = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (::scrollManager.isInitialized) scrollManager.clear()
        spacer = null
    }

    override fun handleErrorMessage(message: Message) {
        if (message.isUnauthorizedUser) signOut()
        else super.handleErrorMessage(message)

        val isIllegalTeamMember = message.isIllegalTeamMember
        val shouldGoBack = isIllegalTeamMember || message.isInvalidObject

        if (isIllegalTeamMember) teamViewModel.updateDefaultTeam(Team.empty())

        val activity = activity ?: return

        if (shouldGoBack) activity.onBackPressed()
    }

    protected fun inputRecycledViewPool(): RecyclerView.RecycledViewPool =
            (requireActivity() as MainActivity).inputRecycledPool

    protected fun onInconsistencyDetected(exception: IndexOutOfBoundsException) {
        Logger.log(stableTag, "Inconsistent Recyclerview", exception)
        val activity = activity
        activity?.onBackPressed()
    }

    protected fun updateFabForScrollState(dy: Int) {
        if (abs(dy) < 9) return
        uiState = uiState.copy(fabExtended = dy < 0)
    }

    protected fun updateTopSpacerElevation() {
        if (spacer == null || !::scrollManager.isInitialized) return
        spacer?.isSelected = scrollManager.recyclerView?.canScrollVertically(-1) ?: false
    }

    protected fun signOut() {
        teamViewModel.updateDefaultTeam(Team.empty())
        disposables.add(userViewModel.signOut().subscribe(
                { navigator.signOut() },
                { navigator.signOut() }
        ))
    }

    protected fun showCompetitor(competitor: Competitor) = when (val entity = competitor.entity) {
        is Team -> JoinRequestFragment.joinInstance(entity, userViewModel.currentUser)
        is User -> UserEditFragment.newInstance(entity)
        else -> null
    }?.let { navigator.show(it) }.run { Unit }

    protected fun pickPlace() = bottomSheetDriver.showBottomSheet {
        val picker = AddressPickerFragment.newInstance()
        picker.setTargetFragment(this@MainActivityFragment, R.id.request_place_pick)

        menuRes = R.menu.empty
        fragment = picker
    }

    protected fun watchForRoleChanges(team: Team, onChanged: () -> Unit) {
        if (team.isEmpty) return
        val user = userViewModel.currentUser
        disposables.add(localRoleViewModel.watchRoleChanges(user, team).subscribe({ onChanged.invoke() }, emptyErrorHandler::invoke))
    }

    protected fun FragmentTransaction.listDetailTransition(
            key: String,
            incomingFragment: Fragment,
            itemViewId: Int = R.id.fragment_header_background,
            thumbnailId: Int = R.id.fragment_header_thumbnail

    ) {
        val args = incomingFragment.arguments ?: return

        val model = args.getParcelable<Parcelable>(key) ?: return

        val holder = this@MainActivityFragment.scrollManager
                .findViewHolderForItemId(model.hashCode().toLong()) as? ModelCardViewHolder<*, *>
                ?: return

        this@MainActivityFragment.exitTransition = sharedFadeTransition()

        addSharedElement(holder.itemView, model.getTransitionName(itemViewId))
        addSharedElement(holder.thumbnail, model.getTransitionName(thumbnailId))
    }

}
