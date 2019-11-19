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
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.enums.Position
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noInputValidation
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

interface RoleEditAdapterListener : ImageWorkerFragment.ImagePickerListener {
    fun canChangeRolePosition(): Boolean

    fun canChangeRoleFields(): Boolean
}

fun roleEditAdapter(
        modelSource: () -> List<Differentiable>,
        delegate: RoleEditAdapterListener
): RecyclerView.Adapter<InputViewHolder> {
    val chooser = RoleEditChooser(delegate)
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

private class RoleEditChooser internal constructor(
        private val delegate: RoleEditAdapterListener
) : TextInputStyle.InputChooser() {

    override fun enabler(item: Item): Boolean = when (item.itemType) {
        Item.INPUT, Item.ABOUT -> false
        Item.NICKNAME -> delegate.canChangeRoleFields()
        else -> false
    }

    override fun iconGetter(item: Item): Int = when (item.itemType) {
        Item.ROLE, Item.ABOUT, Item.NICKNAME -> 0
        Item.INPUT -> when {
            item.stringRes == R.string.first_name && delegate.canChangeRolePosition() -> R.drawable.ic_picture_white_24dp
            else -> 0
        }
        else -> 0
    }

    override fun textChecker(item: Item): CharSequence? = when (item.itemType) {
        Item.INPUT -> item.noBlankFields
        Item.ABOUT,
        Item.NICKNAME -> item.noInputValidation
        else -> item.noBlankFields
    }

    override fun invoke(item: Item): TextInputStyle = when (item.itemType) {
        Item.INPUT,
        Item.ABOUT,
        Item.NICKNAME -> TextInputStyle(
                Item.noClicks,
                delegate::onImageClick,
                this::enabler,
                this::textChecker,
                this::iconGetter)
        Item.ROLE -> SpinnerTextInputStyle(
                R.string.choose_role,
                Config.getPositions(),
                Position::name,
                Position::code
        ) { delegate.canChangeRolePosition() }
        else -> TextInputStyle(
                Item.noClicks,
                delegate::onImageClick,
                this::enabler,
                this::textChecker,
                this::iconGetter)
    }
}
