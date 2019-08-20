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
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noIcon
import com.mainstreetcode.teammate.util.AWAY
import com.mainstreetcode.teammate.util.ITEM
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Adapter for [com.mainstreetcode.teammate.model.Tournament]
 */

class GameEditAdapter(
        private val items: List<Differentiable>,
        listener: AdapterListener
) : BaseAdapter<BaseViewHolder<*>, GameEditAdapter.AdapterListener>(listener) {
    private val chooser: TextInputStyle.InputChooser
    private val competitorPicker = CompetitorAdapter.AdapterListener.asSAM(adapterListener::onAwayClicked)

    init {
        this.chooser = Chooser(adapterListener)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            ITEM -> InputViewHolder<AdapterListener>(getItemView(R.layout.viewholder_simple_input, viewGroup))
            AWAY -> CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), competitorPicker)
                    .hideSubtitle().withTitle(R.string.pick_away_competitor)
            else -> InputViewHolder<AdapterListener>(getItemView(R.layout.viewholder_simple_input, viewGroup))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : InteractiveAdapter.AdapterListener> updateListener(viewHolder: BaseViewHolder<S>): S =
            if (viewHolder is CompetitorViewHolder) competitorPicker as S
            else adapterListener as S

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        super.onBindViewHolder(holder, position)
        when (val item = items[position]) {
            is Item<*> -> (holder as InputViewHolder<*>).bind(chooser[item])
            is Competitor -> (holder as CompetitorViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is Item<*> -> ITEM
        else -> AWAY
    }

    interface AdapterListener : ImageWorkerFragment.ImagePickerListener {
        fun canEditGame(): Boolean

        fun onAwayClicked(away: Competitor)
    }

    private class Chooser internal constructor(private val adapterListener: AdapterListener) : TextInputStyle.InputChooser() {

        override fun enabler(item: Item<*>): Boolean = when (item.itemType) {
            Item.INPUT -> adapterListener.canEditGame()
            else -> false
        }

        override fun invoke(item: Item<*>): TextInputStyle = when (item.itemType) {
            Item.INPUT,
            Item.NUMBER -> TextInputStyle(
                    Item.NO_CLICK,
                    Item.NO_CLICK,
                    this::enabler,
                    Item<*>::noBlankFields,
                    Item<*>::noIcon
            )
            else -> TextInputStyle(
                    Item.NO_CLICK,
                    Item.NO_CLICK,
                    this::enabler,
                    Item<*>::noBlankFields,
                    Item<*>::noIcon
            )
        }
    }
}
