package com.mainstreetcode.teammates.util;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessScroller extends RecyclerView.OnScrollListener {

    private static final int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.

    private final boolean isReverse;
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.

    private LinearLayoutManager mLinearLayoutManager;

    protected EndlessScroller(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
        isReverse = mLinearLayoutManager.getStackFromEnd();
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy == 0) return;

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = mLinearLayoutManager.getItemCount();
        int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        int numScrolled = totalItemCount - visibleItemCount;
        int refreshTrigger = isReverse ? totalItemCount - firstVisibleItem : firstVisibleItem;
        refreshTrigger += visibleThreshold;

        if (loading && totalItemCount > previousTotal) {
            loading = false;
            previousTotal = totalItemCount;
        }

        if (!loading && numScrolled <= refreshTrigger) {
            loading = true;
            onLoadMore(totalItemCount);
        }
    }

    public abstract void onLoadMore(int currentItemCount);
}
