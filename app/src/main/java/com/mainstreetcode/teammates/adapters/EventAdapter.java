package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.AdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.EventViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.EVENT;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.INSTALL_AD;

/**
 * Adapter for {@link com.mainstreetcode.teammates.model.Event}
 */

public class EventAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, EventAdapter.EventAdapterListener> {

    private final List<Identifiable> items;

    public EventAdapter(List<Identifiable> items, EventAdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_content_ad, viewGroup), adapterListener)
                : viewType == INSTALL_AD
                ? new InstallAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
                : new EventViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_event, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
        Identifiable item = items.get(position);
        if (item instanceof Event) ((EventViewHolder) viewHolder).bind((Event) item);
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
        return item instanceof Event ? EVENT : ((Ad) item).getType();
    }

    public interface EventAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onEventClicked(Event item);
    }

}
