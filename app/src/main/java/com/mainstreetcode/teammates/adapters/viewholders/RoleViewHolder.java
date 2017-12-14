package com.mainstreetcode.teammates.adapters.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.User;
import com.squareup.picasso.Picasso;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;

public class RoleViewHolder extends UserHoldingViewHolder<TeamDetailAdapter.UserAdapterListener>
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

        title.setText(user.getFirstName());
        subtitle.setText(role.getName());

        String imageUrl = role.getImageUrl() != null ? role.getImageUrl() : "";

        setTransitionName(itemView, getTransitionName(role, R.id.fragment_header_background));
        setTransitionName(thumbnail, getTransitionName(role, R.id.fragment_header_thumbnail));

        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(context)
                    .load(imageUrl)
                    .fit()
                    .centerInside()
                    .into(thumbnail);
        }
    }

    @Override
    public void onClick(View view) {
        adapterListener.onRoleClicked(role);
    }
}
