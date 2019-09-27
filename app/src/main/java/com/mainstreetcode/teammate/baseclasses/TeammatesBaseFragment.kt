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
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.TransitionSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
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
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.UiState
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.FULL_RES_LOAD_DELAY
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.util.getTransitionName
import com.mainstreetcode.teammate.util.resolveThemeColor
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
import com.tunjid.androidbootstrap.core.components.Navigator
import com.tunjid.androidbootstrap.core.components.activityNavigationController
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.view.util.InsetFlags
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

/**
 * Class for Fragments in [com.mainstreetcode.teammate.activities.MainActivity]
 */

open class TeammatesBaseFragment(layoutRes: Int = 0) : Fragment(layoutRes),
        InsetProvider,
        GlobalUiController,
        View.OnClickListener,
        BottomSheetController,
        TransientBarController,
        Navigator.TagProvider,
        Navigator.TransactionModifier,
        Navigator.NavigationController {

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

    private var activityUiState by activityGlobalUiController()

    private val lastSetUiState = AtomicReference<UiState>()

    open val showsFab = false

    protected var restoredFromBackStack: Boolean = false

    protected var disposables = CompositeDisposable()

    protected var emptyErrorHandler: (Throwable) -> Unit = ErrorHandler.EMPTY

    protected lateinit var defaultErrorHandler: ErrorHandler

    protected lateinit var scrollManager: ScrollManager<out InteractiveViewHolder<*>>

    override val insetFlags = InsetFlags.ALL

    override val navigator: Navigator by activityNavigationController()

    override val bottomSheetDriver: BottomSheetDriver
        get() = requireActivity().run { (this as BottomSheetController).bottomSheetDriver }

    override val transientBarDriver: TransientBarDriver
        get() = requireActivity().run { (this as TransientBarController).transientBarDriver }

    override var uiState: UiState
        get() = activityUiState
        set(value) {
            activityUiState = value
            lastSetUiState.set(value)
        }

    override val stableTag: String = javaClass.simpleName

    override fun onAttach(context: Context) {
        super.onAttach(context)

        defaultErrorHandler = ErrorHandler.builder()
                .defaultMessage(getString(R.string.error_default))
                .add(this::handleErrorMessage)
                .build()

        userViewModel
        roleViewModel.apply { }
        teamViewModel
        defaultErrorHandler.addAction { if (::scrollManager.isInitialized) scrollManager.reset() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUi(fabClickListener = this)

        spacer = view.findViewById(R.id.spacer_toolbar)
        if (spacer == null || (view.parent as? View)?.id != R.id.bottom_sheet_view) return

        spacer?.setBackgroundResource(R.drawable.bg_round_top_toolbar)
        spacer?.clipToOutline = true
    }

    override fun onResume() {
        super.onResume()
        if (view != null) updateUi(fabShows = showsFab)
        if (!restoredFromBackStack && ::scrollManager.isInitialized) uiState = uiState.copy(fabExtended = true)
    }

    override fun onPause() {
        disposables.clear()
        super.onPause()
    }

    override fun onDestroyView() {
        disposables.clear()
        restoredFromBackStack = true
        if (uiState.fabClickListener === this) updateUi(fabClickListener = null)

        if (::scrollManager.isInitialized) scrollManager.clear()
        spacer = null

        super.onDestroyView()
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        )
    }

    override fun onKeyBoardChanged(appeared: Boolean) = Unit

    override fun onClick(view: View) = Unit

    open fun togglePersistentUi() {
        lastSetUiState.get()?.apply { uiState = this }
    }

    protected fun setEnterExitTransitions() {
        if (Config.isStaticVariant) return

        sharedElementEnterTransition = cardTransition()
        sharedElementReturnTransition = cardTransition()
    }

    protected fun removeEnterExitTransitions() {
        enterTransition = Fade()
        exitTransition = Fade()
        sharedElementEnterTransition = null
        sharedElementReturnTransition = null
    }

    protected fun updateFabOnScroll(dx: Int, dy: Int) =
            if (showsFab && abs(dy) > 3) uiState = uiState.copy(fabShows = dy < 0) else Unit

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

    protected fun hideKeyboard() {
        val root = view ?: return

        val imm = root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(root.windowToken, 0)
    }

    protected fun showCompetitor(competitor: Competitor) = when (val entity = competitor.entity) {
        is Team -> JoinRequestFragment.joinInstance(entity, userViewModel.currentUser)
        is User -> UserEditFragment.newInstance(entity)
        else -> null
    }?.let { navigator.show(it) }.run { Unit }

    protected fun pickPlace() = bottomSheetDriver.showBottomSheet {
        val picker = AddressPickerFragment.newInstance()
        picker.setTargetFragment(this@TeammatesBaseFragment, R.id.request_place_pick)

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

        val holder = this@TeammatesBaseFragment.scrollManager
                .findViewHolderForItemId(model.hashCode().toLong()) as? ModelCardViewHolder<*, *>
                ?: return

        this@TeammatesBaseFragment.exitTransition = sharedFadeTransition()

        addSharedElement(holder.itemView, model.getTransitionName(itemViewId))
        addSharedElement(holder.thumbnail, model.getTransitionName(thumbnailId))
    }

    protected fun defaultUi(
            @DrawableRes fabIcon: Int = uiState.fabIcon,
            @StringRes fabText: Int = uiState.fabText,
            fabShows: Boolean = false,
            fabExtended: Boolean = uiState.fabExtended,
            @MenuRes toolBarMenu: Int = 0,
            toolbarShows: Boolean = true,
            toolbarInvalidated: Boolean = false,
            toolbarTitle: CharSequence = "",
            @MenuRes altToolBarMenu: Int = 0,
            altToolBarShows: Boolean = false,
            altToolbarInvalidated: Boolean = false,
            altToolbarTitle: CharSequence = "",
            @ColorInt navBarColor: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) requireActivity().resolveThemeColor(R.attr.nav_bar_color) else Color.BLACK,
            bottomNavShows: Boolean = true,
            grassShows: Boolean = false,
            systemUiShows: Boolean = true,
            hasLightNavBar: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
            fabClickListener: View.OnClickListener? = this
    ) = updateUi(
            fabIcon,
            fabText,
            fabShows,
            fabExtended,
            toolBarMenu,
            toolbarShows,
            toolbarInvalidated,
            toolbarTitle,
            altToolBarMenu,
            altToolBarShows,
            altToolbarInvalidated,
            altToolbarTitle,
            navBarColor,
            bottomNavShows,
            grassShows,
            systemUiShows,
            hasLightNavBar,
            fabClickListener
    )

    protected fun updateUi(
            @DrawableRes fabIcon: Int = uiState.fabIcon,
            @StringRes fabText: Int = uiState.fabText,
            fabShows: Boolean = uiState.fabShows,
            fabExtended: Boolean = uiState.fabExtended,
            @MenuRes toolBarMenu: Int = uiState.toolBarMenu,
            toolbarShows: Boolean = uiState.toolbarShows,
            toolbarInvalidated: Boolean = uiState.toolbarInvalidated,
            toolbarTitle: CharSequence = uiState.toolbarTitle,
            @MenuRes altToolBarMenu: Int = uiState.altToolBarMenu,
            altToolBarShows: Boolean = uiState.altToolBarShows,
            altToolbarInvalidated: Boolean = uiState.altToolbarInvalidated,
            altToolbarTitle: CharSequence = uiState.altToolbarTitle,
            @ColorInt navBarColor: Int = uiState.navBarColor,
            bottomNavShows: Boolean = uiState.bottomNavShows,
            grassShows: Boolean = uiState.grassShows,
            systemUiShows: Boolean = uiState.systemUiShows,
            hasLightNavBar: Boolean = uiState.hasLightNavBar,
            fabClickListener: View.OnClickListener? = uiState.fabClickListener
    ) {
        if (navigator.currentFragment !== this) return

        uiState = uiState.copy(
                fabIcon = fabIcon,
                fabText = fabText,
                fabShows = fabShows,
                fabExtended = fabExtended,
                toolBarMenu = toolBarMenu,
                toolbarShows = toolbarShows,
                toolbarInvalidated = toolbarInvalidated,
                toolbarTitle = toolbarTitle,
                altToolBarMenu = altToolBarMenu,
                altToolBarShows = altToolBarShows,
                altToolbarInvalidated = altToolbarInvalidated,
                altToolbarTitle = altToolbarTitle,
                navBarColor = navBarColor,
                bottomNavShows = bottomNavShows,
                grassShows = grassShows,
                systemUiShows = systemUiShows,
                hasLightNavBar = hasLightNavBar,
                fabClickListener = fabClickListener
        )
    }

    private fun sharedFadeTransition() = Fade().apply { duration = FULL_RES_LOAD_DELAY.toLong() }

    private fun cardTransition(): TransitionSet = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeImageTransform())
            .setOrdering(TransitionSet.ORDERING_TOGETHER)
            .apply { startDelay = 25; duration = FULL_RES_LOAD_DELAY.toLong() }

    private fun handleErrorMessage(message: Message) {
        transientBarDriver.toggleProgress(false)

        if (message.isUnauthorizedUser) signOut()
        else transientBarDriver.showSnackBar(message.message)

        val isIllegalTeamMember = message.isIllegalTeamMember
        val shouldGoBack = isIllegalTeamMember || message.isInvalidObject

        if (isIllegalTeamMember) teamViewModel.updateDefaultTeam(Team.empty())

        val activity = activity ?: return

        if (shouldGoBack) activity.onBackPressed()
    }

    companion object {
        const val GRASS_COLOR = -0x6c44af

        val NO_TOP: InsetFlags = InsetFlags.NO_TOP
        val NONE: InsetFlags = InsetFlags.NONE
    }
}
