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

package com.mainstreetcode.teammate.adapters.viewholders.input

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.StatTypeAdapterListener
import com.mainstreetcode.teammate.adapters.statTypeAdapter
import com.mainstreetcode.teammate.adapters.viewholders.ChipViewHolder
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.model.enums.StatAttribute
import com.mainstreetcode.teammate.model.enums.StatType
import com.mainstreetcode.teammate.model.enums.StatTypes

class StatAttributeViewHolder(itemView: View, stat: Stat) : InputViewHolder(itemView) {

    private lateinit var adapter: RecyclerView.Adapter<ChipViewHolder>
    private val statType = StatType.empty()
    private val statTypes: StatTypes = stat.sport.stats

    private val recyclerView: RecyclerView

    override val hintLateralTranslation: Float
        get() = super.hintLateralTranslation

    override val hintLongitudinalTranslation: Float
        get() = -((itemView.height - hint.height - recyclerView.height) * 0.5f)

    init {
        adapter = statTypeAdapter(object : StatTypeAdapterListener {
            override val attributes: List<StatAttribute>
                get() = if (isEnabled) statType.attributes else stat.attributes

            override val isEnabled: Boolean
                get() = textInputStyle?.isEditable ?: false

            override fun isSelected(attribute: StatAttribute): Boolean = stat.contains(attribute)

            override fun onAttributeTapped(attribute: StatAttribute) {
                if (!isEnabled) return
                stat.compoundAttribute(attribute)
                adapter.notifyDataSetChanged()
            }
        })

        val layoutManager = FlexboxLayoutManager(itemView.context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.CENTER

        recyclerView = itemView.findViewById(R.id.inner_list)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    override fun bind(inputStyle: TextInputStyle) {
        super.bind(inputStyle)
        statType.update(statTypes.fromCodeOrFirst(inputStyle.item.rawValue.toString()))
        adapter.notifyDataSetChanged()
    }

    override fun updateText(text: CharSequence) {
        super.updateText(text)
        val value = textInputStyle?.item?.rawValue ?: return
        var current: StatType? = null

        for (type in statTypes)
            if (type.code == value) {
                current = type
                break
            }
        if (current == null) return
        statType.update(current)
        adapter.notifyDataSetChanged()
    }
}
