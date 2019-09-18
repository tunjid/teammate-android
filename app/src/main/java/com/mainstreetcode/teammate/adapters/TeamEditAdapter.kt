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
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.neverEnabled
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.model.noSpecialCharacters
import com.mainstreetcode.teammate.util.ITEM
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.tunjid.androidbootstrap.view.util.inflate

/**
 * Adapter for [Team]
 */

class TeamEditAdapter(
        private val items: List<Differentiable>,
        listener: TeamEditAdapterListener
) : BaseAdapter<InputViewHolder, TeamEditAdapter.TeamEditAdapterListener>(listener) {

    private val chooser: TextInputStyle.InputChooser

    init {
        chooser = Chooser(delegate)
    }// setHasStableIds(true); DO NOT PUT THIS BACK

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

    interface TeamEditAdapterListener : ImageWorkerFragment.ImagePickerListener {

        fun onAddressClicked()

        fun canEditFields(): Boolean
    }

    private class Chooser internal constructor(private val delegate: TeamEditAdapterListener) : TextInputStyle.InputChooser() {

        override fun iconGetter(item: Item): Int = when {
            item.stringRes == R.string.team_name && delegate.canEditFields() -> R.drawable.ic_picture_white_24dp
            else -> 0
        }

        override fun enabler(item: Item): Boolean = when (item.itemType) {
            Item.ABOUT -> item.neverEnabled
            Item.ZIP,
            Item.CITY,
            Item.INFO,
            Item.STATE,
            Item.INPUT,
            Item.SPORT,
            Item.NUMBER,
            Item.DESCRIPTION -> delegate.canEditFields()
            else -> item.neverEnabled
        }

        override fun textChecker(item: Item): CharSequence? = when (item.itemType) {
            Item.CITY,
            Item.STATE,
            Item.INPUT,
            Item.NUMBER -> item.noBlankFields
            Item.INFO -> item.noSpecialCharacters
            Item.ZIP,
            Item.DESCRIPTION -> item.noInputValidation
            else -> item.noBlankFields
        }

        override fun invoke(item: Item): TextInputStyle = when (val itemType = item.itemType) {
            Item.ZIP,
            Item.CITY,
            Item.INFO,
            Item.STATE,
            Item.ABOUT,
            Item.INPUT,
            Item.NUMBER,
            Item.DESCRIPTION -> TextInputStyle(
                    or((itemType == Item.CITY || itemType == Item.STATE || itemType == Item.ZIP),
                            delegate::onAddressClicked,
                            Item.noClicks),
                    or(itemType == Item.INPUT,
                            delegate::onImageClick,
                            delegate::onAddressClicked),
                    this::enabler,
                    this::textChecker,
                    this::iconGetter)
            Item.SPORT -> SpinnerTextInputStyle(
                    R.string.choose_sport,
                    Config.getSports(),
                    Sport::name,
                    Sport::code,
                    this::enabler)
            else -> TextInputStyle(
                    or((itemType == Item.CITY || itemType == Item.STATE || itemType == Item.ZIP),
                            delegate::onAddressClicked,
                            Item.noClicks),
                    or(itemType == Item.INPUT,
                            delegate::onImageClick,
                            delegate::onAddressClicked),
                    this::enabler,
                    this::textChecker,
                    this::iconGetter)
        }
    }
}
