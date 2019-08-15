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

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.model.RemoteImage
import com.mainstreetcode.teammate.util.ViewHolderUtil
import com.mainstreetcode.teammate.util.ViewHolderUtil.Companion.THUMBNAIL_SIZE
import com.squareup.picasso.Picasso
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter


open class ModelCardViewHolder<H : RemoteImage, T : InteractiveAdapter.AdapterListener> internal constructor(
        itemView: View,
        adapterListener: T
) : BaseViewHolder<T>(itemView, adapterListener) {

    protected lateinit var model: H

    internal var title: TextView = itemView.findViewById(R.id.item_title)
    internal var subtitle: TextView = itemView.findViewById(R.id.item_subtitle)
    var thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)

    open val isThumbnail: Boolean
        get() = true

    init {
        ViewHolderUtil.updateForegroundDrawable(itemView)
        @Suppress("LeakingThis")
        if (isThumbnail) thumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    open fun bind(model: H) {
        this.model = model
        val imageUrl = model.imageUrl

        if (TextUtils.isEmpty(imageUrl)) thumbnail.setImageResource(R.color.dark_grey)
        else load(imageUrl, thumbnail)
    }

    fun withTitle(@StringRes titleRes: Int): ModelCardViewHolder<H, T> {
        title.setText(titleRes)
        return this
    }

    fun withSubTitle(@StringRes subTitleRes: Int): ModelCardViewHolder<H, T> {
        subtitle.setText(subTitleRes)
        return this
    }

    internal fun setTitle(title: CharSequence) {
        if (!TextUtils.isEmpty(title)) this.title.text = title
    }

    internal fun setSubTitle(subTitle: CharSequence) {
        if (!TextUtils.isEmpty(subTitle)) this.subtitle.text = subTitle
    }

    internal fun load(imageUrl: String, destination: ImageView) {
        val creator = Picasso.get().load(imageUrl)

        if (!isThumbnail) creator.fit().centerCrop()
        else creator.placeholder(R.drawable.bg_image_placeholder)
                    .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .centerInside()

        creator.into(destination)
    }
}
