package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.TeamChatViewHolder;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.model.User;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamChatAdapter extends BaseRecyclerViewAdapter<TeamChatViewHolder, BaseRecyclerViewAdapter.AdapterListener> {
    private final TeamChatRoom teamChatRoom;
    private final User signedInUser;

    public TeamChatAdapter(TeamChatRoom teamChatRoom, User signedInUser) {
        this.teamChatRoom = teamChatRoom;
        this.signedInUser = signedInUser;
    }

    @Override
    public TeamChatViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        @LayoutRes int layoutRes = R.layout.viewholder_chat;
        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        return new TeamChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TeamChatViewHolder viewHolder, int i) {
        List<TeamChat> chats = teamChatRoom.getChats();
        int size = chats.size();

        TeamChat teamChat = chats.get(i);
        TeamChat next = i < size - 1 ? chats.get(i + 1): null;

        User chatUser = teamChat.getUser();
        boolean hideDetails = (next != null && chatUser.equals(next.getUser()));

        viewHolder.bind(teamChat, signedInUser.equals(teamChat.getUser()), !hideDetails);
    }

    @Override
    public int getItemCount() {
        return teamChatRoom.getChats().size();
    }

}
