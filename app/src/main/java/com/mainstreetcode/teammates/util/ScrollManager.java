package com.mainstreetcode.teammates.util;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;

import com.mainstreetcode.teammates.adapters.viewholders.EmptyViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

public class ScrollManager {

    private static final String TAG = "ScrollManager";

    @Nullable private EndlessScroller scroller;
    @Nullable private EmptyViewHolder viewHolder;
    @Nullable private SwipeRefreshLayout refreshLayout;

    private RecyclerView recyclerView;
    private Adapter adapter;

    private ScrollManager(@Nullable EndlessScroller scroller,
                          @Nullable EmptyViewHolder viewHolder,
                          @Nullable SwipeRefreshLayout refreshLayout,
                          @Nullable InconsistentDelegate.InconsistencyHandler handler,
                          RecyclerView recyclerView, Adapter adapter, LayoutManager layoutManager,
                          List<OnScrollListener> listeners) {

        this.scroller = scroller;
        this.viewHolder = viewHolder;
        this.refreshLayout = refreshLayout;

        this.recyclerView = recyclerView;
        this.adapter = adapter;

        recyclerView.setLayoutManager(handler != null ? InconsistentDelegate.wrap(layoutManager, handler) : layoutManager);
        recyclerView.setAdapter(adapter);

        if (scroller != null) recyclerView.addOnScrollListener(scroller);
        for (OnScrollListener listener : listeners) recyclerView.addOnScrollListener(listener);
    }

    public static Builder withRecyclerView(RecyclerView recyclerView) {
        Builder builder = new Builder();
        builder.recyclerView = recyclerView;
        return builder;
    }

    public void onDiff(DiffUtil.DiffResult result) {
        boolean hasAdapter = adapter != null;
        if (hasAdapter) result.dispatchUpdatesTo(adapter);
        if (refreshLayout != null) refreshLayout.setRefreshing(false);
        if (viewHolder != null && hasAdapter) viewHolder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyDataSetChanged() {
        boolean hasAdapter = adapter != null;
        if (hasAdapter) adapter.notifyDataSetChanged();
        if (refreshLayout != null) refreshLayout.setRefreshing(false);
        if (viewHolder != null && hasAdapter) viewHolder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyItemChanged(int position) {
        if (adapter != null) adapter.notifyItemChanged(position);
    }

    public void notifyItemRemoved(int position) {
        if (adapter != null) adapter.notifyItemRemoved(position);
    }

    public void reset() {
        if (scroller != null) scroller.reset();
    }

    public void clear() {
        if (recyclerView != null) recyclerView.clearOnScrollListeners();

        scroller = null;
        refreshLayout = null;
        viewHolder = null;

        recyclerView = null;
        adapter = null;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public RecyclerView.ViewHolder findViewHolderForItemId(long id) {
        return recyclerView.findViewHolderForItemId(id);
    }

    public static class Builder {

        Runnable scrollCallback;
        SwipeRefreshLayout refreshLayout;
        EmptyViewHolder emptyViewholder;
        InconsistentDelegate.InconsistencyHandler handler;

        RecyclerView recyclerView;
        Adapter adapter;
        LayoutManager layoutManager;

        List<Consumer<Integer>> stateConsumers = new ArrayList<>();
        List<BiConsumer<Integer, Integer>> displacementConsumers = new ArrayList<>();

        private Builder() {}

        public Builder withAdapter(@NonNull Adapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public Builder withLayoutManager(@NonNull LayoutManager layoutManager) {
            this.layoutManager = layoutManager;
            return this;
        }

        public Builder withEndlessScrollCallback(@NonNull Runnable scrollCallback) {
            this.scrollCallback = scrollCallback;
            return this;
        }

        public Builder withStateListener(@NonNull Consumer<Integer> stateListener) {
            this.stateConsumers.add(stateListener);
            return this;
        }

        public Builder withScrollListener(@NonNull BiConsumer<Integer, Integer> scrollListener) {
            this.displacementConsumers.add(scrollListener);
            return this;
        }

        public Builder withInconsistencyHandler(@NonNull InconsistentDelegate.InconsistencyHandler handler) {
            this.handler = handler;
            return this;
        }

        public Builder withRefreshLayout(@NonNull SwipeRefreshLayout refreshLayout, Runnable refreshAction) {
            this.refreshLayout = refreshLayout;
            refreshLayout.setOnRefreshListener(refreshAction::run);
            return this;
        }

        public Builder withEmptyViewholder(@NonNull EmptyViewHolder emptyViewholder) {
            this.emptyViewholder = emptyViewholder;
            return this;
        }

        public ScrollManager build() {
            if (recyclerView == null)
                throw new IllegalArgumentException("RecyclerView must be provided");
            if (layoutManager == null)
                throw new IllegalArgumentException("RecyclerView LayoutManager must be provided");
            if (adapter == null)
                throw new IllegalArgumentException("RecyclerView Adapter must be provided");

            EndlessScroller scroller = scrollCallback == null ? null : new EndlessScroller(layoutManager) {
                @Override
                public void onLoadMore(int currentItemCount) {scrollCallback.run();}
            };

            int stateConsumersSize = stateConsumers.size();
            int scrollConsumersSize = displacementConsumers.size();
            int max = Math.max(stateConsumersSize, scrollConsumersSize);

            List<OnScrollListener> scrollListeners = new ArrayList<>(max);


            for (int i = 0; i < max; i++) {
                final Consumer<Integer> consumer;
                final BiConsumer<Integer, Integer> biConsumer;

                if (i < stateConsumersSize) consumer = stateConsumers.get(i);
                else consumer = null;
                if (i < scrollConsumersSize) biConsumer = displacementConsumers.get(i);
                else biConsumer = null;

                scrollListeners.add(new OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (biConsumer == null) return;
                        try { biConsumer.accept(dx, dy);}
                        catch (Exception e) {Logger.log(TAG, "Unable to dispatch scroll callback", e);}
                    }

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        if (consumer == null) return;
                        try { consumer.accept(newState);}
                        catch (Exception e) {Logger.log(TAG, "Unable to dispatch scroll changed callback", e);}
                    }
                });
            }

            stateConsumers.clear();
            displacementConsumers.clear();

            return new ScrollManager(scroller, emptyViewholder, refreshLayout, handler, recyclerView, adapter, layoutManager, scrollListeners);
        }
    }
}
