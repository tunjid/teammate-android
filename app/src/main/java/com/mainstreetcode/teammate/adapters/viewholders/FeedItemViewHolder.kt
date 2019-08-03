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

import android.view.View
import androidx.core.view.ViewCompat.setTransitionName
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.FeedAdapter
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.RemoteImage
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.notifications.FeedItem
import com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName

/**
 * Viewholder for a [Team]
 */
class FeedItemViewHolder(
        itemView: View,
        listener: FeedAdapter.FeedItemAdapterListener
) : ModelCardViewHolder<RemoteImage, FeedAdapter.FeedItemAdapterListener>(itemView, listener), View.OnClickListener {

    private lateinit var item: FeedItem<*>

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(item: FeedItem<*>) {
        this.item = item
        bind(item.model)

        title.text = item.title
        subtitle.text = item.body
    }

    override fun bind(model: RemoteImage) {
        super.bind(model)
        if (model is Media) {
            setTransitionName(itemView, getTransitionName(model, R.id.fragment_media_background))
            setTransitionName(thumbnail, getTransitionName(model, R.id.fragment_media_thumbnail))
        } else if (model is Event || model is JoinRequest) {
            setTransitionName(itemView, getTransitionName(item, R.id.fragment_header_background))
            setTransitionName(thumbnail, getTransitionName(item, R.id.fragment_header_thumbnail))
        }
    }

    override fun onClick(view: View) {
        adapterListener.onFeedItemClicked(item)
    }
}