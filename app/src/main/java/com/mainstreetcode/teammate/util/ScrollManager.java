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

package com.mainstreetcode.teammate.util;


import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.model.ListState;
import com.tunjid.androidbootstrap.recyclerview.AbstractListManagerBuilder;
import com.tunjid.androidbootstrap.recyclerview.EndlessScroller;
import com.tunjid.androidbootstrap.recyclerview.ListManager;
import com.tunjid.androidbootstrap.recyclerview.SwipeDragOptions;

import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ScrollManager<VH extends RecyclerView.ViewHolder> extends ListManager<VH, ListState> {

    private final EmptyViewHolder viewHolder;

    protected ScrollManager(@Nullable EndlessScroller scroller,
                            @Nullable EmptyViewHolder placeholder,
                            @Nullable SwipeRefreshLayout refreshLayout,
                            @Nullable SwipeDragOptions<VH> options,
                            @Nullable RecyclerView.RecycledViewPool recycledViewPool,
                            RecyclerView recyclerView, Adapter<? extends VH> adapter,
                            LayoutManager layoutManager,
                            List<RecyclerView.ItemDecoration> decorations,
                            List<OnScrollListener> listeners,
                            boolean hasFixedSize) {

        super(scroller, placeholder, refreshLayout,
                options, recycledViewPool, recyclerView,
                adapter, layoutManager, decorations,
                listeners, hasFixedSize);

        viewHolder = placeholder;
    }

    public static <VH extends RecyclerView.ViewHolder> Builder<VH> with(RecyclerView recyclerView) {
        Builder<VH> builder = new Builder<>();
        return builder.setRecyclerView(recyclerView);
    }

    public void setViewHolderColor(@EmptyViewHolder.EmptyTint @AttrRes int color) {
        if (viewHolder == null || adapter == null) return;
        viewHolder.setColor(color);
        viewHolder.toggle(adapter.getItemCount() == 0);
    }

    public static class Builder<VH extends RecyclerView.ViewHolder>
            extends AbstractListManagerBuilder<
            Builder<VH>,
            ScrollManager<VH>,
            VH,
            ListState> {

        @Override
        public ScrollManager<VH> build() {
            EmptyViewHolder viewHolder = (EmptyViewHolder) placeholder;
            RecyclerView.LayoutManager layoutManager = buildLayoutManager();
            EndlessScroller scroller = buildEndlessScroller(layoutManager);
            List<RecyclerView.OnScrollListener> scrollListeners = buildScrollListeners();

            return new ScrollManager<>(
                    scroller, viewHolder, refreshLayout, swipeDragOptions, recycledViewPool,
                    recyclerView, adapter, layoutManager, itemDecorations, scrollListeners, hasFixedSize);
        }

        public Builder<VH> withEndlessScroll(Runnable runnable) {
            this.endlessScrollVisibleThreshold = 5;
            this.endlessScrollConsumer = __ -> runnable.run();
            return thisInstance;
        }

        Builder<VH> setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            return thisInstance;
        }
    }
}
