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

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.widget.TextViewCompat
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.mainstreetcode.teammate.R

class ExpandingToolbar private constructor(private val container: ViewGroup, private val onCollapsed: () -> Unit) {

    private val optionsList: View = container.findViewById(R.id.search_options)
    private val searchButton: TextView = container.findViewById(R.id.search)
    private val searchTitle: TextView = container.findViewById(R.id.search_title)

    init {
        val searchClickListener = { _: View -> toggleVisibility() }
        container.setOnClickListener(searchClickListener)
        searchButton.setOnClickListener(searchClickListener)
    }

    fun setTitle(@StringRes titleRes: Int) {
        searchTitle.text = searchTitle.context.getText(titleRes)
    }

    @SuppressLint("ResourceAsColor")
    fun setTitleIcon(isDown: Boolean) {
        val resVal = if (isDown) R.drawable.anim_vect_down_to_right_arrow else R.drawable.anim_vect_right_to_down_arrow

        val icon = AnimatedVectorDrawableCompat.create(searchTitle.context, resVal) ?: return

        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(searchTitle, null, null, icon, null)
    }

    fun changeVisibility(invisible: Boolean) {
        TransitionManager.beginDelayedTransition(container, AutoTransition()
                .addListener(object : Transition.TransitionListener {
                    override fun onTransitionEnd(transition: Transition) {
                        if (invisible) onCollapsed.invoke()
                    }

                    override fun onTransitionStart(transition: Transition) {}

                    override fun onTransitionCancel(transition: Transition) {}

                    override fun onTransitionPause(transition: Transition) {}

                    override fun onTransitionResume(transition: Transition) {}
                }))

        setTitleIcon(invisible)

        val animatedDrawable = TextViewCompat.getCompoundDrawablesRelative(searchTitle)[2] as AnimatedVectorDrawableCompat

        animatedDrawable.start()

        val visibility = if (invisible) View.GONE else View.VISIBLE
        searchButton.visibility = visibility
        optionsList.visibility = visibility
    }

    private fun toggleVisibility() {
        val invisible = optionsList.visibility == View.VISIBLE
        changeVisibility(invisible)
    }

    companion object {

        fun create(container: ViewGroup, onCollapsed: () -> Unit): ExpandingToolbar =
                ExpandingToolbar(container, onCollapsed)
    }
}
