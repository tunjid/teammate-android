package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ViewHolderUtil;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

public class UserViewHolder extends ModelCardViewHolder<User, ViewHolderUtil.SimpleAdapterListener<User>>
        implements View.OnClickListener {

    public UserViewHolder(View itemView, ViewHolderUtil.SimpleAdapterListener<User> adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(User model) {
        super.bind(model);

        title.setText(model.getFirstName());
        subtitle.setText(model.getLastName());

        setTransitionName(itemView, getTransitionName(model, R.id.fragment_header_background));
        setTransitionName(thumbnail, getTransitionName(model, R.id.fragment_header_thumbnail));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onItemClicked(model);
    }
}
