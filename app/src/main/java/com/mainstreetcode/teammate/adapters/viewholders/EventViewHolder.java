package com.mainstreetcode.teammate.adapters.viewholders;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.EventAdapter;
import com.mainstreetcode.teammate.model.Event;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;


public class EventViewHolder extends ModelCardViewHolder<Event, EventAdapter.EventAdapterListener>
        implements View.OnClickListener {

    private TextView eventLocation;

    public EventViewHolder(View itemView, EventAdapter.EventAdapterListener adapterListener) {
        super(itemView, adapterListener);
        eventLocation = itemView.findViewById(R.id.location);
        itemView.setOnClickListener(this);
    }

    public void bind(Event item) {
        super.bind(item);
        title.setText(item.getName());
        subtitle.setText(item.getTime());
        eventLocation.setText(item.getLocationName());

        ViewCompat.setTransitionName(itemView, getTransitionName(item, R.id.fragment_header_background));
        ViewCompat.setTransitionName(thumbnail, getTransitionName(item, R.id.fragment_header_thumbnail));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onEventClicked(model);
    }

    public ImageView getImage() {
        return thumbnail;
    }
}
