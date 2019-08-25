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


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.SnackBarUtils.findSuitableParent

class LoadingBar
/**
 * Constructor for the transient bottom bar.
 *
 * @param parent   The parent for this transient bottom bar.
 * @param content  The content view for this transient bottom bar.
 * @param callback The content view callback for this transient bottom bar.
 */
private constructor(parent: ViewGroup, content: View, callback: com.google.android.material.snackbar.ContentViewCallback) : BaseTransientBottomBar<LoadingBar>(parent, content, callback) {

    init {
        // Remove the default insets applied that account for the keyboard showing up
        // since it's handled by us
        val wrapper = content.parent as View
        ViewCompat.setOnApplyWindowInsetsListener(wrapper) { _, insets -> insets }
    }

    companion object {

        fun make(view: View, @Duration duration: Int): LoadingBar {
            val parent = findSuitableParent(view)
            val inflater = LayoutInflater.from(parent!!.context)

            val content = inflater.inflate(R.layout.snackbar_loading, parent, false)
            val viewCallback = SnackBarUtils.SnackbarAnimationCallback(content)
            val loadingBar = LoadingBar(parent, content, viewCallback)

            loadingBar.duration = duration
            return loadingBar
        }
    }
}
