package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.JoinRequestViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.UserHoldingViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleViewHolder;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamDetailAdapter extends BaseRecyclerViewAdapter<UserHoldingViewHolder, TeamDetailAdapter.UserAdapterListener> {
    private final Team team;

    public TeamDetailAdapter(Team team, UserAdapterListener listener) {
        super(listener);
        this.team = team;
    }

    @Override
    public UserHoldingViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        @LayoutRes int layoutRes = R.layout.viewholder_user_team;
        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        return viewType == R.id.viewholder_role
                ? new RoleViewHolder(itemView, adapterListener)
                : new JoinRequestViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(UserHoldingViewHolder baseTeamViewHolder, int i) {
        List<Role> roles = team.getRoles();
        List<JoinRequest> joinRequests = team.getJoinRequests();
        int joinedUsersSize = roles.size();
        boolean isJoinedUser = i < joinedUsersSize;

        int index = isJoinedUser ? i : i - joinedUsersSize;
        if (isJoinedUser) ((RoleViewHolder) baseTeamViewHolder).bind(roles.get(index));
        else ((JoinRequestViewHolder) baseTeamViewHolder).bind(joinRequests.get(index));
    }

    @Override
    public int getItemCount() {
        return team.getRoles().size() + team.getJoinRequests().size();
    }

    @Override
    public int getItemViewType(int position) {
        return position < team.getRoles().size() ? R.id.viewholder_role : R.id.viewholder_join_request;
    }

    public interface UserAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onRoleClicked(Role role);

        void onJoinRequestClicked(JoinRequest request);
    }

}
