package com.mainstreetcode.teammate.adapters;

import android.content.Context;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.TeamChatViewHolder;
import com.mainstreetcode.teammate.model.Chat;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.User;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.CHAT;

/**
 * Adapter for {@link Chat}
 */

public class TeamChatAdapter extends InteractiveAdapter<TeamChatViewHolder, TeamChatAdapter.ChatAdapterListener> {
    private final List<Differentiable> items;
    private final User signedInUser;

    public TeamChatAdapter(List<Differentiable> items, User signedInUser,
                           TeamChatAdapter.ChatAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.items = items;
        this.signedInUser = signedInUser;
    }

    @NonNull
    @Override
    public TeamChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        @LayoutRes int layoutRes = R.layout.viewholder_chat;
        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        return new TeamChatViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamChatViewHolder viewHolder, int i) {
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
    public void onViewRecycled(@NonNull TeamChatViewHolder holder) {
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
        return items.get(position) instanceof Chat ? CHAT : CONTENT_AD;
    }

    public interface ChatAdapterListener extends InteractiveAdapter.AdapterListener {
        void onChatClicked(Chat chat);
    }

    private Chat forceCast(Differentiable identifiable) {
        return (Chat) identifiable;
    }
}
