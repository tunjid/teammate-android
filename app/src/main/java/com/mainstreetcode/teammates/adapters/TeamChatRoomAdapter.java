package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.TeamChatRoomViewHolder;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link com.mainstreetcode.teammates.model.TeamChatRoom}
 */

public class TeamChatRoomAdapter extends BaseRecyclerViewAdapter<TeamChatRoomViewHolder, TeamChatRoomAdapter.TeamChatRoomAdapterListener> {

    private final List<TeamChatRoom> items;

    public TeamChatRoomAdapter(List<TeamChatRoom> items, TeamChatRoomAdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @Override
    public TeamChatRoomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_team, viewGroup, false);
        return new TeamChatRoomViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(TeamChatRoomViewHolder viewHolder, int i) {
        viewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    public interface TeamChatRoomAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onChatRoomClicked(TeamChatRoom item);
    }

}
