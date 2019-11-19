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
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.ImageMediaViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.MediaViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.VideoMediaViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.bind
import com.mainstreetcode.teammate.model.Ad
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.util.CONTENT_AD
import com.mainstreetcode.teammate.util.INSTALL_AD
import com.mainstreetcode.teammate.util.MEDIA_IMAGE
import com.mainstreetcode.teammate.util.MEDIA_VIDEO
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

/**
 * Adapter for [Media]
 */

fun mediaAdapter(
        modelSource: () -> List<Differentiable>,
        listener: MediaAdapterListener
): RecyclerView.Adapter<RecyclerView.ViewHolder> = adapterOf(
        itemsSource = modelSource,
        viewHolderCreator = { viewGroup: ViewGroup, viewType: Int ->
            when (viewType) {
                CONTENT_AD -> ContentAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_content_ad), listener)
                INSTALL_AD -> InstallAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_install_ad), listener)
                MEDIA_IMAGE -> ImageMediaViewHolder(viewGroup.inflate(R.layout.viewholder_image), listener)
                else -> VideoMediaViewHolder(viewGroup.inflate(R.layout.viewholder_video), listener)
            }
        },
        viewHolderBinder = { holder, item, _ ->
            when {
                item is Ad<*> && holder is AdViewHolder<*> -> holder.bind(item)
                item is Media && holder is MediaViewHolder<*> -> holder.bind(item)
            }
        },
        itemIdFunction = { it.hashCode().toLong() },
        viewTypeFunction = { if (it is Media) if (it.isImage) MEDIA_IMAGE else MEDIA_VIDEO else (it as Ad<*>).type }
)

interface MediaAdapterListener {

    val isFullScreen: Boolean

    fun onFillLoaded() {}

    fun onMediaClicked(item: Media)

    fun onMediaLongClicked(media: Media): Boolean

    fun isSelected(media: Media): Boolean
}