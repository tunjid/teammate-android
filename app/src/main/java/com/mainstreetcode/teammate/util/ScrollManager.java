package com.mainstreetcode.teammate.util;


import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.model.ListState;
import com.tunjid.androidbootstrap.recyclerview.EndlessScroller;
import com.tunjid.androidbootstrap.recyclerview.SwipeDragOptions;

import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ScrollManager<VH extends RecyclerView.ViewHolder>
        extends com.tunjid.androidbootstrap.recyclerview.ScrollManager<VH, ListState> {

    private final EmptyViewHolder viewHolder;

    protected ScrollManager(@Nullable EndlessScroller scroller,
                            @Nullable EmptyViewHolder placeholder,
                            @Nullable SwipeRefreshLayout refreshLayout,
                            @Nullable SwipeDragOptions<VH> options,
                            @Nullable RecyclerView.RecycledViewPool recycledViewPool,
                            RecyclerView recyclerView, Adapter<VH> adapter,
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
            extends com.tunjid.androidbootstrap.recyclerview.AbstractScrollManagerBuilder<
            ScrollManager.Builder<VH>,
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
