package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.User;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link User}
 */

public class UserAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, ImageWorkerFragment.ImagePickerListener> {

    private final User user;

    public UserAdapter(User user, ImageWorkerFragment.ImagePickerListener listener) {
        super(listener);
        this.user = user;
    }

    @Override
    public BaseItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_simple_input, viewGroup, false);

        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), true);
            default:
                return new BaseItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseItemViewHolder viewHolder, int i) {
        viewHolder.bind(user.get(i));
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    @Override
    public int getItemViewType(int position) {
        return user.get(position).getItemType();
    }

    @Override
    public long getItemId(int position) {
        return user.get(position).hashCode();
    }

}
