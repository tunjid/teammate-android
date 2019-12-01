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

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.mainstreetcode.teammate.R
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.innermostFocusedChild
import com.tunjid.androidx.view.util.marginLayoutParams

class WindowInsetsDriver(
        globalUiController: GlobalUiController,
        private val parentContainer: ViewGroup,
        private val fragmentContainer: FragmentContainerView,
        private val coordinatorLayout: CoordinatorLayout,
        private val toolbar: Toolbar,
        private val altToolbar: Toolbar,
        private val bottomNavView: View,
        private val stackNavigatorSource: () -> Navigator?
) : FragmentManager.FragmentLifecycleCallbacks(), GlobalUiController by globalUiController {

    private var leftInset: Int = 0
    private var rightInset: Int = 0
    private var insetsApplied: Boolean = false
    private var lastWindowInsets: WindowInsetsCompat? = null
    private var lastInsetDispatch: InsetDispatch? = InsetDispatch()

    private val bottomNavHeight get() = bottomNavView.height

    init {
        ViewCompat.setOnApplyWindowInsetsListener(parentContainer) { _, insets -> onInsetsDispatched(insets) }
        bottomNavView.doOnLayout { lastWindowInsets?.let(this::consumeFragmentInsets) }
        fragmentContainer.bottomInsetSpring {
            addEndListener { _, _, _, _ ->
                val input = fragmentContainer.innermostFocusedChild as? EditText
                        ?: return@addEndListener
                input.text = input.text // Scroll to text that has focus
            }
        }
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
            onFragmentViewCreated(v, f)

    private fun isNotInCurrentFragmentContainer(fragment: Fragment): Boolean =
            stackNavigatorSource()?.run { fragment.id != containerId } ?: true

    private fun onInsetsDispatched(insets: WindowInsetsCompat): WindowInsetsCompat {
        if (this.insetsApplied) return insets

        topInset = insets.systemWindowInsetTop
        leftInset = insets.systemWindowInsetLeft
        rightInset = insets.systemWindowInsetRight
        bottomInset = insets.systemWindowInsetBottom

        toolbar.marginLayoutParams.topMargin = topInset
        altToolbar.marginLayoutParams.topMargin = topInset
        bottomNavView.marginLayoutParams.bottomMargin = bottomInset

        adjustInsetForFragment(stackNavigatorSource()?.current)

        this.insetsApplied = true
        return insets
    }

    private fun onFragmentViewCreated(v: View, fragment: Fragment) {
        if (fragment !is InsetProvider || isNotInCurrentFragmentContainer(fragment)) return
        adjustInsetForFragment(fragment)

        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets -> consumeFragmentInsets(insets) }
    }

    private fun consumeFragmentInsets(insets: WindowInsetsCompat): WindowInsetsCompat = insets.apply {
        lastWindowInsets = this

        coordinatorLayout.ifBottomInsetChanged(coordinatorInsetReducer(systemWindowInsetBottom)) {
            bottomInsetSpring().animateToFinalPosition(it.toFloat())
        }
        fragmentContainer.ifBottomInsetChanged(contentInsetReducer(systemWindowInsetBottom)) {
            bottomInsetSpring().animateToFinalPosition(it.toFloat())
        }

        val current = stackNavigatorSource()?.current ?: return@apply
        if (isNotInCurrentFragmentContainer(current)) return@apply
        if (current !is InsetProvider) return@apply

        val large = systemWindowInsetBottom > bottomInset + bottomNavHeight given uiState.bottomNavShows
        val bottom = if (large) bottomInset else fragmentInsetReducer(current.insetFlags)

        current.view?.apply { ifBottomInsetChanged(bottom) { marginLayoutParams.bottomMargin = it } }
    }

    private fun adjustInsetForFragment(fragment: Fragment?) {
        if (fragment !is InsetProvider || isNotInCurrentFragmentContainer(fragment)) return

        fragment.insetFlags.dispatch(fragment.tag) {
            if (insetFlags == null || lastInsetDispatch == this) return

            parentContainer.updatePadding(
                    left = this.leftInset given insetFlags.hasLeftInset,
                    right = this.rightInset given insetFlags.hasRightInset
            )

            fragment.view?.marginLayoutParams?.apply {
                topMargin = topInset given insetFlags.hasTopInset
                bottomMargin = fragmentInsetReducer(insetFlags)
            }

            lastInsetDispatch = this
        }
    }

    private inline fun InsetFlags.dispatch(tag: String?, receiver: InsetDispatch.() -> Unit) =
            receiver.invoke(InsetDispatch(tag, leftInset, topInset, rightInset, bottomInset, bottomNavHeight, this))

    private fun contentInsetReducer(systemBottomInset: Int) =
            systemBottomInset - bottomInset

    private fun coordinatorInsetReducer(systemBottomInset: Int) =
            if (systemBottomInset > bottomInset) systemBottomInset
            else bottomInset + (bottomNavHeight given uiState.bottomNavShows)

    private fun fragmentInsetReducer(insetFlags: InsetFlags): Int {
        return bottomNavHeight.given(uiState.bottomNavShows) + bottomInset.given(insetFlags.hasBottomInset)
    }

    companion object {
        var topInset: Int = 0
        var bottomInset: Int = 0
    }

    private data class InsetDispatch(
            val tag: String? = null,
            val leftInset: Int = 0,
            val topInset: Int = 0,
            val rightInset: Int = 0,
            val bottomInset: Int = 0,
            val bottomNavHeight: Int = 0,
            val insetFlags: InsetFlags? = null
    )
}

private infix fun Int.given(flag: Boolean) = if (flag) this else 0

private fun View.ifBottomInsetChanged(newInset: Int, action: View.(Int) -> Unit) {
    if (marginLayoutParams.bottomMargin != newInset) action(newInset)
}

private fun View.bottomInsetSpring(modifier: SpringAnimation.() -> Unit = {}): SpringAnimation =
        getTag(R.id.bottom_inset_spring) as? SpringAnimation ?: springAnimationOf(
                { marginLayoutParams.bottomMargin = it.toInt(); invalidate(); requestLayout() },
                { marginLayoutParams.bottomMargin.toFloat() },
                0F
        ).apply {
            setTag(R.id.bottom_inset_spring, this@apply)
            spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            modifier(this)
        }