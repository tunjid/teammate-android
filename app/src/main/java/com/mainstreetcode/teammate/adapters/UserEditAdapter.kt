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
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.model.noSpecialCharacters
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

/**
 * Adapter for [User]
 */

interface UserEditAdapterListener : ImageWorkerFragment.ImagePickerListener {
    fun canEdit(): Boolean
}

fun userEditAdapter(
        modelSource: () -> List<Differentiable>,
        delegate: UserEditAdapterListener
): RecyclerView.Adapter<InputViewHolder> {
    val chooser = UserEditChooser(delegate)
    return adapterOf(
            itemsSource = modelSource,
            viewHolderCreator = { viewGroup: ViewGroup, _: Int ->
                InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
            },
            viewHolderBinder = { holder, item, _ ->
                if (item is Item) holder.bind(chooser[item])
            },
            itemIdFunction = { it.diffId.hashCode().toLong() },
            onViewHolderRecycled = InputViewHolder::clear,
            onViewHolderDetached = InputViewHolder::onDetached,
            onViewHolderRecycleFailed = { it.clear(); false }
    )
}

private class UserEditChooser internal constructor(
        private val delegate: UserEditAdapterListener
) : TextInputStyle.InputChooser() {

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