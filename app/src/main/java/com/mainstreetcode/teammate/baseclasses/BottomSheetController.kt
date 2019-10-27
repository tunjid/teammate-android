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


import android.location.Address
import android.os.Parcel
import android.os.Parcelable
import android.transition.Fade
import android.transition.Transition
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.fragments.main.AddressPickerFragment
import com.mainstreetcode.teammate.fragments.main.BlankBottomSheetFragment
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.navigation.AppNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.savedstate.LifecycleSavedStateContainer

interface BottomSheetController {
    val bottomSheetDriver: BottomSheetDriver
}

class BottomSheetDriver(
        private val host: FragmentActivity,
        private val stateContainer: LifecycleSavedStateContainer,
        private val bottomSheetContainer: ViewGroup,
        private val bottomSheetToolbar: Toolbar,
        private val transientBarDriver: TransientBarDriver
) : OnBackPressedCallback(true) {

    private var bottomToolbarState: ToolbarState? = null
    private val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheetContainer)

    private val bottomSheetTransition: Transition
        get() = Fade().setDuration(250)

    val isBottomSheetShowing: Boolean
        get() = bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN

    val current: Fragment?
        get() = if (!isEnabled) null
        else host.supportFragmentManager.findFragmentById(R.id.bottom_sheet_view).run {
            if (this is BlankBottomSheetFragment) null else this
        }

    init {
        bottomSheetToolbar.setOnMenuItemClickListener(host::onOptionsItemSelected)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                isEnabled = newState != BottomSheetBehavior.STATE_HIDDEN
                if (newState == BottomSheetBehavior.STATE_HIDDEN) restoreHiddenViewState()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })

        bottomToolbarState = stateContainer.savedState.getParcelable(BOTTOM_TOOLBAR_STATE)
        refreshBottomToolbar()

        if (current == null) hideBottomSheet()
        else adjustTopPadding()
    }

    override fun handleOnBackPressed() = hideBottomSheet()

    fun hideBottomSheet() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            restoreHiddenViewState()
        } else isEnabled = false
    }

    /**
     * Shows the supplied fragment in a bottom sheet on the Activity's level
     *
     * @param requestCode code passed to the [fragment] shown with [Fragment.setTargetFragment]
     * @param menuRes the menu inflated into the toolbar of this bottom sheet
     * @param title the title shown in the toolbar of this bottom sheet
     * @param target The target fragment for the [fragment] shown. It MUST be in the
     * [FragmentManager] of the [host] activity for this [BottomSheetDriver]!
     * @param fragment the fragment shown in the bottom sheet
     */
    fun showBottomSheet(
            requestCode: Int,
            menuRes: Int = R.menu.empty,
            title: String = "",
            target: Fragment? = null,
            fragment: TeammatesBaseFragment? = null) {

        fragment ?: return
        fragment.exitTransition = bottomSheetTransition
        fragment.enterTransition = bottomSheetTransition
        fragment.setTargetFragment(target ?: RelayFragment.getInstance(host), requestCode)

        isEnabled = true
        transientBarDriver.clearTransientBars()

        adjustTopPadding()

        host.supportFragmentManager.beginTransaction()
                .replace(R.id.bottom_sheet_view, fragment, fragment.stableTag)
                .runOnCommit {
                    bottomToolbarState = ToolbarState(menuRes, title)
                    stateContainer.savedState.putParcelable(BOTTOM_TOOLBAR_STATE, bottomToolbarState)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    refreshBottomToolbar()
                }.commit()
    }

    private fun refreshBottomToolbar() = bottomToolbarState?.run {
        bottomSheetToolbar.menu.clear()
        bottomSheetToolbar.inflateMenu(menuRes)
        bottomSheetToolbar.title = title
    }

    private fun adjustTopPadding() {
        val topPadding = WindowInsetsDriver.topInset + host.resources.getDimensionPixelSize(R.dimen.single_margin)
        bottomSheetContainer.setPadding(0, topPadding, 0, 0)
    }

    private fun restoreHiddenViewState() {
        val navigator = (host as? Navigator.Controller)?.navigator ?: return
        val onCommit = {
            val post = navigator.current as? TeammatesBaseFragment
            if (post != null && post.view != null) post.togglePersistentUi()
        }

        BlankBottomSheetFragment.newInstance().apply {
            this@BottomSheetDriver.host.supportFragmentManager.beginTransaction()
                    .replace(R.id.bottom_sheet_view, this, stableTag)
                    .runOnCommit(onCommit)
                    .commit()
        }
    }

    companion object {
        const val BOTTOM_TOOLBAR_STATE = "BOTTOM_TOOLBAR_STATE"
    }
}

class ToolbarState(
        val menuRes: Int,
        val title: String
) : Parcelable {

    private constructor(`in`: Parcel) : this(`in`.readInt(), `in`.readString()!!)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(menuRes)
        dest.writeString(title)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<ToolbarState> = object : Parcelable.Creator<ToolbarState> {
            override fun createFromParcel(`in`: Parcel): ToolbarState = ToolbarState(`in`)

            override fun newArray(size: Int): Array<ToolbarState?> = arrayOfNulls(size)
        }
    }
}

/**
 * Relays callbacks from the fragment in the bottom sheet to the [Fragment] visible behind it
 */
class RelayFragment : Fragment(),
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener,
        AddressPickerFragment.AddressPicker {

    val navigator by activityNavigatorController<AppNavigator>()

    override fun onUserClicked(item: User) {
        caller<UserAdapter.AdapterListener>()?.run { this.onUserClicked(item) }
    }

    override fun onTeamClicked(item: Team) {
        caller<TeamAdapter.AdapterListener>()?.run { this.onTeamClicked(item) }
    }

    override fun onAddressPicked(address: Address) {
        caller<AddressPickerFragment.AddressPicker>()?.run { this.onAddressPicked(address) }
    }

    private inline fun <reified T : Any> caller(): T? = navigator.activeNavigator.current as? T

    companion object {

        private const val TAG = "relay"

        fun getInstance(host: FragmentActivity): RelayFragment = host.run {
            supportFragmentManager.findFragmentByTag(TAG) as? RelayFragment
                    ?: RelayFragment().apply { supportFragmentManager.commitNow { add(this@apply, TAG) } }
        }
    }
}