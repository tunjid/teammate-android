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

package com.mainstreetcode.teammate.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.TeamChatViewHolder
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.User
 import com.mainstreetcode.teammate.util.CHAT
import com.mainstreetcode.teammate.util.CONTENT_AD
import com.mainstreetcode.teammate.util.areDifferentDays
import com.tunjid.androidx.recyclerview.InteractiveAdapter
import com.tunjid.androidx.recyclerview.diff.Differentiable

/**
 * Adapter for [Chat]
 */

class TeamChatAdapter(
        private val items: () -> List<Differentiable>, private val signedInUser: User,
        listener: ChatAdapterListener
) : InteractiveAdapter<TeamChatViewHolder, TeamChatAdapter.ChatAdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TeamChatViewHolder {
        val context = viewGroup.context
        @LayoutRes val layoutRes = R.layout.viewholder_chat
        val itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false)

        return TeamChatViewHolder(itemView, delegate)
    }

    override fun onBindViewHolder(viewHolder: TeamChatViewHolder, i: Int) {
        val list = items()
        val size = list.size

        val chat = forceCast(list[i])
        val prev = if (i == 0) null else forceCast(list[i - 1])
        val next = if (i < size - 1) forceCast(list[i + 1]) else null

        val chatUser = chat.user
        val created = chat.created

        val hideDetails = next != null && chatUser == next.user
        val showPicture = signedInUser != chatUser && (prev == null || chatUser != prev.user)
        val isFirstMessageToday = DateUtils.isToday(created.time) && areDifferentDays(prev?.created, created)

        viewHolder.bind(chat, signedInUser == chat.user, !hideDetails, showPicture, isFirstMessageToday)
    }

    override fun getItemCount(): Int = items().size

    override fun getItemId(position: Int): Long = items()[position].hashCode().toLong()

    override fun getItemViewType(position: Int): Int =
            if (items()[position] is Chat) CHAT else CONTENT_AD

    interface ChatAdapterListener {
        fun onChatClicked(chat: Chat)
    }

    private fun forceCast(identifiable: Differentiable): Chat = identifiable as Chat
}
