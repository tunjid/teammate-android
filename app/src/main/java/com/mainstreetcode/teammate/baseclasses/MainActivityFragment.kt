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
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.activities.MainActivity
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
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import kotlin.math.abs

/**
 * Class for Fragments in [com.mainstreetcode.teammate.activities.MainActivity]
 */

open class MainActivityFragment : TeammatesBaseFragment() {

    protected lateinit var feedViewModel: FeedViewModel
    protected lateinit var roleViewModel: RoleViewModel
    protected lateinit var userViewModel: UserViewModel
    protected lateinit var teamViewModel: TeamViewModel
    protected lateinit var chatViewModel: ChatViewModel
    protected lateinit var gameViewModel: GameViewModel
    protected lateinit var statViewModel: StatViewModel
    protected lateinit var prefsViewModel: PrefsViewModel
    protected lateinit var eventViewModel: EventViewModel
    protected lateinit var mediaViewModel: MediaViewModel
    protected lateinit var locationViewModel: LocationViewModel
    protected lateinit var localRoleViewModel: LocalRoleViewModel
    protected lateinit var teamMemberViewModel: TeamMemberViewModel
    protected lateinit var competitorViewModel: CompetitorViewModel
    protected lateinit var tournamentViewModel: TournamentViewModel
    protected lateinit var blockedUserViewModel: BlockedUserViewModel

    private var spacer: View? = null
    protected lateinit var scrollManager: ScrollManager<out InteractiveViewHolder<*>>

    protected val isBottomSheetShowing: Boolean
        get() {
            val controller = persistentUiController
            return if (controller is BottomSheetController) (controller as BottomSheetController).isBottomSheetShowing else false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        localRoleViewModel = ViewModelProviders.of(this).get(LocalRoleViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val provider = ViewModelProviders.of(requireActivity())
        feedViewModel = provider.get(FeedViewModel::class.java)
        roleViewModel = provider.get(RoleViewModel::class.java)
        userViewModel = provider.get(UserViewModel::class.java)
        teamViewModel = provider.get(TeamViewModel::class.java)
        chatViewModel = provider.get(ChatViewModel::class.java)
        gameViewModel = provider.get(GameViewModel::class.java)
        statViewModel = provider.get(StatViewModel::class.java)
        prefsViewModel = provider.get(PrefsViewModel::class.java)
        eventViewModel = provider.get(EventViewModel::class.java)
        mediaViewModel = provider.get(MediaViewModel::class.java)
        locationViewModel = provider.get(LocationViewModel::class.java)
        teamMemberViewModel = provider.get(TeamMemberViewModel::class.java)
        competitorViewModel = provider.get(CompetitorViewModel::class.java)
        tournamentViewModel = provider.get(TournamentViewModel::class.java)
        blockedUserViewModel = provider.get(BlockedUserViewModel::class.java)

        defaultErrorHandler.addAction { if (::scrollManager.isInitialized) scrollManager.reset() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spacer = view.findViewById(R.id.spacer_toolbar)
        if (spacer == null || (view.parent as View).id != R.id.bottom_sheet_view) return

        spacer?.setBackgroundResource(R.drawable.bg_round_top_toolbar)
        spacer?.clipToOutline = true
    }

    override fun onResume() {
        super.onResume()
        if (!restoredFromBackStack() && ::scrollManager.isInitialized) setFabExtended(true)
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

    protected fun inputRecycledViewPool(): RecyclerView.RecycledViewPool? =
            (requireActivity() as MainActivity).inputRecycledPool

    protected fun hideBottomSheet() {
        val controller = persistentUiController
        if (controller is BottomSheetController)
            (controller as BottomSheetController).hideBottomSheet()
    }

    protected fun showBottomSheet(args: BottomSheetController.Args) {
        val controller = persistentUiController
        if (controller is BottomSheetController)
            (controller as BottomSheetController).showBottomSheet(args)
    }

    protected fun onInconsistencyDetected(exception: IndexOutOfBoundsException) {
        Logger.log(stableTag, "Inconsistent Recyclerview", exception)
        val activity = activity
        activity?.onBackPressed()
    }

    protected fun updateFabForScrollState(dy: Int) {
        if (abs(dy) < 9) return
        setFabExtended(dy < 0)
    }

    protected fun updateTopSpacerElevation() {
        if (spacer == null || !::scrollManager.isInitialized) return
        spacer?.isSelected = scrollManager.recyclerView.canScrollVertically(-1)
    }

    protected fun signOut() {
        teamViewModel.updateDefaultTeam(Team.empty())
        disposables.add(userViewModel.signOut().subscribe(
                { MainActivity.startRegistrationActivity(requireActivity()) },
                { MainActivity.startRegistrationActivity(requireActivity()) }
        ))
    }

    protected fun showCompetitor(competitor: Competitor) = when (val entity = competitor.entity) {
        is Team -> JoinRequestFragment.joinInstance(entity, userViewModel.currentUser)
        is User -> UserEditFragment.newInstance(entity)
        else -> null
    }?.let { showFragment(it) }.run { Unit }

    protected fun pickPlace() {
        val picker = AddressPickerFragment.newInstance()
        picker.setTargetFragment(this, R.id.request_place_pick)

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(picker)
                .setTitle("")
                .build())
    }

    protected fun watchForRoleChanges(team: Team, onChanged: () -> Unit) {
        if (team.isEmpty) return
        val user = userViewModel.currentUser
        disposables.add(localRoleViewModel.watchRoleChanges(user, team).subscribe({ onChanged.invoke() }, emptyErrorHandler::invoke))
    }

    protected fun BaseFragment.listDetailTransition(
            key: String,
            itemViewId: Int = R.id.fragment_header_background,
            thumbnailId: Int = R.id.fragment_header_thumbnail

    ): FragmentTransaction? {
        val fallBack = super.provideFragmentTransaction(this)

        val args = arguments ?: return fallBack

        val model = args.getParcelable<Parcelable>(key) ?: return fallBack

        val holder = scrollManager.findViewHolderForItemId(model.hashCode().toLong()) as? ModelCardViewHolder<*, *>
                ?: return fallBack

        return beginTransaction()
                .addSharedElement(holder.itemView, model.getTransitionName(itemViewId))
                .addSharedElement(holder.thumbnail, model.getTransitionName(thumbnailId))
    }

}
