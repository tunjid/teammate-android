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

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.SettingsAdapter
import com.mainstreetcode.teammate.model.SettingsItem
import com.mainstreetcode.teammate.util.ViewHolderUtil
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class SettingsViewHolder(
        itemView: View,
        adapterListener: SettingsAdapter.SettingsAdapterListener
) : InteractiveViewHolder<SettingsAdapter.SettingsAdapterListener>(itemView, adapterListener), View.OnClickListener {

    private lateinit var item: SettingsItem
    private val itemName: TextView = itemView.findViewById(R.id.item_name)

    internal val icon: Drawable?
        get() {
            val src = ContextCompat.getDrawable(itemView.context, item.drawableRes) ?: return null

            val wrapped = DrawableCompat.wrap(src)
            DrawableCompat.setTint(wrapped, ViewHolderUtil.resolveThemeColor(itemView.context, R.attr.alt_icon_tint))

            return wrapped
        }

    init {
        itemName.setOnClickListener(this)
    }

    fun bind(item: SettingsItem) {
        this.item = item
        itemName.setText(item.stringRes)
        itemName.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
    }

    override fun onClick(view: View) {
        adapterListener.onSettingsItemClicked(item)
    }
}
