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
import com.mainstreetcode.teammate.adapters.viewholders.ChipViewHolder
import com.mainstreetcode.teammate.model.enums.StatAttribute
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.view.util.inflate

fun statTypeAdapter(listener: StatTypeAdapterListener): RecyclerView.Adapter<ChipViewHolder> = adapterOf(
        itemsSource = { listener.attributes },
        viewHolderCreator = { viewGroup: ViewGroup, _: Int ->
            ChipViewHolder(viewGroup.inflate(R.layout.viewholder_stat_attribute), listener)
        },
        viewHolderBinder = { holder, item, _ -> holder.bind(item) }
)

interface StatTypeAdapterListener {

    val attributes: List<StatAttribute>

    val isEnabled: Boolean

    fun isSelected(attribute: StatAttribute): Boolean

    fun onAttributeTapped(attribute: StatAttribute)
}