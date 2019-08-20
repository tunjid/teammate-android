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
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.UserViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.DateTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.StatAggregate
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.always
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.util.ITEM
import com.mainstreetcode.teammate.util.TEAM
import com.mainstreetcode.teammate.util.USER
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import java.util.*

/**
 * Adapter for [Event]
 */

class StatAggregateRequestAdapter(
        private val request: StatAggregate.Request,
        listener: AdapterListener
) : BaseAdapter<BaseViewHolder<*>, StatAggregateRequestAdapter.AdapterListener>(listener) {
    private val chooser: TextInputStyle.InputChooser

    init {
        chooser = Chooser()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseViewHolder<*> = when (viewType) {
        ITEM -> InputViewHolder<ImageWorkerFragment.ImagePickerListener>(getItemView(R.layout.viewholder_simple_input, viewGroup))
        USER -> UserViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), UserAdapter.AdapterListener.asSAM(adapterListener::onUserPicked))
                .withTitle(R.string.pick_user)
        TEAM -> TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), TeamAdapter.AdapterListener.asSAM(adapterListener::onTeamPicked))
                .withTitle(R.string.pick_team)
        else -> InputViewHolder<ImageWorkerFragment.ImagePickerListener>(getItemView(R.layout.viewholder_simple_input, viewGroup))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : InteractiveAdapter.AdapterListener> updateListener(viewHolder: BaseViewHolder<S>): S = when {
        viewHolder.itemViewType == USER -> UserAdapter.AdapterListener.asSAM(adapterListener::onUserPicked) as S
        viewHolder.itemViewType == TEAM -> TeamAdapter.AdapterListener.asSAM(adapterListener::onTeamPicked) as S
        else -> adapterListener as S
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        super.onBindViewHolder(holder, position)
        when (val identifiable = request.items[position]) {
            is Item<*> -> (holder as InputViewHolder<*>).bind(chooser[identifiable])
            is User -> (holder as UserViewHolder).bind(identifiable)
            is Team -> (holder as TeamViewHolder).bind(identifiable)
        }
    }

    override fun getItemCount(): Int = request.items.size

    override fun getItemViewType(position: Int): Int {
        val identifiable = request.items[position]
        return if (identifiable is Item<*>) ITEM else if (identifiable is User) USER else TEAM
    }

    interface AdapterListener : InteractiveAdapter.AdapterListener {
        fun onUserPicked(user: User)

        fun onTeamPicked(team: Team)
    }

    internal class Chooser internal constructor() : TextInputStyle.InputChooser() {

        private val sports: MutableList<Sport>

        init {
            sports = ArrayList(Config.getSports())
            sports.add(0, Sport.empty())
        }

        override fun invoke(item: Item<*>): TextInputStyle = when (item.itemType) {
            Item.SPORT -> SpinnerTextInputStyle(
                    R.string.choose_sport,
                    sports,
                    Sport::name,
                    Sport::code,
                    Item<*>::always,
                    Item<*>::noInputValidation)
            Item.DATE -> DateTextInputStyle(Item<*>::always)
            else -> SpinnerTextInputStyle(
                    R.string.choose_sport,
                    sports,
                    Sport::name,
                    Sport::code,
                    Item<*>::always,
                    Item<*>::noInputValidation)
        }
    }
}
