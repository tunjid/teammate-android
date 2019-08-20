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
import com.mainstreetcode.teammate.model.Item.Companion.ZIP
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.never
import com.mainstreetcode.teammate.util.ITEM
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Adapter for [Team]
 */

class TeamEditAdapter(
        private val items: List<Differentiable>,
        listener: TeamEditAdapterListener
) : BaseAdapter<InputViewHolder<TeamEditAdapter.TeamEditAdapterListener>, TeamEditAdapter.TeamEditAdapterListener>(listener) {

    private val chooser: TextInputStyle.InputChooser

    init {
        chooser = Chooser(adapterListener)
    }// setHasStableIds(true); DO NOT PUT THIS BACK

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): InputViewHolder<TeamEditAdapterListener> =
            InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup))

    @Suppress("UNCHECKED_CAST")
    override fun <S : AdapterListener> updateListener(viewHolder: BaseViewHolder<S>): S =
            adapterListener as S

    override fun onBindViewHolder(holder: InputViewHolder<TeamEditAdapterListener>, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = items[position]
        if (item is Item<*>) holder.bind(chooser[item])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = ITEM

    interface TeamEditAdapterListener : ImageWorkerFragment.ImagePickerListener {

        fun onAddressClicked()

        fun canEditFields(): Boolean
    }

    private class Chooser internal constructor(private val adapterListener: TeamEditAdapterListener) : TextInputStyle.InputChooser() {

        override fun iconGetter(item: Item<*>): Int = when {
            item.stringRes == R.string.team_name && adapterListener.canEditFields() -> R.drawable.ic_picture_white_24dp
            else -> 0
        }

        override fun enabler(item: Item<*>): Boolean = when (item.itemType) {
            Item.ABOUT -> item.never
            ZIP, Item.CITY, Item.INFO, Item.STATE, Item.INPUT, Item.SPORT, Item.NUMBER, Item.DESCRIPTION -> adapterListener.canEditFields()
            else -> item.never
        }

        override fun textChecker(item: Item<*>): CharSequence? = when (item.itemType) {
            Item.CITY, Item.STATE, Item.INPUT, Item.NUMBER -> Item.NON_EMPTY.invoke(item)
            Item.INFO -> Item.ALLOWS_SPECIAL_CHARACTERS.invoke(item)
            ZIP, Item.DESCRIPTION -> Item.ALL_INPUT_VALID.invoke(item)
            else -> Item.NON_EMPTY.invoke(item)
        }

        override fun invoke(item: Item<*>): TextInputStyle {
            when (val itemType = item.itemType) {
                ZIP, Item.CITY, Item.INFO, Item.STATE, Item.ABOUT, Item.INPUT, Item.NUMBER, Item.DESCRIPTION -> return TextInputStyle(
                        or((itemType == Item.CITY || itemType == Item.STATE || itemType == ZIP),
                                { adapterListener.onAddressClicked() },
                                Item.NO_CLICK),
                        or(itemType == Item.INPUT,
                                { adapterListener.onImageClick() },

                                { adapterListener.onAddressClicked() }),
                        { this.enabler(it) },
                        { this.textChecker(it) },
                        { this.iconGetter(it) })
                Item.SPORT -> return SpinnerTextInputStyle(
                        R.string.choose_sport,
                        Config.getSports(),
                        { it.name },
                        { it.code },
                        { this.enabler(it) })
                else -> return TextInputStyle(
                        or((itemType == Item.CITY || itemType == Item.STATE || itemType == ZIP),
                                { adapterListener.onAddressClicked() },
                                Item.NO_CLICK),
                        or(itemType == Item.INPUT,
                                { adapterListener.onImageClick() },
                                { adapterListener.onAddressClicked() }),
                        { this.enabler(it) },
                        { this.textChecker(it) },
                        { this.iconGetter(it) })
            }
        }
    }
}
