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
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noIcon
import com.mainstreetcode.teammate.util.AWAY
import com.mainstreetcode.teammate.util.ITEM
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

interface GameEditAdapterListener : ImageWorkerFragment.ImagePickerListener {
    fun canEditGame(): Boolean

    fun onAwayClicked(away: Competitor)
}

fun gameEditAdapter(
        modelSource: () -> List<Differentiable>,
        delegate: GameEditAdapterListener
): RecyclerView.Adapter<RecyclerView.ViewHolder> {
    val chooser = GameEditChooser(delegate)
    return adapterOf<Differentiable, RecyclerView.ViewHolder>(
            itemsSource = modelSource,
            viewHolderCreator = { viewGroup: ViewGroup, viewType: Int ->
                when (viewType) {
                    ITEM -> InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
                    AWAY -> CompetitorViewHolder(viewGroup.inflate(R.layout.viewholder_competitor), delegate::onAwayClicked)
                            .hideSubtitle().withTitle(R.string.pick_away_competitor)
                    else -> InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
                }
            },
            viewHolderBinder = { holder, item, _ ->
                when {
                    item is Item && holder is InputViewHolder -> holder.bind(chooser[item])
                    item is Competitor && holder is CompetitorViewHolder -> holder.bind(item)
                }
            },
            viewTypeFunction = {
                when (it) {
                    is Item -> ITEM
                    else -> AWAY
                }
            },
            onViewHolderRecycled = { if (it is InputViewHolder) it.clear() },
            onViewHolderDetached = { if (it is InputViewHolder) it.onDetached() },
            onViewHolderRecycleFailed = { if (it is InputViewHolder) it.clear(); false }
    )
}

private class GameEditChooser internal constructor(private val delegate: GameEditAdapterListener) : TextInputStyle.InputChooser() {

    override fun enabler(item: Item): Boolean = when (item.itemType) {
        Item.INPUT -> delegate.canEditGame()
        else -> false
    }

    override fun invoke(item: Item): TextInputStyle = when (item.itemType) {
        Item.INPUT,
        Item.NUMBER -> TextInputStyle(
                Item.noClicks,
                Item.noClicks,
                this::enabler,
                Item::noBlankFields,
                Item::noIcon
        )
        else -> TextInputStyle(
                Item.noClicks,
                Item.noClicks,
                this::enabler,
                Item::noBlankFields,
                Item::noIcon
        )
    }
}