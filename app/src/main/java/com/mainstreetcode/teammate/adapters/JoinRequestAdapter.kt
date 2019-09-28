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
import com.mainstreetcode.teammate.model.enums.Position
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.neverEnabled
import com.mainstreetcode.teammate.model.noIcon
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.util.ITEM
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

/**
 * Adapter for [JoinRequestAdapter]
 */

class JoinRequestAdapter(
        private val items: List<Differentiable>,
        listener: AdapterListener
) : BaseAdapter<InputViewHolder, JoinRequestAdapter.AdapterListener>(listener) {

    private val chooser: TextInputStyle.InputChooser

    init {
        chooser = Chooser(delegate)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): InputViewHolder =
            InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))

    @Suppress("UNCHECKED_CAST")
    override fun <S : Any> updateListener(viewHolder: BaseViewHolder<S>): S =
            delegate as S

    override fun onBindViewHolder(holder: InputViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = items[position]
        if (item is Item) holder.bind(chooser[item])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = ITEM

    interface AdapterListener : ImageWorkerFragment.ImagePickerListener {
        fun canEditFields(): Boolean

        fun canEditRole(): Boolean
    }

    internal class Chooser internal constructor(private val delegate: AdapterListener) : TextInputStyle.InputChooser() {

        override fun invoke(item: Item): TextInputStyle {
            when (val itemType = item.itemType) {
                Item.SPORT -> return SpinnerTextInputStyle(
                        R.string.choose_sport,
                        Config.getSports(),
                        Sport::name,
                        Sport::code,
                        Item::neverEnabled,
                        Item::noInputValidation)
                Item.ROLE -> return SpinnerTextInputStyle(
                        R.string.choose_role,
                        Config.getPositions(),
                        Position::name,
                        Position::code,
                        { delegate.canEditRole() },
                        Item::noInputValidation)
                else -> return TextInputStyle(
                        Item.noClicks,
                        Item.noClicks,
                        or<(Item) -> Boolean>(itemType == Item.INPUT, { delegate.canEditFields() }, Item::neverEnabled),
                        Item::noInputValidation,
                        Item::noIcon)
            }
        }
    }
}
