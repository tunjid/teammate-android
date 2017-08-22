package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.User;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for {@link User}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class UserAdapter extends BaseRecyclerViewAdapter<UserAdapter.UserViewHolder, UserAdapter.UserAdapterListener> {

    private final List<User> users;

    public UserAdapter(List<User> users, UserAdapterListener listener) {
        super(listener);
        this.users = users;
        setHasStableIds(true);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_grid_item, viewGroup, false);
        return new UserViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(UserViewHolder viewHolder, int i) {
        viewHolder.bind(users.get(i));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public long getItemId(int position) {
        return users.get(position).hashCode();
    }

    public interface UserAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onUserClicked(User item);
    }

    static class UserViewHolder extends BaseViewHolder<UserAdapterListener>
            implements View.OnClickListener {

        private User item;
        private TextView teamName;
        private TextView teamLocation;

        UserViewHolder(View itemView, UserAdapterListener adapterListener) {
            super(itemView, adapterListener);
            teamName = itemView.findViewById(R.id.item_title);
            teamLocation = itemView.findViewById(R.id.item_subtitle);
            itemView.setOnClickListener(this);
        }

        void bind(User item) {
            this.item = item;
            teamName.setText(item.getFirstName());
            //teamLocation.setText(item.getCity());
        }

        @Override
        public void onClick(View view) {
            adapterListener.onUserClicked(item);
        }
    }
}
