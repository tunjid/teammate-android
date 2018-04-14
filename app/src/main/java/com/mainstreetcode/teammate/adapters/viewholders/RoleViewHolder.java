package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.User;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

public class RoleViewHolder extends ModelCardViewHolder<Role, TeamDetailAdapter.UserAdapterListener>
        implements View.OnClickListener {

    public RoleViewHolder(View itemView, TeamDetailAdapter.UserAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(Role model) {
        super.bind(model);

        User user = model.getUser();

        title.setText(user.getFirstName());
        subtitle.setText(model.getPosition().getName());

        setTransitionName(itemView, getTransitionName(model, R.id.fragment_header_background));
        setTransitionName(thumbnail, getTransitionName(model, R.id.fragment_header_thumbnail));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onRoleClicked(model);
    }
}
