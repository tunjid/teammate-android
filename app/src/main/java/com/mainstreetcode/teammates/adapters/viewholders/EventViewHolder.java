package com.mainstreetcode.teammates.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.EventAdapter;
import com.mainstreetcode.teammates.model.Event;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;


public class EventViewHolder extends BaseViewHolder<EventAdapter.EventAdapterListener>
        implements View.OnClickListener {

    private Event item;
    private ImageView image;
    private TextView eventName;
    private TextView eventTime;
    private TextView eventLocation;

    public EventViewHolder(View itemView, EventAdapter.EventAdapterListener adapterListener) {
        super(itemView, adapterListener);
        image = itemView.findViewById(R.id.thumbnail);
        eventName = itemView.findViewById(R.id.item_title);
        eventTime = itemView.findViewById(R.id.item_subtitle);
        eventLocation = itemView.findViewById(R.id.location);
        itemView.setOnClickListener(this);
    }

    public void bind(Event item) {
        this.item = item;
        eventName.setText(item.getName());
        eventTime.setText(item.getTime());

        eventLocation.setText(item.getLocationName());
        eventLocation.setVisibility(TextUtils.isEmpty(item.getLocationName()) ? View.GONE : View.VISIBLE);

        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Picasso.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .fit()
                    .centerCrop()
                    .into(image);
        }
    }

    @Override
    public void onClick(View view) {
        adapterListener.onEventClicked(item);
    }
}
