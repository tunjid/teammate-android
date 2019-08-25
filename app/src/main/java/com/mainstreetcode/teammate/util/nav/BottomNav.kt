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

package com.mainstreetcode.teammate.util.nav

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import com.google.android.material.shape.MaterialShapeDrawable
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.util.setMaterialOverlay
import kotlin.math.min

class BottomNav constructor(
        private val container: LinearLayout,
        onSwipe: () -> Unit,
        onClick: View.OnClickListener,
        vararg navItems: NavItem) {

    private val viewHolders: Array<ViewHolder>

    init {
        val min = min(navItems.size, 5)
        val inflater = LayoutInflater.from(container.context)

        viewHolders = (0 until min).mapIndexed { index, _ ->
            val itemView = inflater.inflate(R.layout.bottom_nav_item, container, false)
            addViewHolder(ViewHolder(itemView, onSwipe))
                    .bind(navItems[index])
                    .setOnClickListener(onClick)
        }.toTypedArray()

        container.setMaterialOverlay()
    }

    fun highlight(@IdRes highlighted: Int) {
        for (viewHolder in viewHolders)
            viewHolder.tint(if (viewHolder.itemView.id == highlighted) R.attr.bottom_nav_selected else R.attr.bottom_nav_unselected)
    }

    fun getViewHolder(@IdRes id: Int): ViewHolder? {
        for (holder in viewHolders) if (holder.itemView.id == id) return holder
        return null
    }

    private fun addViewHolder(viewHolder: ViewHolder): ViewHolder {
        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        params.weight = 1f
        container.addView(viewHolder.itemView, params)
        return viewHolder
    }

    class Builder internal constructor() {
        private lateinit var container: LinearLayout
        private lateinit var listener: View.OnClickListener
        private lateinit var navItems: Array<out NavItem>
        private lateinit var onSwipe: (() -> Unit)

        fun setContainer(container: LinearLayout): Builder = apply { this.container = container }

        fun setSwipeRunnable(onSwipe: () -> Unit): Builder = apply { this.onSwipe = onSwipe }

        fun setListener(listener: View.OnClickListener): Builder = apply { this.listener = listener }

        fun setNavItems(vararg navItems: NavItem): Builder = apply { this.navItems = navItems }

        fun createBottomNav(): BottomNav = BottomNav(container, onSwipe, listener, *navItems)
    }

    companion object {

        fun builder(): Builder = Builder()
    }
}
