package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.TeamChatViewHolder;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.User;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.AD;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.CHAT;

/**
 * Adapter for {@link Chat}
 */

public class TeamChatAdapter extends BaseRecyclerViewAdapter<TeamChatViewHolder, TeamChatAdapter.ChatAdapterListener> {
    private final List<Identifiable> items;
    private final User signedInUser;

    public TeamChatAdapter(List<Identifiable> items, User signedInUser,
                           TeamChatAdapter.ChatAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.items = items;
        this.signedInUser = signedInUser;
    }

    @Override
    public TeamChatViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        @LayoutRes int layoutRes = R.layout.viewholder_chat;
        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        return new TeamChatViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(TeamChatViewHolder viewHolder, int i) {
        int size = items.size();

        Chat chat = forceCast(items.get(i));
        Chat prev = i == 0 ? null : forceCast(items.get(i - 1));
        Chat next = i < size - 1 ? forceCast(items.get(i + 1)) : null;

        User chatUser = chat.getUser();
        boolean hideDetails = (next != null && chatUser.equals(next.getUser()));
        boolean showPicture = !signedInUser.equals(chatUser) && (prev == null || !chatUser.equals(prev.getUser()));

        viewHolder.bind(chat, signedInUser.equals(chat.getUser()), !hideDetails, showPicture);
    }

    @Override
    public void onViewRecycled(TeamChatViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof Chat ? CHAT : AD;
    }

    public interface ChatAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onChatClicked(Chat chat);
    }

    private Chat forceCast(Identifiable identifiable) {
        return (Chat) identifiable;
    }
}
