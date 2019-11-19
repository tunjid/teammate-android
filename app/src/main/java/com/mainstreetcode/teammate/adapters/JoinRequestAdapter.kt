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
import com.mainstreetcode.teammate.adapters.viewholders.input.or
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.enums.Position
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.neverEnabled
import com.mainstreetcode.teammate.model.noIcon
import com.mainstreetcode.teammate.model.noInputValidation
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

interface JoinRequestAdapterListener : ImageWorkerFragment.ImagePickerListener {
    fun canEditFields(): Boolean

    fun canEditRole(): Boolean
}

fun joinRequestAdapter(
        modelSource: () -> List<Differentiable>,
        delegate: JoinRequestAdapterListener
): RecyclerView.Adapter<InputViewHolder> {
    val chooser = JoinRequestChooser(delegate)
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

private class JoinRequestChooser internal constructor(
        private val delegate: JoinRequestAdapterListener
) : TextInputStyle.InputChooser() {

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