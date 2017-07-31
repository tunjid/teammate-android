package com.mainstreetcode.teammates.adapters.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.EventEditAdapter;
import com.mainstreetcode.teammates.model.User;
import com.squareup.picasso.Picasso;

public class EventGuestViewHolder extends UserHoldingViewHolder<EventEditAdapter.EditAdapterListener>
        implements View.OnClickListener {

    private User user;

    public EventGuestViewHolder(View itemView, EventEditAdapter.EditAdapterListener listener) {
        super(itemView, listener);
        itemView.setOnClickListener(this);
    }

    public void bind(User user, boolean attending) {
        this.user = user;

        Context context = itemView.getContext();

        userName.setText(user.getFirstName());
        userStatus.setText(context.getString(attending ? R.string.event_attending : R.string.event_not_attending));
        String imageUrl = user.getImageUrl() != null ? user.getImageUrl() : "";

        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(context)
                    .load(imageUrl)
                    .fit()
                    .centerInside()
                    .into(userPicture);
        }
    }

    @Override
    public void onClick(View view) {
        adapterListener.rsvpToEvent(user);
    }
}
