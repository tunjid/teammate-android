package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.EventViewHolder;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.AD;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.EVENT;

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
        return viewType == AD
                ? new ContentAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_content_ad, viewGroup), adapterListener)
                : new EventViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_event, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
        Identifiable item = items.get(position);
        if(item instanceof Event) ((EventViewHolder)viewHolder).bind((Event)item);
        else if(item instanceof Ad) ((ContentAdViewHolder)viewHolder).bind((Ad)item);
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
        return items.get(position) instanceof Event ? EVENT : AD;
    }

    public interface EventAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onEventClicked(Event item);
    }

}
