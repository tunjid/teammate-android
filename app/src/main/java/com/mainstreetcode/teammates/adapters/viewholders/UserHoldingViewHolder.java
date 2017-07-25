package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamDetailAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;


public class UserHoldingViewHolder extends BaseViewHolder<TeamDetailAdapter.UserAdapterListener> {

    TextView userName;
    TextView userStatus;
    ImageView userPicture;

    UserHoldingViewHolder(View itemView, TeamDetailAdapter.UserAdapterListener adapterListener) {
        super(itemView, adapterListener);
        userName = itemView.findViewById(R.id.user_name);
        userStatus = itemView.findViewById(R.id.user_status);
        userPicture = itemView.findViewById(R.id.thumbnail);
    }
}
