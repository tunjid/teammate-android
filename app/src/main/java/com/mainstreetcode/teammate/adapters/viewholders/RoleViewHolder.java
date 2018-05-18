package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamMemberAdapter;
import com.mainstreetcode.teammate.model.Role;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

public class RoleViewHolder extends ModelCardViewHolder<Role, TeamMemberAdapter.UserAdapterListener>
        implements View.OnClickListener {

    public RoleViewHolder(View itemView, TeamMemberAdapter.UserAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(Role model) {
        super.bind(model);

        title.setText(model.getTitle());
        subtitle.setText(model.getPosition().getName());

        setTransitionName(itemView, getTransitionName(model, R.id.fragment_header_background));
        setTransitionName(thumbnail, getTransitionName(model, R.id.fragment_header_thumbnail));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onRoleClicked(model);
    }
}
