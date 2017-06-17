package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamDetailAdapter extends BaseRecyclerViewAdapter<TeamDetailAdapter.UserViewHolder, TeamDetailAdapter.UserAdapterListener> {

    private final Team team;

    public TeamDetailAdapter(Team team) {
        this.team = team;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        @LayoutRes int layoutRes = R.layout.viewholder_user_team;
        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        return new UserViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(UserViewHolder baseTeamViewHolder, int i) {
        List<User> users = team.getUsers();
        List<User> pendingUsers = team.getPendingUsers();
        int joinedUsersSize = users.size();
        boolean isJoinedUser = i < joinedUsersSize;

        int index = isJoinedUser ? i : i - joinedUsersSize;
        baseTeamViewHolder.bind(isJoinedUser ? users.get(index) : pendingUsers.get(index));
    }

    @Override
    public int getItemCount() {
        return team.getUsers().size() + team.getPendingUsers().size();
    }

    public interface UserAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onUserClicked(User user);
    }

    static class UserViewHolder extends BaseViewHolder<UserAdapterListener>
            implements View.OnClickListener {

        private User user;
        private TextView userName;
        private TextView userStatus;

        UserViewHolder(View itemView, UserAdapterListener adapterListener) {
            super(itemView, adapterListener);
            userName = itemView.findViewById(R.id.user_name);
            userStatus = itemView.findViewById(R.id.user_status);
            itemView.setOnClickListener(this);
        }

        void bind(User item) {
            this.user = item;
            Context context = itemView.getContext();

            userName.setText(item.getFirstName());
            userStatus.setText(!TextUtils.isEmpty(item.getRole())
                    ? item.getRole()
                    : item.isTeamApproved()
                    ? context.getString(R.string.user_invited)
                    : context.getString(R.string.user_requests_join));
        }

        @Override
        public void onClick(View view) {
            adapterListener.onUserClicked(user);
        }
    }
}
