package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.FeedItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammate.model.Ad;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.notifications.FeedItem;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.FEED_ITEM;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.INSTALL_AD;

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

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_list_content_ad, viewGroup), adapterListener)
                : viewType == INSTALL_AD
                ? new InstallAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
                : new FeedItemViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int position) {
        Identifiable item = items.get(position);
        if (item instanceof FeedItem) ((FeedItemViewHolder) viewHolder).bind((FeedItem) item);
        else if (item instanceof Ad) ((AdViewHolder) viewHolder).bind((Ad) item);
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
        Identifiable item = items.get(position);
        return item instanceof FeedItem ? FEED_ITEM : ((Ad) item).getType();
    }

    public interface FeedItemAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onFeedItemClicked(FeedItem item);
    }

}
