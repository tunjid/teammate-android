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
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.enums.Position
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.util.ITEM
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Adapter for [Role]
 */

class RoleEditAdapter(
        private val items: List<Differentiable>,
        listener: RoleEditAdapterListener
) : BaseAdapter<InputViewHolder<RoleEditAdapter.RoleEditAdapterListener>, RoleEditAdapter.RoleEditAdapterListener>(listener) {

    private val chooser: TextInputStyle.InputChooser

    init {
        chooser = Chooser(adapterListener)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): InputViewHolder<RoleEditAdapterListener> {
        return InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : AdapterListener> updateListener(viewHolder: BaseViewHolder<S>): S {
        return adapterListener as S
    }

    override fun onBindViewHolder(holder: InputViewHolder<RoleEditAdapterListener>, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = items[position]
        if (item is Item<*>) holder.bind(chooser[item])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = ITEM

    interface RoleEditAdapterListener : ImageWorkerFragment.ImagePickerListener {
        fun canChangeRolePosition(): Boolean

        fun canChangeRoleFields(): Boolean
    }

    internal class Chooser internal constructor(private val adapterListener: RoleEditAdapterListener) : TextInputStyle.InputChooser() {

        override fun enabler(item: Item<*>): Boolean = when (item.itemType) {
            Item.INPUT, Item.ABOUT -> false
            Item.NICKNAME -> adapterListener.canChangeRoleFields()
            else -> false
        }

        override fun iconGetter(item: Item<*>): Int = when (item.itemType) {
            Item.ROLE, Item.ABOUT, Item.NICKNAME -> 0
            Item.INPUT -> when {
                item.stringRes == R.string.first_name && adapterListener.canChangeRolePosition() -> R.drawable.ic_picture_white_24dp
                else -> 0
            }
            else -> 0
        }

        override fun textChecker(item: Item<*>): CharSequence? = when (item.itemType) {
            Item.INPUT -> item.noBlankFields
            Item.ABOUT,
            Item.NICKNAME -> item.noInputValidation
            else -> item.noBlankFields
        }

        override fun invoke(item: Item<*>): TextInputStyle = when (item.itemType) {
            Item.INPUT,
            Item.ABOUT,
            Item.NICKNAME -> TextInputStyle(
                    Item.NO_CLICK,
                    adapterListener::onImageClick,
                    this::enabler,
                    this::textChecker,
                    this::iconGetter)
            Item.ROLE -> SpinnerTextInputStyle(
                    R.string.choose_role,
                    Config.getPositions(),
                    Position::name,
                    Position::code
            ) { adapterListener.canChangeRolePosition() }
            else -> TextInputStyle(
                    Item.NO_CLICK,
                    adapterListener::onImageClick,
                    this::enabler,
                    this::textChecker,
                    this::iconGetter)
        }
    }
}
