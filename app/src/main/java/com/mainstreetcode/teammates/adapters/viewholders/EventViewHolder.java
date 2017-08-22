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
    private TextView teamName;
    private TextView teamLocation;

    public EventViewHolder(View itemView, EventAdapter.EventAdapterListener adapterListener) {
        super(itemView, adapterListener);
        image = itemView.findViewById(R.id.thumbnail);
        teamName = itemView.findViewById(R.id.item_title);
        teamLocation = itemView.findViewById(R.id.item_subtitle);
        itemView.setOnClickListener(this);
    }

    public void bind(Event item) {
        this.item = item;
        teamName.setText(item.getName());
        teamLocation.setText(item.getTime());

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
