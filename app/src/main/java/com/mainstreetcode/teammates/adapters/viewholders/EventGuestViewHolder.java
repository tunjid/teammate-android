package com.mainstreetcode.teammates.adapters.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.User;
import com.squareup.picasso.Picasso;

public class EventGuestViewHolder extends UserHoldingViewHolder {

    public EventGuestViewHolder(View itemView) {
        super(itemView, null);

    }

    public void bind(User user, boolean attending) {
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

}
