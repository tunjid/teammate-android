package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.model.User;

import static androidx.core.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

public class UserViewHolder extends ModelCardViewHolder<User, UserAdapter.AdapterListener>
        implements View.OnClickListener {

    public UserViewHolder(View itemView, UserAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(User model) {
        super.bind(model);

        setTitle(model.getFirstName());
        setSubTitle(model.getLastName());

        setTransitionName(itemView, getTransitionName(model, R.id.fragment_header_background));
        setTransitionName(thumbnail, getTransitionName(model, R.id.fragment_header_thumbnail));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onUserClicked(model);
    }
}
