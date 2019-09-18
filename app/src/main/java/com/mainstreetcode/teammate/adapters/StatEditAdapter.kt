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
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.StatAttributeViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.Item.Companion.STAT_TYPE
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.alwaysEnabled
import com.mainstreetcode.teammate.model.enums.StatType
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noIcon
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.util.ITEM
import com.mainstreetcode.teammate.util.TEAM
import com.mainstreetcode.teammate.util.USER
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.tunjid.androidbootstrap.view.util.inflate

/**
 * Adapter for [com.mainstreetcode.teammate.model.Tournament]
 */

class StatEditAdapter(
        private val items: List<Differentiable>,
        listener: AdapterListener
) : BaseAdapter<BaseViewHolder<*>, StatEditAdapter.AdapterListener>(listener) {

    private val chooser: TextInputStyle.InputChooser
    private val userListener = UserAdapter.AdapterListener.asSAM { delegate.onUserClicked() }
    private val teamListener = TeamAdapter.AdapterListener.asSAM { delegate.onTeamClicked() }

    init {
        chooser = Chooser(delegate)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseViewHolder<*> = when (viewType) {
        ITEM -> InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
        STAT_TYPE -> StatAttributeViewHolder(viewGroup.inflate(R.layout.viewholder_stat_type),
                delegate.stat)
        USER -> UserViewHolder(viewGroup.inflate(R.layout.viewholder_list_item), userListener)
        TEAM -> TeamViewHolder(viewGroup.inflate(R.layout.viewholder_list_item), teamListener)
        else -> InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : Any> updateListener(viewHolder: BaseViewHolder<S>): S = when {
        viewHolder.itemViewType == USER -> userListener as S
        viewHolder.itemViewType == TEAM -> teamListener as S
        else -> delegate as S
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        super.onBindViewHolder(holder, position)

        when (val item = items[position]) {
            is Item -> (holder as InputViewHolder).bind(chooser[item])
            is User -> (holder as UserViewHolder).bind(item)
            is Team -> (holder as TeamViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (val differentiable = items[position]) {
            is Item ->
                if (STAT_TYPE == differentiable.itemType) STAT_TYPE
                else ITEM
            is User -> USER
            else -> TEAM
        }
    }

    interface AdapterListener : ImageWorkerFragment.ImagePickerListener {
        val stat: Stat

        fun onUserClicked()

        fun onTeamClicked()

        fun canChangeStat(): Boolean
    }

    internal class Chooser internal constructor(private val delegate: AdapterListener) : TextInputStyle.InputChooser() {

        override fun invoke(item: Item): TextInputStyle = when (item.itemType) {
            Item.INPUT, Item.NUMBER -> TextInputStyle(
                    Item.noClicks,
                    Item.noClicks,
                    Item::alwaysEnabled,
                    Item::noBlankFields,
                    Item::noIcon)
            STAT_TYPE -> SpinnerTextInputStyle(
                    R.string.choose_stat,
                    delegate.stat.sport.stats,
                    StatType::emojiAndName,
                    StatType::code,
                    { delegate.canChangeStat() },
                    Item::noInputValidation)
            else -> TextInputStyle(
                    Item.noClicks,
                    Item.noClicks,
                    Item::alwaysEnabled,
                    Item::noBlankFields,
                    Item::noIcon
            )
        }
    }
}
