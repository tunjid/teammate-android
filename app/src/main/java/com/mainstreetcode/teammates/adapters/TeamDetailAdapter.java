package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamDetailAdapter extends BaseRecyclerViewAdapter<TeamDetailAdapter.UserHoldingViewHolder, TeamDetailAdapter.UserAdapterListener> {
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
                ? new UserViewHolder(itemView, adapterListener)
                : new RequestViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(UserHoldingViewHolder baseTeamViewHolder, int i) {
        List<Role> roles = team.getRoles();
        List<JoinRequest> joinRequests = team.getJoinRequests();
        int joinedUsersSize = roles.size();
        boolean isJoinedUser = i < joinedUsersSize;

        int index = isJoinedUser ? i : i - joinedUsersSize;
        if (isJoinedUser) ((UserViewHolder) baseTeamViewHolder).bind(roles.get(index));
        else ((RequestViewHolder) baseTeamViewHolder).bind(joinRequests.get(index));
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

    static class UserHoldingViewHolder extends BaseViewHolder<UserAdapterListener> {

        TextView userName;
        TextView userStatus;
        ImageView userPicture;

        UserHoldingViewHolder(View itemView, UserAdapterListener adapterListener) {
            super(itemView, adapterListener);
            userName = itemView.findViewById(R.id.user_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userPicture = itemView.findViewById(R.id.thumbnail);
        }
    }

    static class UserViewHolder extends UserHoldingViewHolder
            implements View.OnClickListener {

        private Role role;

        UserViewHolder(View itemView, UserAdapterListener adapterListener) {
            super(itemView, adapterListener);
            itemView.setOnClickListener(this);
        }

        void bind(Role role) {
            this.role = role;
            User item = role.getUser();
            Context context = itemView.getContext();

            userName.setText(item.getFirstName());
//            userStatus.setText(item.isTeamApproved() && !item.isUserApproved()
//                    ? context.getString(R.string.user_invited)
//                    : !item.isTeamApproved() && item.isUserApproved()
//                    ? context.getString(R.string.user_requests_join)
//                    : item.getRoleName());

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

    static class RequestViewHolder extends UserHoldingViewHolder
            implements View.OnClickListener {

        private JoinRequest request;

        RequestViewHolder(View itemView, UserAdapterListener adapterListener) {
            super(itemView, adapterListener);
            itemView.setOnClickListener(this);
        }

        void bind(JoinRequest request) {
            this.request = request;
            User item = request.getUser();
            Context context = itemView.getContext();

            userName.setText(item.getFirstName());
            userStatus.setText(request.isTeamApproved() && !request.isUserApproved()
                    ? context.getString(R.string.user_invited)
                    : context.getString(R.string.user_requests_join));

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
}
