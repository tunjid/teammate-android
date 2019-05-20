/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.TeamChatViewHolder;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.Date;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CHAT;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;

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
        Date created = chat.getCreated();

        boolean hideDetails = (next != null && chatUser.equals(next.getUser()));
        boolean showPicture = !signedInUser.equals(chatUser) && (prev == null || !chatUser.equals(prev.getUser()));
        boolean isFirstMessageToday = DateUtils.isToday(created.getTime()) && ModelUtils.areDifferentDays(prev == null ? null : prev.getCreated(), created);

        viewHolder.bind(chat, signedInUser.equals(chat.getUser()), !hideDetails, showPicture, isFirstMessageToday);
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
