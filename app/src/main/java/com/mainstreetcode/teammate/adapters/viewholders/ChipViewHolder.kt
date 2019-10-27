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

import com.google.android.material.chip.Chip
import android.view.View

import com.mainstreetcode.teammate.adapters.StatTypeAdapter
import com.mainstreetcode.teammate.model.enums.StatAttribute
import com.tunjid.androidx.recyclerview.InteractiveViewHolder

class ChipViewHolder(itemView: View, listener: StatTypeAdapter.AdapterListener) : InteractiveViewHolder<StatTypeAdapter.AdapterListener>(itemView, listener) {
    private val chip: Chip = itemView as Chip
    private lateinit var attribute: StatAttribute

    init {
        chip.setOnClickListener { delegate?.onAttributeTapped(attribute) }
    }

    fun bind(attribute: StatAttribute) {
        this.attribute = attribute
        chip.text = attribute.name
        chip.isEnabled = delegate?.isEnabled == true
        chip.isChecked = delegate?.isSelected(attribute) == true
    }
}
