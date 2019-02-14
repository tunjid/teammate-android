package com.mainstreetcode.teammate.util;


import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.model.ListState;
import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.Consumer;
import com.tunjid.androidbootstrap.recyclerview.EndlessScroller;
import com.tunjid.androidbootstrap.recyclerview.ListPlaceholder;
import com.tunjid.androidbootstrap.recyclerview.SwipeDragOptions;

import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ScrollManager<VH extends RecyclerView.ViewHolder>
        extends com.tunjid.androidbootstrap.recyclerview.ScrollManager<ListState, VH> {

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
            extends com.tunjid.androidbootstrap.recyclerview.AbstractScrollManagerBuilder<ScrollManager<VH>, ListState, VH> {

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
            return this;
        }

        Builder<VH> setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            return this;
        }

        @Override
        public Builder<VH> setHasFixedSize() {
            super.setHasFixedSize();
            return this;
        }

        @Override
        public Builder<VH> onLayoutManager(Consumer<LayoutManager> layoutManagerConsumer) {
            super.onLayoutManager(layoutManagerConsumer);
            return this;
        }

        @Override
        public Builder<VH> withLinearLayoutManager() {
            super.withLinearLayoutManager();
            return this;
        }

        @Override
        public Builder<VH> withGridLayoutManager(int spanCount) {
            super.withGridLayoutManager(spanCount);
            return this;
        }

        @Override
        public Builder<VH> withStaggeredGridLayoutManager(int spanCount) {
            super.withStaggeredGridLayoutManager(spanCount);
            return this;
        }

        @Override
        public Builder<VH> withRecycledViewPool(RecyclerView.RecycledViewPool recycledViewPool) {
            super.withRecycledViewPool(recycledViewPool);
            return this;
        }

        @Override
        public Builder<VH> withInconsistencyHandler(Consumer<IndexOutOfBoundsException> handler) {
            super.withInconsistencyHandler(handler);
            return this;
        }

        @Override
        public Builder<VH> withEndlessScrollCallback(int threshold, @NonNull Consumer<Integer> endlessScrollConsumer) {
            super.withEndlessScrollCallback(threshold, endlessScrollConsumer);
            return this;
        }

        @Override
        public Builder<VH> addStateListener(@NonNull Consumer<Integer> stateListener) {
            super.addStateListener(stateListener);
            return this;
        }

        @Override
        public Builder<VH> addScrollListener(@NonNull BiConsumer<Integer, Integer> scrollListener) {
            super.addScrollListener(scrollListener);
            return this;
        }

        @Override
        public Builder<VH> addDecoration(@NonNull RecyclerView.ItemDecoration decoration) {
            super.addDecoration(decoration);
            return this;
        }

        @Override
        public Builder<VH> withRefreshLayout(@NonNull SwipeRefreshLayout refreshLayout, Runnable refreshAction) {
            super.withRefreshLayout(refreshLayout, refreshAction);
            return this;
        }

        @Override
        public Builder<VH> withAdapter(@NonNull Adapter<VH> adapter) {
            super.withAdapter(adapter);
            return this;
        }

        @Override
        public Builder<VH> withPlaceholder(@NonNull ListPlaceholder<ListState> placeholder) {
            super.withPlaceholder(placeholder);
            return this;
        }

        @Override
        public Builder<VH> withSwipeDragOptions(@NonNull SwipeDragOptions<VH> swipeDragOptions) {
            super.withSwipeDragOptions(swipeDragOptions);
            return this;
        }
    }
}
