package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Event;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for {@link com.mainstreetcode.teammates.model.Event}
 */

public class EventAdapter extends BaseRecyclerViewAdapter<EventAdapter.EventViewHolder, EventAdapter.EventAdapterListener> {

    private final List<Event> items;

    public EventAdapter(List<Event> items, EventAdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_events, viewGroup, false);
        return new EventViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(EventViewHolder eventViewHolder, int i) {
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

    public interface EventAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onEventClicked(Event item);
    }

    static class EventViewHolder extends BaseViewHolder<EventAdapterListener>
            implements View.OnClickListener {

        private Event item;
        //private ImageView teamLogo;
        private TextView teamName;
        private TextView teamLocation;

        EventViewHolder(View itemView, EventAdapterListener adapterListener) {
            super(itemView, adapterListener);
            //teamLogo = itemView.findViewById(R.id.thumbnail);
            teamName = itemView.findViewById(R.id.name);
            teamLocation = itemView.findViewById(R.id.time);
            itemView.setOnClickListener(this);
        }

        void bind(Event item) {
            this.item = item;
            teamName.setText(item.getName());
            teamLocation.setText(item.getTime());
//
//            Picasso.with(itemView.getContext())
//                    .load(item.getLogoUrl())
//                    .fit()
//                    .centerInside()
//                    .into(teamLogo);
        }

        @Override
        public void onClick(View view) {
            adapterListener.onEventClicked(item);
        }
    }
}
