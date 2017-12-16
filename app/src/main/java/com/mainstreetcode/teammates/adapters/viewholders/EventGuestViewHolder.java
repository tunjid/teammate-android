package com.mainstreetcode.teammates.adapters.viewholders;

import android.content.Context;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.EventEditAdapter;
import com.mainstreetcode.teammates.model.User;

public class EventGuestViewHolder extends ModelCardViewHolder<User, EventEditAdapter.EditAdapterListener>
        implements View.OnClickListener {

    public EventGuestViewHolder(View itemView, EventEditAdapter.EditAdapterListener listener) {
        super(itemView, listener);
        itemView.setOnClickListener(this);
    }

    public void bind(User model, boolean attending) {
        super.bind(model);

        Context context = itemView.getContext();

        title.setText(model.getFirstName());
        subtitle.setText(context.getString(attending ? R.string.event_attending : R.string.event_not_attending));
    }

    @Override
    public void onClick(View view) {
        adapterListener.rsvpToEvent(model);
    }
}
