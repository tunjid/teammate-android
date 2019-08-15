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

import android.view.ViewGroup
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.FeedItemViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.bind
import com.mainstreetcode.teammate.model.Ad
import com.mainstreetcode.teammate.notifications.FeedItem
import com.mainstreetcode.teammate.util.ViewHolderUtil.Companion.CONTENT_AD
import com.mainstreetcode.teammate.util.ViewHolderUtil.Companion.FEED_ITEM
import com.mainstreetcode.teammate.util.ViewHolderUtil.Companion.INSTALL_AD
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Adapter for [FeedItem]
 */

class FeedAdapter(
        private val items: List<Differentiable>,
        listener: FeedItemAdapterListener
) : InteractiveAdapter<InteractiveViewHolder<*>, FeedAdapter.FeedItemAdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): InteractiveViewHolder<*> = when (viewType) {
        CONTENT_AD -> ContentAdViewHolder(getItemView(R.layout.viewholder_list_content_ad, viewGroup), adapterListener)
        INSTALL_AD -> InstallAdViewHolder(getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
        else -> FeedItemViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(viewHolder: InteractiveViewHolder<*>, position: Int) {
        when (val item = items[position]) {
            is FeedItem<*> -> (viewHolder as FeedItemViewHolder).bind(item)
            is Ad<*> -> (viewHolder as AdViewHolder<*>).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is FeedItem<*> -> FEED_ITEM
        else -> (item as Ad<*>).type
    }

    interface FeedItemAdapterListener : AdapterListener {
        fun onFeedItemClicked(item: FeedItem<*>)
    }

}
