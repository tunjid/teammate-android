package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.FeedItemViewHolder;
import com.mainstreetcode.teammates.model.ContentAd;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.notifications.FeedItem;
import com.mainstreetcode.teammates.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.FEED_ITEM;

/**
 * Adapter for {@link FeedItem}
 */

public class FeedAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, FeedAdapter.FeedItemAdapterListener> {

    private final List<Identifiable> items;

    public FeedAdapter(List<Identifiable> items, FeedItemAdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_list_content_ad, viewGroup), adapterListener)
                : new FeedItemViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
        Identifiable item = items.get(position);
        if (item instanceof FeedItem) ((FeedItemViewHolder) viewHolder).bind((FeedItem) item);
        else if (item instanceof ContentAd) ((ContentAdViewHolder) viewHolder).bind((ContentAd) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof FeedItem ? FEED_ITEM : CONTENT_AD;
    }

    public interface FeedItemAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onFeedItemClicked(FeedItem item);
    }

}
