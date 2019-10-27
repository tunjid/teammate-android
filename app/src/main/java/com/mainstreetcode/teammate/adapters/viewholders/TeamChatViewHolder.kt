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

package com.mainstreetcode.teammate.adapters.viewholders

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamChatAdapter
import com.mainstreetcode.teammate.model.Chat
import com.squareup.picasso.Picasso
import com.tunjid.androidx.recyclerview.InteractiveViewHolder
import com.tunjid.androidx.view.util.marginLayoutParams

/**
 * Viewholder for a [Chat]
 */
class TeamChatViewHolder(itemView: View, listener: TeamChatAdapter.ChatAdapterListener) : InteractiveViewHolder<TeamChatAdapter.ChatAdapterListener>(itemView, listener) {

    private lateinit var item: Chat

    private val space: View = itemView.findViewById(R.id.mid_guide)
    private val image: ImageView = itemView.findViewById(R.id.image)
    private val today: TextView = itemView.findViewById(R.id.today)
    private val content: TextView = itemView.findViewById(R.id.content)
    private val details: TextView = itemView.findViewById(R.id.details)

    init {
        content.setOnClickListener { delegate?.onChatClicked(item) }
    }

    fun bind(item: Chat, isSignedInUser: Boolean, showDetails: Boolean, showPicture: Boolean,
             isFirstMessageToday: Boolean) {

        this.item = item
        val context = itemView.context

        content.text = item.content
        itemView.alpha = if (item.isEmpty) 0.6f else 1f
        image.visibility = if (showPicture) View.VISIBLE else View.GONE
        space.visibility = if (showPicture) View.VISIBLE else View.GONE
        today.visibility = if (isFirstMessageToday) View.VISIBLE else View.GONE
        details.visibility = if (showDetails || item.isEmpty) View.VISIBLE else View.GONE
        content.setBackgroundResource(if (isSignedInUser) R.drawable.bg_chat_box else R.drawable.bg_chat_box_alt)

        if (!isSignedInUser && !TextUtils.isEmpty(item.imageUrl))
            Picasso.get().load(item.imageUrl).fit().centerCrop().into(image)

        if (item.isEmpty)
            details.setText(R.string.chat_sending)
        else if (showDetails) details.text = getDetailsText(item, isSignedInUser, context)

        if (isFirstMessageToday)
            content.marginLayoutParams.topMargin = context.resources.getDimensionPixelSize(R.dimen.single_margin)

        val detailsParams = details.layoutParams as ConstraintLayout.LayoutParams
        val leftParams = image.layoutParams as ConstraintLayout.LayoutParams

        detailsParams.horizontalBias = if (isSignedInUser) 1f else 0f
        leftParams.horizontalBias = detailsParams.horizontalBias
    }

    private fun getDetailsText(item: Chat, isSignedInUser: Boolean, context: Context): CharSequence {
        val date = item.createdDate
        val firstName = item.user.firstName
        return if (isSignedInUser) date else context.getString(R.string.chat_details, firstName, date)
    }
}
