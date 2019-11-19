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
import com.mainstreetcode.teammate.adapters.viewholders.input.DateTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.HeadToHead
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.alwaysEnabled
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.enums.TournamentType
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.util.AWAY
import com.mainstreetcode.teammate.util.HOME
import com.mainstreetcode.teammate.util.ITEM
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate
import java.util.*

interface HeadToHeadAdapterListener {
    fun onHomeClicked(home: Competitor)

    fun onAwayClicked(away: Competitor)
}

fun headToHeadRequestAdapter(
        request: HeadToHead.Request,
        listener: HeadToHeadAdapterListener
): RecyclerView.Adapter<RecyclerView.ViewHolder> {

    val chooser = HeadToHeadChooser()

    return adapterOf<Differentiable, RecyclerView.ViewHolder>(
            itemsSource = request::items,
            viewHolderCreator = { viewGroup: ViewGroup, viewType: Int ->
                when (viewType) {
                    ITEM -> InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
                    HOME -> CompetitorViewHolder(viewGroup.inflate(R.layout.viewholder_competitor), listener::onHomeClicked)
                            .hideSubtitle().withTitle(R.string.pick_home_competitor)
                    AWAY -> CompetitorViewHolder(viewGroup.inflate(R.layout.viewholder_competitor), listener::onAwayClicked)
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
                when {
                    it is Item -> ITEM
                    it is Competitor && it.entity.id == request.homeId -> HOME
                    else -> AWAY
                }
            },
            itemIdFunction = { it.diffId.hashCode().toLong() },
            onViewHolderRecycled = { if (it is InputViewHolder) it.clear() },
            onViewHolderDetached = { if (it is InputViewHolder) it.onDetached() },
            onViewHolderRecycleFailed = { if (it is InputViewHolder) it.clear(); false }
    )
}

private class HeadToHeadChooser internal constructor() : TextInputStyle.InputChooser() {

    private val sports: MutableList<Sport>

    init {
        sports = ArrayList(Config.getSports())
        sports.add(0, Sport.empty())
    }

    override fun invoke(item: Item): TextInputStyle = when (item.itemType) {
        Item.SPORT -> SpinnerTextInputStyle(
                R.string.choose_sport,
                sports,
                Sport::name,
                Sport::code,
                Item::alwaysEnabled,
                Item::noInputValidation)
        Item.DATE -> DateTextInputStyle(Item::alwaysEnabled)
        Item.TOURNAMENT_TYPE -> SpinnerTextInputStyle(
                R.string.tournament_type,
                Config.getTournamentTypes { true },
                TournamentType::name,
                TournamentType::code,
                Item::alwaysEnabled,
                Item::noInputValidation)
        else -> SpinnerTextInputStyle(
                R.string.choose_sport,
                sports,
                Sport::name,
                Sport::code,
                Item::alwaysEnabled,
                Item::noInputValidation)
    }
}
