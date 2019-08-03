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
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.ListState
import com.mainstreetcode.teammate.util.ViewHolderUtil
import com.tunjid.androidbootstrap.recyclerview.ListPlaceholder

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

class EmptyViewHolder(itemView: View, @DrawableRes iconRes: Int, @StringRes stringRes: Int) : ListPlaceholder<ListState> {

    private val text: TextView = itemView.findViewById(R.id.item_title)
    private val icon: ImageView = itemView.findViewById(R.id.icon)

    @ColorInt
    private var color: Int = 0

    @IntDef(R.attr.empty_view_holder_tint, R.attr.alt_empty_view_holder_tint)
    @Retention(RetentionPolicy.SOURCE)
    annotation class EmptyTint

    init {
        color = ViewHolderUtil.resolveThemeColor(itemView.context, R.attr.empty_view_holder_tint)

        update(iconRes, stringRes)
    }

    override fun toggle(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        icon.visibility = visibility
        text.visibility = visibility
    }

    override fun bind(data: ListState) {
        update(data.imageRes, data.textRes)
    }

    fun setColor(@EmptyTint @AttrRes attrRes: Int) {
        this.color = ViewHolderUtil.resolveThemeColor(icon.context, attrRes)
        text.setTextColor(color)
        icon.setImageDrawable(getDrawable(icon.drawable))
    }

    private fun update(@DrawableRes iconRes: Int, @StringRes stringRes: Int) {
        text.setText(stringRes)
        icon.setImageDrawable(getIcon(iconRes))
    }

    private fun getIcon(@DrawableRes iconRes: Int): Drawable? {
        val context = icon.context
        val original = VectorDrawableCompat.create(context.resources, iconRes, context.theme)

        return getDrawable(original)
    }

    private fun getDrawable(original: Drawable?): Drawable? {
        if (original == null) return null
        var mutated: Drawable = original
        if (color != R.color.white) mutated = original.mutate()

        val wrapped = DrawableCompat.wrap(mutated)
        DrawableCompat.setTint(wrapped, color)

        return wrapped
    }

}
