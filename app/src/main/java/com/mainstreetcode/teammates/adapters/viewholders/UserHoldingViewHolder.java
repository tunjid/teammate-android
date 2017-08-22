package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;


public class UserHoldingViewHolder<T extends BaseRecyclerViewAdapter.AdapterListener> extends BaseViewHolder<T> {

    TextView userName;
    TextView userStatus;
    ImageView userPicture;

    UserHoldingViewHolder(View itemView, T adapterListener) {
        super(itemView, adapterListener);
        userName = itemView.findViewById(R.id.item_title);
        userStatus = itemView.findViewById(R.id.item_subtitle);
        userPicture = itemView.findViewById(R.id.thumbnail);
    }
}
