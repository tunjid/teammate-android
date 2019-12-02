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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.TeamChatViewHolder
import com.mainstreetcode.teammate.model.Ad
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.CHAT
import com.mainstreetcode.teammate.util.areDifferentDays
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

/**
 * Adapter for [Chat]
 */

fun chatAdapter(
        modelSource: () -> List<Differentiable>,
        signedInUser: User,
        listener: (Chat) -> Unit
): RecyclerView.Adapter<TeamChatViewHolder> = adapterOf(
        itemsSource = modelSource,
        viewHolderCreator = { viewGroup: ViewGroup, _: Int ->
            TeamChatViewHolder(viewGroup.inflate(R.layout.viewholder_chat), listener)
        },
        viewHolderBinder = { holder, item, i ->
            val list = modelSource()
            val size = list.size

            val chat = forceCast(item)
            val prev = if (i == 0) null else forceCast(list[i - 1])
            val next = if (i < size - 1) forceCast(list[i + 1]) else null

            val chatUser = chat.user
            val created = chat.created

            val hideDetails = next != null && chatUser == next.user
            val showPicture = signedInUser != chatUser && (prev == null || chatUser != prev.user)
            val isFirstMessageToday = DateUtils.isToday(created.time) && areDifferentDays(prev?.created, created)

            holder.bind(chat, signedInUser == chat.user, !hideDetails, showPicture, isFirstMessageToday)
        },
        itemIdFunction = { it.hashCode().toLong() },
        viewTypeFunction = { (it as? Ad<*>)?.type ?: CHAT }
)

private fun forceCast(identifiable: Differentiable): Chat = identifiable as Chat