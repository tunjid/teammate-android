package com.mainstreetcode.teammates.adapters.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.User;
import com.squareup.picasso.Picasso;

public class JoinRequestViewHolder extends UserHoldingViewHolder<TeamDetailAdapter.UserAdapterListener>
        implements View.OnClickListener {

    private JoinRequest request;

    public JoinRequestViewHolder(View itemView, TeamDetailAdapter.UserAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

   public void bind(JoinRequest request) {
        this.request = request;
        User item = request.getUser();
        Context context = itemView.getContext();

        userName.setText(item.getFirstName());
        userStatus.setText(request.isTeamApproved() && !request.isUserApproved()
                ? context.getString(R.string.user_invited, request.getRoleName())
                : context.getString(R.string.user_requests_join, request.getRoleName()));

        String imageUrl = item.getImageUrl() != null ? item.getImageUrl() : "";

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
        adapterListener.onJoinRequestClicked(request);
    }
}
