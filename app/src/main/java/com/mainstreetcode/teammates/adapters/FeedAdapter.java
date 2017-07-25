package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.FeedItemViewHolder;
import com.mainstreetcode.teammates.model.FeedItem;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link com.mainstreetcode.teammates.model.FeedItem}
 */

public class FeedAdapter extends BaseRecyclerViewAdapter<FeedItemViewHolder, FeedAdapter.FeedItemAdapterListener> {

    private final List<FeedItem> items;

    public FeedAdapter(List<FeedItem> items) {
        //super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @Override
    public FeedItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_feed, viewGroup, false);
        return new FeedItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FeedItemViewHolder eventViewHolder, int i) {
        eventViewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    interface FeedItemAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        //void onFeedItemClicked(FeedItem item);
    }

}
