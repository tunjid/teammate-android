package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.BlockedUserAdapter;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.User;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

public class BlockedUserViewHolder extends ModelCardViewHolder<BlockedUser, BlockedUserAdapter.UserAdapterListener>
        implements View.OnClickListener {

    public BlockedUserViewHolder(View itemView, BlockedUserAdapter.UserAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(BlockedUser model) {
        super.bind(model);

        User user = model.getUser();

        title.setText(user.getFirstName());
        subtitle.setText(model.getReason().getName());

        setTransitionName(itemView, getTransitionName(model, R.id.fragment_header_background));
        setTransitionName(thumbnail, getTransitionName(model, R.id.fragment_header_thumbnail));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onBlockedUserClicked(model);
    }
}
