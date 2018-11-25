package com.mainstreetcode.teammate.util;


import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags;

public class ScrollManager {

    private static final String TAG = "ScrollManager";

    @Nullable
    private EndlessScroller scroller;
    @Nullable
    private EmptyViewHolder viewHolder;
    @Nullable
    private ItemTouchHelper touchHelper;
    @Nullable
    private SwipeRefreshLayout refreshLayout;

    private RecyclerView recyclerView;
    private Adapter adapter;

    private ScrollManager(@Nullable EndlessScroller scroller,
                          @Nullable EmptyViewHolder viewHolder,
                          @Nullable SwipeRefreshLayout refreshLayout,
                          @Nullable SwipeDragOptions options,
                          @Nullable RecyclerView.RecycledViewPool recycledViewPool,
                          RecyclerView recyclerView, Adapter adapter, LayoutManager layoutManager,
                          List<OnScrollListener> listeners,
                          boolean hasFixedSize,
                          boolean hasLines) {

        this.scroller = scroller;
        this.viewHolder = viewHolder;
        this.refreshLayout = refreshLayout;

        this.recyclerView = recyclerView;
        this.adapter = adapter;

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        if (hasFixedSize) recyclerView.setHasFixedSize(true);
        if (scroller != null) recyclerView.addOnScrollListener(scroller);
        if (recycledViewPool != null) recyclerView.setRecycledViewPool(recycledViewPool);
        if (options != null) touchHelper = fromSwipeDragOptions(this, options);
        if (touchHelper != null) touchHelper.attachToRecyclerView(recyclerView);
        for (OnScrollListener listener : listeners) recyclerView.addOnScrollListener(listener);
        if (hasLines && layoutManager instanceof LinearLayoutManager)
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), ((LinearLayoutManager) layoutManager).getOrientation()));
    }

    public static Builder withRecyclerView(RecyclerView recyclerView) {
        Builder builder = new Builder();
        builder.recyclerView = recyclerView;
        return builder;
    }

    public static SwipeDragOptionsBuilder swipeDragOptionsBuilder() {
        return new SwipeDragOptionsBuilder();
    }

    public void updateForEmptyList(@DrawableRes int iconRes, @StringRes int stringRes) {
        if (viewHolder != null) viewHolder.update(iconRes, stringRes);
    }

    public void setViewHolderColor(@EmptyViewHolder.EmptyTint @AttrRes int color) {
        if (viewHolder == null || adapter == null) return;
        viewHolder.setColor(color);
        viewHolder.toggle(adapter.getItemCount() == 0);
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
        if (viewHolder != null && adapter != null) viewHolder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyItemInserted(int position) {
        if (adapter != null) adapter.notifyItemInserted(position);
        if (viewHolder != null && adapter != null) viewHolder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyItemRemoved(int position) {
        if (adapter != null) adapter.notifyItemRemoved(position);
        if (viewHolder != null && adapter != null) viewHolder.toggle(adapter.getItemCount() == 0);
    }

    @SuppressWarnings("WeakerAccess")
    public void notifyItemMoved(int from, int to) {
        if (adapter != null) adapter.notifyItemMoved(from, to);
        if (viewHolder != null && adapter != null) viewHolder.toggle(adapter.getItemCount() == 0);
    }

    public void notifyItemRangeChanged(int start, int count) {
        if (adapter != null) adapter.notifyItemRangeChanged(start, count);
        if (viewHolder != null && adapter != null) viewHolder.toggle(adapter.getItemCount() == 0);
    }

    public void startDrag(RecyclerView.ViewHolder viewHolder) {
        if (touchHelper != null) touchHelper.startDrag(viewHolder);
    }

    public void setRefreshing() { if (refreshLayout != null) refreshLayout.setRefreshing(true); }

    public void reset() {
        if (scroller != null) scroller.reset();
        if (refreshLayout != null) refreshLayout.setRefreshing(false);
    }

    public void clear() {
        if (recyclerView != null) recyclerView.clearOnScrollListeners();

        scroller = null;
        refreshLayout = null;
        viewHolder = null;

        recyclerView = null;
        adapter = null;
    }

    @SuppressWarnings("unused")
    public int getFirstVisiblePosition() {
        LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager castedManager = (LinearLayoutManager) layoutManager;
            return castedManager.findFirstVisibleItemPosition();
        }
        else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager castedManager = (StaggeredGridLayoutManager) layoutManager;

            int[] positions = new int[castedManager.getSpanCount()];
            castedManager.findFirstVisibleItemPositions(positions);

            List<Integer> indexes = new ArrayList<>(positions.length);
            for (int i : positions) indexes.add(i);

            return Collections.min(indexes);
        }
        return -1;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public RecyclerView.ViewHolder findViewHolderForItemId(long id) {
        return recyclerView.findViewHolderForItemId(id);
    }

    public static class Builder {

        private static final int LINEAR_LAYOUT_MANAGER = 0;
        private static final int GRID_LAYOUT_MANAGER = 1;
        private static final int STAGGERED_GRID_LAYOUT_MANAGER = 2;

        int spanCount;
        int layoutManagerType;
        boolean hasFixedSize;
        boolean hasLines;

        Runnable scrollCallback;
        SwipeRefreshLayout refreshLayout;
        EmptyViewHolder emptyViewholder;

        RecyclerView recyclerView;
        Adapter adapter;
        LayoutManager layoutManager;

        SwipeDragOptions swipeDragOptions;
        RecyclerView.RecycledViewPool recycledViewPool;
        ModelUtils.Consumer<LayoutManager> layoutManagerConsumer;
        ModelUtils.Consumer<IndexOutOfBoundsException> handler;
        List<ModelUtils.Consumer<Integer>> stateConsumers = new ArrayList<>();
        List<BiConsumer<Integer, Integer>> displacementConsumers = new ArrayList<>();

        private Builder() {}

        public Builder setHasFixedSize() {
            this.hasFixedSize = true;
            return this;
        }

        public Builder withLines() {
            this.hasLines = true;
            return this;
        }

        public Builder withAdapter(@NonNull Adapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public Builder onLayoutManager(ModelUtils.Consumer<LayoutManager> layoutManagerConsumer) {
            this.layoutManagerConsumer = layoutManagerConsumer;
            return this;
        }

        public Builder withLinearLayoutManager() {
            layoutManagerType = LINEAR_LAYOUT_MANAGER;
            return this;
        }

        public Builder withGridLayoutManager(int spanCount) {
            layoutManagerType = GRID_LAYOUT_MANAGER;
            this.spanCount = spanCount;
            return this;
        }

        public Builder withStaggeredGridLayoutManager(int spanCount) {
            layoutManagerType = STAGGERED_GRID_LAYOUT_MANAGER;
            this.spanCount = spanCount;
            return this;
        }

        public Builder withRecycledViewPool(RecyclerView.RecycledViewPool recycledViewPool) {
            this.recycledViewPool = recycledViewPool;
            return this;
        }

        public Builder withInconsistencyHandler(ModelUtils.Consumer<IndexOutOfBoundsException> handler) {
            this.handler = handler;
            return this;
        }

        public Builder withEndlessScrollCallback(@NonNull Runnable scrollCallback) {
            this.scrollCallback = scrollCallback;
            return this;
        }

        public Builder addStateListener(@NonNull ModelUtils.Consumer<Integer> stateListener) {
            this.stateConsumers.add(stateListener);
            return this;
        }

        public Builder addScrollListener(@NonNull BiConsumer<Integer, Integer> scrollListener) {
            this.displacementConsumers.add(scrollListener);
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

        public Builder withSwipeDragOptions(@NonNull SwipeDragOptions swipeDragOptions) {
            this.swipeDragOptions = swipeDragOptions;
            return this;
        }

        public ScrollManager build() {
            switch (layoutManagerType) {
                case STAGGERED_GRID_LAYOUT_MANAGER:
                    layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL) {
                        @Override
                        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                            try { super.onLayoutChildren(recycler, state);}
                            catch (IndexOutOfBoundsException e) {handler.accept(e);}
                        }
                    };
                    break;
                case GRID_LAYOUT_MANAGER:
                    layoutManager = new GridLayoutManager(recyclerView.getContext(), spanCount) {
                        @Override
                        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                            try { super.onLayoutChildren(recycler, state);}
                            catch (IndexOutOfBoundsException e) {handler.accept(e);}
                        }
                    };
                    break;
                case LINEAR_LAYOUT_MANAGER:
                    layoutManager = new LinearLayoutManager(recyclerView.getContext()) {
                        @Override
                        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                            try { super.onLayoutChildren(recycler, state);}
                            catch (IndexOutOfBoundsException e) {handler.accept(e);}
                        }
                    };
                    break;
            }

            if (recyclerView == null)
                throw new IllegalArgumentException("RecyclerView must be provided");
            if (layoutManager == null)
                throw new IllegalArgumentException("RecyclerView LayoutManager must be provided");
            if (adapter == null)
                throw new IllegalArgumentException("RecyclerView Adapter must be provided");
            if (handler == null)
                throw new IllegalArgumentException("InconsistencyHandler must be provided");

            if (layoutManager instanceof LinearLayoutManager)
                ((LinearLayoutManager) layoutManager).setRecycleChildrenOnDetach(true);
            if (layoutManagerConsumer != null) layoutManagerConsumer.accept(layoutManager);

            EndlessScroller scroller = scrollCallback == null ? null : new EndlessScroller(layoutManager) {
                @Override
                public void onLoadMore(int currentItemCount) {scrollCallback.run();}
            };

            int stateConsumersSize = stateConsumers.size();
            int scrollConsumersSize = displacementConsumers.size();
            int max = Math.max(stateConsumersSize, scrollConsumersSize);

            List<OnScrollListener> scrollListeners = new ArrayList<>(max);


            for (int i = 0; i < max; i++) {
                final ModelUtils.Consumer<Integer> consumer;
                final BiConsumer<Integer, Integer> biConsumer;

                if (i < stateConsumersSize) consumer = stateConsumers.get(i);
                else consumer = null;
                if (i < scrollConsumersSize) biConsumer = displacementConsumers.get(i);
                else biConsumer = null;

                scrollListeners.add(new OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (biConsumer == null) return;
                        try { biConsumer.accept(dx, dy);}
                        catch (Exception e) {
                            Logger.log(TAG, "Unable to dispatch scroll callback", e);
                        }
                    }

                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        if (consumer == null) return;
                        try { consumer.accept(newState);}
                        catch (Exception e) {
                            Logger.log(TAG, "Unable to dispatch scroll changed callback", e);
                        }
                    }
                });
            }

            stateConsumers.clear();
            displacementConsumers.clear();

            return new ScrollManager(
                    scroller, emptyViewholder, refreshLayout, swipeDragOptions, recycledViewPool,
                    recyclerView, adapter, layoutManager, scrollListeners, hasFixedSize, hasLines);
        }
    }

    static class SwipeDragOptions {
        ModelUtils.Consumer<RecyclerView.ViewHolder> swipeDragEndConsumerConsumer;
        BiConsumer<RecyclerView.ViewHolder, Integer> swipeDragStartConsumerConsumer;
        Supplier<Boolean> itemViewSwipeSupplier;
        Supplier<Boolean> longPressDragSupplier;
        Function<RecyclerView.ViewHolder, Integer> movementFlagsSupplier;
        Supplier<List> listSupplier;

        SwipeDragOptions(ModelUtils.Consumer<RecyclerView.ViewHolder> swipeDragEndConsumerConsumer,
                         BiConsumer<RecyclerView.ViewHolder, Integer> swipeDragStartConsumerConsumer,
                         Supplier<Boolean> itemViewSwipeSupplier,
                         Supplier<Boolean> longPressDragSupplier,
                         Function<RecyclerView.ViewHolder, Integer> movementFlagsSupplier,
                         Supplier<List> listSupplier) {
            this.swipeDragEndConsumerConsumer = swipeDragEndConsumerConsumer;
            this.swipeDragStartConsumerConsumer = swipeDragStartConsumerConsumer;
            this.itemViewSwipeSupplier = itemViewSwipeSupplier;
            this.longPressDragSupplier = longPressDragSupplier;
            this.movementFlagsSupplier = movementFlagsSupplier;
            this.listSupplier = listSupplier;
        }
    }

    private static ItemTouchHelper fromSwipeDragOptions(ScrollManager scrollManager, SwipeDragOptions options) {
        return new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public boolean isItemViewSwipeEnabled() { return options.itemViewSwipeSupplier.get(); }

            @Override
            public boolean isLongPressDragEnabled() { return options.longPressDragSupplier.get(); }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                try { return options.movementFlagsSupplier.apply(viewHolder); }
                catch (Exception e) { e.printStackTrace(); }
                return defaultMovements();
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                List items = options.listSupplier.get();

                if (from < to) for (int i = from; i < to; i++) Collections.swap(items, i, i + 1);
                else for (int i = from; i > to; i--) Collections.swap(items, i, i - 1);

                scrollManager.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                options.listSupplier.get().remove(position);
                scrollManager.notifyItemRemoved(position);
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                try { options.swipeDragStartConsumerConsumer.accept(viewHolder, actionState); }
                catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                options.swipeDragEndConsumerConsumer.accept(viewHolder);
            }
        });
    }

    public static class SwipeDragOptionsBuilder {
        private ModelUtils.Consumer<RecyclerView.ViewHolder> swipeDragEndConsumerConsumer = viewHolder -> {};
        private BiConsumer<RecyclerView.ViewHolder, Integer> swipeDragStartConsumerConsumer = (viewHolder, state) -> {};
        private Supplier<Boolean> itemViewSwipeSupplier = () -> false;
        private Supplier<Boolean> longPressDragEnabledSupplier = () -> false;
        private Function<RecyclerView.ViewHolder, Integer> movementFlagsSupplier = viewHolder -> defaultMovements();
        private Supplier<List> listSupplier;

        public SwipeDragOptionsBuilder setSwipeDragEndConsumer(ModelUtils.Consumer<RecyclerView.ViewHolder> swipeDragEndConsumerConsumer) {
            this.swipeDragEndConsumerConsumer = swipeDragEndConsumerConsumer;
            return this;
        }

        public SwipeDragOptionsBuilder setSwipeDragStartConsumerConsumer(BiConsumer<RecyclerView.ViewHolder, Integer> swipeDragStartConsumerConsumer) {
            this.swipeDragStartConsumerConsumer = swipeDragStartConsumerConsumer;
            return this;
        }

        public SwipeDragOptionsBuilder setItemViewSwipeSupplier(Supplier<Boolean> itemViewSwipeSupplier) {
            this.itemViewSwipeSupplier = itemViewSwipeSupplier;
            return this;
        }

        public SwipeDragOptionsBuilder setLongPressDragEnabledSupplier(Supplier<Boolean> longPressDragEnabledSupplier) {
            this.longPressDragEnabledSupplier = longPressDragEnabledSupplier;
            return this;
        }

        public SwipeDragOptionsBuilder setMovementFlagsSupplier(Function<RecyclerView.ViewHolder, Integer> movementFlagsSupplier) {
            this.movementFlagsSupplier = movementFlagsSupplier;
            return this;
        }

        public SwipeDragOptionsBuilder setListSupplier(Supplier<List> listSupplier) {
            this.listSupplier = listSupplier;
            return this;
        }

        public ScrollManager.SwipeDragOptions build() {
            return new ScrollManager.SwipeDragOptions(swipeDragEndConsumerConsumer, swipeDragStartConsumerConsumer, itemViewSwipeSupplier, longPressDragEnabledSupplier, movementFlagsSupplier, listSupplier);
        }
    }

    public static int defaultMovements() {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }
}
