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
import com.mainstreetcode.teammate.adapters.viewholders.ChipViewHolder
import com.mainstreetcode.teammate.model.enums.StatAttribute
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter

class StatTypeAdapter(adapterListener: AdapterListener) : InteractiveAdapter<ChipViewHolder, StatTypeAdapter.AdapterListener>(adapterListener) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ChipViewHolder =
            ChipViewHolder(getItemView(R.layout.viewholder_stat_attribute, viewGroup), adapterListener)

    override fun onBindViewHolder(chipViewHolder: ChipViewHolder, i: Int) {
        chipViewHolder.bind(adapterListener.attributes[i])
    }

    override fun getItemCount(): Int = adapterListener.attributes.size

    interface AdapterListener : InteractiveAdapter.AdapterListener {

        val attributes: List<StatAttribute>

        val isEnabled: Boolean

        fun isSelected(attribute: StatAttribute): Boolean

        fun onAttributeTapped(attribute: StatAttribute)
    }
}
