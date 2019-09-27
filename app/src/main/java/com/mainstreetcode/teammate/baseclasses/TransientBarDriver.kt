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
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar
import com.mainstreetcode.teammate.adapters.viewholders.LoadingBar
import com.mainstreetcode.teammate.util.resolveThemeColor
import java.util.*

interface TransientBarController {
    val transientBarDriver : TransientBarDriver
}

class TransientBarDriver(
        private val coordinatorLayout: CoordinatorLayout,
        private val anchorView: View
) : LifecycleEventObserver {

    private var loadingBar: LoadingBar? = null
    private val transientBottomBars = ArrayList<BaseTransientBottomBar<*>>()

    private val callback = object : BaseTransientBottomBar.BaseCallback<BaseTransientBottomBar<*>>() {
        override fun onDismissed(bar: BaseTransientBottomBar<*>?, event: Int) {
            transientBottomBars.remove(bar)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = when (event) {
        Lifecycle.Event.ON_PAUSE -> clearTransientBars()
        Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
        else -> Unit
    }

    fun showSnackBar(message: CharSequence) {
        val snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.view) { _, insets -> insets }
        snackbar.fabDependentShow()
    }

    fun showSnackBar(consumer: (Snackbar) -> Unit) {
        val snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_INDEFINITE).withCallback(callback)

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.view) { _, insets -> insets }
        consumer.invoke(snackbar)
        snackbar.fabDependentShow()
    }

    fun showChoices(consumer: (ChoiceBar) -> Unit) {
        val bar = ChoiceBar.make(coordinatorLayout, Snackbar.LENGTH_INDEFINITE).withCallback(callback)
        consumer.invoke(bar)
        bar.fabDependentShow()
    }

    @SuppressLint("Range", "WrongConstant")
    fun toggleProgress(show: Boolean) {
        val bar = loadingBar
        if (show && bar != null && bar.isShown) return
        if (show) LoadingBar.make(coordinatorLayout, Snackbar.LENGTH_INDEFINITE).apply { loadingBar = this }.show()
        else if (bar != null && bar.isShownOrQueued) bar.dismiss()
    }

    fun clearTransientBars() {
        for (bar in transientBottomBars)
            if (bar is ChoiceBar) bar.dismissAsTimeout()
            else bar.dismiss()
        transientBottomBars.clear()
    }

    private fun BaseTransientBottomBar<*>.fabDependentShow() = anchorView.postDelayed(HIDER_DURATION.toLong()) {
        transientBottomBars.add(this)
        (getView() as? ViewGroup)?.apply {
            backgroundTintList = ColorStateList.valueOf(context.resolveThemeColor(R.attr.colorOnSurface))
            recursiveTextStyle()
        }
        if (anchorView.isVisible) setAnchorView(anchorView)
        show()
    }

    private fun ViewGroup.recursiveTextStyle() {
        val buttonColor = context.resolveThemeColor(R.attr.colorSecondaryVariant)
        val textColor = context.resolveThemeColor(R.attr.colorSurface)

        forEach {
            @Suppress("CascadeIf")
            if (it is ViewGroup) it.recursiveTextStyle()
            else if (it is Button) it.setTextColor(buttonColor)
            else if (it is TextView) it.setTextColor(textColor)
        }
    }
}

const val HIDER_DURATION = 300

@Suppress("UNCHECKED_CAST")
fun <T : BaseTransientBottomBar<T>> BaseTransientBottomBar<T>.withCallback(callback: BaseTransientBottomBar.BaseCallback<BaseTransientBottomBar<*>>): T =
        addCallback(callback as BaseTransientBottomBar.BaseCallback<T>)
