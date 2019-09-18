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


import androidx.annotation.AttrRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.model.ListState
import com.tunjid.androidbootstrap.recyclerview.AbstractListManagerBuilder
import com.tunjid.androidbootstrap.recyclerview.EndlessScroller
import com.tunjid.androidbootstrap.recyclerview.ListManager
import com.tunjid.androidbootstrap.recyclerview.SwipeDragOptions
import java.lang.IllegalArgumentException

class ScrollManager<VH : RecyclerView.ViewHolder> constructor(
        scroller: EndlessScroller?,
        private val viewHolder: EmptyViewHolder?,
        refreshLayout: SwipeRefreshLayout?,
        options: SwipeDragOptions<VH>?,
        recycledViewPool: RecyclerView.RecycledViewPool?,
        recyclerView: RecyclerView,
        adapter: Adapter<out VH>,
        layoutManager: LayoutManager,
        decorations: List<RecyclerView.ItemDecoration>,
        listeners: List<OnScrollListener>,
        hasFixedSize: Boolean
) : ListManager<VH, ListState>(
        scroller,
        viewHolder,
        refreshLayout,
        options,
        recycledViewPool,
        recyclerView,
        adapter,
        layoutManager,
        decorations,
        listeners,
        hasFixedSize
) {

    fun setViewHolderColor(@EmptyViewHolder.EmptyTint @AttrRes color: Int) {
        if (viewHolder == null || recyclerView?.adapter == null) return
        viewHolder.setColor(color)
        viewHolder.toggle(recyclerView?.adapter?.itemCount == 0)
    }

    class Builder<VH : RecyclerView.ViewHolder> : AbstractListManagerBuilder<Builder<VH>, ScrollManager<VH>, VH, ListState>() {

        override fun build(): ScrollManager<VH> {
            val recyclerView = this.recyclerView ?: throw IllegalArgumentException("RecyclerView is required")
            val adapter = this.adapter ?: throw IllegalArgumentException("RecyclerView is required")
            val viewHolder = placeholder as? EmptyViewHolder
            val layoutManager = buildLayoutManager()
            val scroller = buildEndlessScroller(layoutManager)
            val scrollListeners = buildScrollListeners()

            return ScrollManager(
                    scroller, viewHolder, refreshLayout, swipeDragOptions, recycledViewPool,
                    recyclerView, adapter, layoutManager, itemDecorations, scrollListeners, hasFixedSize)
        }

        fun withEndlessScroll(runnable: () -> Unit): Builder<VH> {
            this.endlessScrollVisibleThreshold = 5
            this.endlessScrollConsumer = { runnable.invoke() }
            return thisInstance
        }

        internal fun setRecyclerView(recyclerView: RecyclerView): Builder<VH> {
            this.recyclerView = recyclerView
            return thisInstance
        }
    }

    companion object {

        fun <VH : RecyclerView.ViewHolder> with(recyclerView: RecyclerView): Builder<VH> {
            val builder = Builder<VH>()
            return builder.setRecyclerView(recyclerView)
        }
    }
}
