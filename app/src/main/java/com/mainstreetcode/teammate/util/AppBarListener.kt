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

package com.mainstreetcode.teammate.util

import android.view.View
import androidx.core.view.doOnLayout
import com.google.android.material.appbar.AppBarLayout

class AppBarListener constructor(
        private val appBarLayout: AppBarLayout,
        private val offsetDiffListener: (OffsetProps) -> Unit
) : View.OnAttachStateChangeListener, AppBarLayout.OnOffsetChangedListener {

    private var lastOffset: Int = 0
    private var appBarHeight: Int = 0

    init {
        appBarLayout.addOnOffsetChangedListener(this)
        appBarLayout.addOnAttachStateChangeListener(this)
        appBarLayout.doOnLayout { this.appBarHeight = appBarLayout.height }
    }

    override fun onViewAttachedToWindow(v: View) {}

    override fun onViewDetachedFromWindow(v: View) {
        appBarLayout.removeOnOffsetChangedListener(this)
        appBarLayout.removeOnAttachStateChangeListener(this)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, newOffset: Int) {
        offsetDiffListener.invoke(OffsetProps(lastOffset - newOffset, newOffset, appBarHeight))
        lastOffset = newOffset
    }

    class OffsetProps internal constructor(val dy: Int, offsetInverse: Int, private val appBarHeight: Int) {

        val offset = -offsetInverse

        val fraction: Float
            get() = offset.toFloat() / appBarHeight

        fun appBarUnmeasured(): Boolean = appBarHeight == 0
    }
}
