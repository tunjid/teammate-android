package com.mainstreetcode.teammate.adapters.viewholders;

import android.content.Context;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.User;

public class JoinRequestViewHolder extends ModelCardViewHolder<JoinRequest, TeamDetailAdapter.UserAdapterListener>
        implements View.OnClickListener {

    public JoinRequestViewHolder(View itemView, TeamDetailAdapter.UserAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    @Override
    public void bind(JoinRequest model) {
        super.bind(model);

        User item = model.getUser();
        Context context = itemView.getContext();

        title.setText(item.getFirstName());
        subtitle.setText(model.isTeamApproved() && !model.isUserApproved()
                ? context.getString(R.string.user_invited, model.getRoleName())
                : context.getString(R.string.user_requests_join, model.getRoleName()));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onJoinRequestClicked(model);
    }
}
