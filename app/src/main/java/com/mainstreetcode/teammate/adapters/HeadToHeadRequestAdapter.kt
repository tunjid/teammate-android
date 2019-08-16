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
import com.mainstreetcode.teammate.adapters.viewholders.input.DateTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.HeadToHead
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.Item.Companion.ALL_INPUT_VALID
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.util.AWAY
import com.mainstreetcode.teammate.util.HOME
import com.mainstreetcode.teammate.util.ITEM
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import java.util.*

/**
 * Adapter for [Event]
 */

class HeadToHeadRequestAdapter(
        private val request: HeadToHead.Request,
        listener: AdapterListener
) : BaseAdapter<BaseViewHolder<*>, HeadToHeadRequestAdapter.AdapterListener>(listener) {

    private val chooser: Chooser

    init {
        chooser = Chooser()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            ITEM -> InputViewHolder<ImageWorkerFragment.ImagePickerListener>(getItemView(R.layout.viewholder_simple_input, viewGroup))
            HOME -> CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), CompetitorAdapter.AdapterListener.asSAM { adapterListener.onHomeClicked(it) })
                    .hideSubtitle().withTitle(R.string.pick_home_competitor)
            AWAY -> CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), CompetitorAdapter.AdapterListener.asSAM { adapterListener.onAwayClicked(it) })
                    .hideSubtitle().withTitle(R.string.pick_away_competitor)
            else -> InputViewHolder<ImageWorkerFragment.ImagePickerListener>(getItemView(R.layout.viewholder_simple_input, viewGroup))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : InteractiveAdapter.AdapterListener> updateListener(viewHolder: BaseViewHolder<S>): S {
        return when {
            viewHolder.itemViewType == HOME -> CompetitorAdapter.AdapterListener.asSAM { adapterListener.onHomeClicked(it) } as S
            viewHolder.itemViewType == AWAY -> CompetitorAdapter.AdapterListener.asSAM { adapterListener.onAwayClicked(it) } as S
            else -> adapterListener as S
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        super.onBindViewHolder(holder, position)
        when (val identifiable = request.items[position]) {
            is Item<*> -> (holder as InputViewHolder<*>).bind(chooser[identifiable])
            is Competitor -> (holder as CompetitorViewHolder).bind(identifiable)
        }
    }

    override fun getItemCount(): Int = request.items.size

    override fun getItemViewType(position: Int): Int {
        val identifiable = request.items[position]
        return when {
            identifiable is Item<*> -> ITEM
            position == 2 -> HOME
            else -> AWAY
        }
    }

    interface AdapterListener : InteractiveAdapter.AdapterListener {
        fun onHomeClicked(home: Competitor)

        fun onAwayClicked(away: Competitor)
    }

    private class Chooser internal constructor() : TextInputStyle.InputChooser() {

        private val sports: MutableList<Sport>

        init {
            sports = ArrayList(Config.getSports())
            sports.add(0, Sport.empty())
        }

        override fun invoke(item: Item<*>): TextInputStyle = when (item.itemType) {
            Item.SPORT -> SpinnerTextInputStyle(
                    R.string.choose_sport,
                    sports,
                    { it.name },
                    { it.code },
                    Item.TRUE,
                    ALL_INPUT_VALID)
            Item.DATE -> DateTextInputStyle(Item.TRUE)
            Item.TOURNAMENT_TYPE -> SpinnerTextInputStyle(
                    R.string.tournament_type,
                    Config.getTournamentTypes { true },
                    { it.name },
                    { it.code },
                    Item.TRUE,
                    ALL_INPUT_VALID)
            else -> SpinnerTextInputStyle(
                    R.string.choose_sport,
                    sports,
                    { it.name },
                    { it.code },
                    Item.TRUE,
                    ALL_INPUT_VALID)
        }
    }
}
