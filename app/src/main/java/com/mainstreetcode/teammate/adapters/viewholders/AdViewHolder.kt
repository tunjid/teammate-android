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

import android.text.TextUtils.isEmpty
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Ad
import com.mainstreetcode.teammate.model.ContentAd
import com.mainstreetcode.teammate.model.InstallAd
import com.mainstreetcode.teammate.model.Team
import com.squareup.picasso.Picasso
import com.tunjid.androidx.recyclerview.InteractiveViewHolder

/**
 * Viewholder for a [Team]
 */
abstract class AdViewHolder<T : Ad<*>> internal constructor(
        itemView: View,
        delegate: Any
) : InteractiveViewHolder<Any>(itemView, delegate) {

    internal lateinit var ad: T

    internal var title: TextView = itemView.findViewById(R.id.item_title)
    internal var subtitle: TextView = itemView.findViewById(R.id.item_subtitle)
    internal var thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)

    internal abstract val imageUrl: String?

    internal fun bindActual(ad: T) {
        this.ad = ad
        setImageAspectRatio(ad)
        thumbnail.doOnLayout {
            val imageUrl = imageUrl
            if (!isEmpty(imageUrl)) Picasso.get().load(imageUrl).fit().centerCrop().into(thumbnail)
        }
    }

    private fun setImageAspectRatio(ad: Ad<*>) {
        val aspectRatio = ad.imageAspectRatio
        if (aspectRatio != null) {
            (thumbnail.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = aspectRatio
        }
    }
}

fun AdViewHolder<*>.bind(ad: Ad<*>) {
    when (this) {
        is ContentAdViewHolder -> bindActual(ad as ContentAd)
        is InstallAdViewHolder -> bindActual(ad as InstallAd)
    }
}