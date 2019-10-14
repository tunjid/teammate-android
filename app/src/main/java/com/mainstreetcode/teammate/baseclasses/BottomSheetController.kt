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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.fragments.main.BlankBottomSheetFragment
import com.tunjid.androidx.navigation.Navigator
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

    val currentFragment: Fragment?
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
    }

    override fun handleOnBackPressed() = hideBottomSheet()

    fun hideBottomSheet() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            restoreHiddenViewState()
        } else isEnabled = false
    }

    fun showBottomSheet(block: Args.() -> Unit) {
        val args = Args().apply(block)

        val toShow = args.fragment ?: return
        toShow.enterTransition = bottomSheetTransition
        toShow.exitTransition = bottomSheetTransition

        isEnabled = true
        transientBarDriver.clearTransientBars()

        val topPadding = WindowInsetsDriver.topInset + host.resources.getDimensionPixelSize(R.dimen.single_margin)
        bottomSheetContainer.setPadding(0, topPadding, 0, 0)

        host.supportFragmentManager.beginTransaction()
                .replace(R.id.bottom_sheet_view, toShow, toShow.stableTag)
                .runOnCommit {
                    bottomToolbarState = args.toolbarState
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

    private fun restoreHiddenViewState() {
        val navigator = (host as? Navigator.Controller)?.navigator ?: return
        val onCommit = {
            val post = navigator.currentFragment as? TeammatesBaseFragment
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

class Args {
    var menuRes: Int = 0
    var title: String = ""
    var fragment: TeammatesBaseFragment? = null

    val toolbarState: ToolbarState
        get() = ToolbarState(menuRes, title)
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