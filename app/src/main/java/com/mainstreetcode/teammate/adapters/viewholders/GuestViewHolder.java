package com.mainstreetcode.teammate.adapters.viewholders;

import android.content.Context;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.EventEditAdapter;
import com.mainstreetcode.teammate.model.Guest;

public class GuestViewHolder extends ModelCardViewHolder<Guest, EventEditAdapter.EventEditAdapterListener>
        implements View.OnClickListener {

    public GuestViewHolder(View itemView, EventEditAdapter.EventEditAdapterListener listener) {
        super(itemView, listener);
        itemView.setOnClickListener(this);
    }

    @Override
    public void bind(Guest model) {
        super.bind(model);

        Context context = itemView.getContext();

        title.setText(model.getUser().getFirstName());
        subtitle.setText(context.getString(model.isAttending() ? R.string.event_attending : R.string.event_not_attending));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onGuestClicked(model);
    }
}
