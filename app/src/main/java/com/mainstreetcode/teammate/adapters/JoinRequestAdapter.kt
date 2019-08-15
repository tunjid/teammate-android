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
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.or
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.Item.Companion.ALL_INPUT_VALID
import com.mainstreetcode.teammate.model.Item.Companion.FALSE
import com.mainstreetcode.teammate.util.ViewHolderUtil.Companion.ITEM
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Adapter for [JoinRequestAdapter]
 */

class JoinRequestAdapter(
        private val items: List<Differentiable>,
        listener: AdapterListener
) : BaseAdapter<InputViewHolder<JoinRequestAdapter.AdapterListener>, JoinRequestAdapter.AdapterListener>(listener) {

    private val chooser: TextInputStyle.InputChooser

    init {
        chooser = Chooser(adapterListener)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): InputViewHolder<AdapterListener> =
            InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup))

    @Suppress("UNCHECKED_CAST")
    override fun <S : InteractiveAdapter.AdapterListener> updateListener(viewHolder: BaseViewHolder<S>): S =
            adapterListener as S

    override fun onBindViewHolder(holder: InputViewHolder<AdapterListener>, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = items[position]
        if (item is Item<*>) holder.bind(chooser[item])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = ITEM

    interface AdapterListener : ImageWorkerFragment.ImagePickerListener {
        fun canEditFields(): Boolean

        fun canEditRole(): Boolean
    }

    internal class Chooser internal constructor(private val adapterListener: AdapterListener) : TextInputStyle.InputChooser() {

        override fun invoke(item: Item<*>): TextInputStyle {
            when (val itemType = item.itemType) {
                Item.SPORT -> return SpinnerTextInputStyle(
                        R.string.choose_sport,
                        Config.getSports(),
                        { it.name },
                        { it.code },
                        FALSE,
                        ALL_INPUT_VALID)
                Item.ROLE -> return SpinnerTextInputStyle(
                        R.string.choose_role,
                        Config.getPositions(),
                        { it.name },
                        { it.code },
                        { adapterListener.canEditRole() },
                        ALL_INPUT_VALID)
                else -> return TextInputStyle(
                        Item.NO_CLICK,
                        Item.NO_CLICK,
                        or(itemType == Item.INPUT, { adapterListener.canEditFields() }, FALSE),
                        ALL_INPUT_VALID,
                        Item.NO_ICON)
            }
        }
    }
}
