package com.mainstreetcode.teammates.adapters.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.mainstreetcode.teammates.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.User;
import com.squareup.picasso.Picasso;

public class RoleViewHolder extends UserHoldingViewHolder
        implements View.OnClickListener {

    private Role role;

    public RoleViewHolder(View itemView, TeamDetailAdapter.UserAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(Role role) {
        this.role = role;
        User user = role.getUser();
        Context context = itemView.getContext();

        userName.setText(user.getFirstName());
        userStatus.setText(role.getName());

        String imageUrl = role.getImageUrl() != null ? role.getImageUrl() : "";

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
        adapterListener.onRoleClicked(role);
    }
}
