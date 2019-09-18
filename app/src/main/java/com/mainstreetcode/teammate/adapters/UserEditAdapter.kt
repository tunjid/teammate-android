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
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.model.noSpecialCharacters
import com.mainstreetcode.teammate.util.ITEM
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.tunjid.androidbootstrap.view.util.inflate

/**
 * Adapter for [User]
 */

class UserEditAdapter(
        private val items: List<Differentiable>,
        listener: AdapterListener
) : BaseAdapter<InputViewHolder, UserEditAdapter.AdapterListener>(listener) {

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

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    interface AdapterListener : ImageWorkerFragment.ImagePickerListener {
        fun canEdit(): Boolean
    }

    private class Chooser internal constructor(private val delegate: AdapterListener) : TextInputStyle.InputChooser() {

        override fun iconGetter(item: Item): Int =
                if (delegate.canEdit() && item.stringRes == R.string.first_name) R.drawable.ic_picture_white_24dp else 0

        override fun textChecker(item: Item): CharSequence? = when (item.itemType) {
            Item.INPUT -> item.noBlankFields
            Item.INFO -> item.noSpecialCharacters
            Item.ABOUT -> item.noInputValidation
            else -> item.noBlankFields
        }

        override fun invoke(item: Item): TextInputStyle = when (item.itemType) {
            Item.INFO,
            Item.INPUT,
            Item.ABOUT -> TextInputStyle(
                    Item.noClicks,
                    delegate::onImageClick,
                    { delegate.canEdit() },
                    this::textChecker,
                    this::iconGetter)
            else -> TextInputStyle(Item.noClicks,
                    delegate::onImageClick,
                    { delegate.canEdit() },
                    this::textChecker,
                    this::iconGetter)
        }
    }
}
