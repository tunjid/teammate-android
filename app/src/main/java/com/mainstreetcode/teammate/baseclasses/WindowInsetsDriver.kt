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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.mainstreetcode.teammate.util.setMaterialOverlay
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.marginLayoutParams
import kotlin.math.max

class WindowInsetsDriver(
        private val stackNavigatorSource: () -> Navigator?,
        private val parentContainer: ViewGroup,
        private val contentContainer: FragmentContainerView,
        private val coordinatorLayout: CoordinatorLayout,
        private val toolbar: Toolbar,
        private val topInsetView: View,
        private val bottomInsetView: View,
        private val keyboardPadding: View,
        private val insetAdjuster: (Int) -> Int
) : FragmentManager.FragmentLifecycleCallbacks() {

    private var leftInset: Int = 0
    private var rightInset: Int = 0
    private var insetsApplied: Boolean = false
    private var lastFragmentBottomInset: Int = Int.MIN_VALUE
    private var lastSystemInsetDispatch: InsetDispatch? = InsetDispatch()

    init {
        ViewCompat.setOnApplyWindowInsetsListener(parentContainer) { _, insets -> consumeSystemInsets(insets) }
    }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) = adjustInsetForFragment(f)

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
            onFragmentViewCreated(v, f)

    private fun isNotInCurrentFragmentContainer(fragment: Fragment): Boolean =
            stackNavigatorSource()?.run { fragment.id != containerId } ?: true

    private fun consumeSystemInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        if (this.insetsApplied) return insets

        topInset = insets.systemWindowInsetTop
        leftInset = insets.systemWindowInsetLeft
        rightInset = insets.systemWindowInsetRight
        bottomInset = insets.systemWindowInsetBottom

        topInsetView.layoutParams.height = topInset
        bottomInsetView.layoutParams.height = bottomInset

        bottomInsetView.setMaterialOverlay()
        keyboardPadding.setOnTouchListener { _, event ->
            if (event.action == KeyEvent.ACTION_UP) setKeyboardPadding(bottomInset)
            true
        }

        adjustInsetForFragment(stackNavigatorSource()?.currentFragment)

        this.insetsApplied = true
        return insets
    }

    private fun onFragmentViewCreated(v: View, fragment: Fragment) {
        if (fragment !is InsetProvider || isNotInCurrentFragmentContainer(fragment)) return
        adjustInsetForFragment(fragment)

        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets -> consumeFragmentInsets(insets) }
    }

    private fun consumeFragmentInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        val bottomSystemInset = insets.systemWindowInsetBottom

        if (lastFragmentBottomInset == bottomSystemInset) return insets

        setKeyboardPadding(bottomSystemInset)

        stackNavigatorSource()?.currentFragment?.run {
            if (this is InsetProvider) onKeyBoardChanged(bottomSystemInset > bottomInset)
        }

        lastFragmentBottomInset = bottomSystemInset

        return insets
    }

    private fun setKeyboardPadding(bottomSystemInset: Int) {
        val old = contentContainer.paddingBottom
        val new = max(insetAdjuster(bottomSystemInset - bottomInset), 0)

        if (old != new) TransitionManager.beginDelayedTransition(parentContainer, AutoTransition().apply {
            duration = ANIMATION_DURATION.toLong()
            coordinatorLayout.forEach { addTarget(it) }
            addTarget(coordinatorLayout) // Animate coordinator and its children, mainly the FAB
            excludeTarget(RecyclerView::class.java, true)
        })

        contentContainer.updatePadding(bottom = new)
        keyboardPadding.layoutParams.height = if (new != 0) new else 1 // 0 breaks animations
    }


    @SuppressLint("InlinedApi")
    private fun adjustInsetForFragment(fragment: Fragment?) {
        if (fragment !is InsetProvider || isNotInCurrentFragmentContainer(fragment)) return

        fragment.insetFlags.dispatch {
            if (insetFlags == null || lastSystemInsetDispatch == this) return

            toolbar.marginLayoutParams.topMargin = if (insetFlags.hasTopInset) 0 else topInset
            coordinatorLayout.marginLayoutParams.bottomMargin = if (insetFlags.hasBottomInset) 0 else bottomInset

            TransitionManager.beginDelayedTransition(parentContainer, AutoTransition().apply {
                duration = ANIMATION_DURATION.toLong()
                contentContainer.forEach { addTarget(it) }
                addTarget(contentContainer)
                excludeTarget(RecyclerView::class.java, true)
            })

            topInsetView.isVisible = insetFlags.hasTopInset
            bottomInsetView.isVisible = insetFlags.hasBottomInset

            parentContainer.setPadding(
                    if (insetFlags.hasLeftInset) this.leftInset else 0,
                    0,
                    if (insetFlags.hasRightInset) this.rightInset else 0,
                    0)

            lastSystemInsetDispatch = this
        }
    }

    private inline fun InsetFlags.dispatch(receiver: InsetDispatch.() -> Unit) =
            receiver.invoke(InsetDispatch(leftInset, topInset, rightInset, bottomInset, this))

    companion object {
        const val ANIMATION_DURATION = 300

        var topInset: Int = 0
        var bottomInset: Int = 0
    }

    private data class InsetDispatch(
            val leftInset: Int = 0,
            val topInset: Int = 0,
            val rightInset: Int = 0,
            val bottomInset: Int = 0,
            val insetFlags: InsetFlags? = null
    )
}