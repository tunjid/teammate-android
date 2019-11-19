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

package com.mainstreetcode.teammate.adapters.viewholders


import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.ContentViewCallback

internal object SnackBarUtils {

    fun findSuitableParent(view: View?): ViewGroup? {
        var ref = view
        var fallback: ViewGroup? = null
        do {
            if (ref is CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return ref
            } else if (ref is FrameLayout) {
                // If we've hit the decor content view, then we didn't find a CoL in the
                // hierarchy, so use it.
                if (ref.id == android.R.id.content) return ref
                else fallback = ref// It's not the content view but we'll use it as our fallback
            }

            if (ref != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                val parent = ref.parent
                ref = if (parent is View) parent else null
            }
        } while (ref != null)

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback
    }

    internal class SnackbarAnimationCallback(private val content: View) : ContentViewCallback {

        override fun animateContentIn(delay: Int, duration: Int) {
            content.alpha = 0f
            ViewCompat.animate(content).alpha(1f).setDuration(duration.toLong())
                    .setStartDelay(delay.toLong()).start()
        }

        override fun animateContentOut(delay: Int, duration: Int) {
            content.alpha = 1f
            ViewCompat.animate(content).alpha(0f).setDuration(duration.toLong())
                    .setStartDelay(delay.toLong()).start()
        }
    }
}
