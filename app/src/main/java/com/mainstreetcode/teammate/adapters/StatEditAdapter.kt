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
import com.mainstreetcode.teammate.model.Item.Companion.ALL_INPUT_VALID
import com.mainstreetcode.teammate.model.Item.Companion.STAT_TYPE
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM
import com.mainstreetcode.teammate.util.ViewHolderUtil.TEAM
import com.mainstreetcode.teammate.util.ViewHolderUtil.USER
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Adapter for [com.mainstreetcode.teammate.model.Tournament]
 */

class StatEditAdapter(private val items: List<Differentiable>, listener: AdapterListener) : BaseAdapter<BaseViewHolder<*>, StatEditAdapter.AdapterListener>(listener) {
    private val chooser: TextInputStyle.InputChooser
    private val userListener = UserAdapter.AdapterListener.asSAM { adapterListener.onUserClicked() }
    private val teamListener = TeamAdapter.AdapterListener.asSAM { adapterListener.onTeamClicked() }

    init {
        chooser = Chooser(adapterListener)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseViewHolder<*> = when (viewType) {
        ITEM -> InputViewHolder<AdapterListener>(getItemView(R.layout.viewholder_simple_input, viewGroup))
        STAT_TYPE -> StatAttributeViewHolder(getItemView(R.layout.viewholder_stat_type, viewGroup),
                adapterListener.stat)
        USER -> UserViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), userListener)
        TEAM -> TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), teamListener)
        else -> InputViewHolder<AdapterListener>(getItemView(R.layout.viewholder_simple_input, viewGroup))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : InteractiveAdapter.AdapterListener> updateListener(viewHolder: BaseViewHolder<S>): S = when {
        viewHolder.itemViewType == USER -> userListener as S
        viewHolder.itemViewType == TEAM -> teamListener as S
        else -> adapterListener as S
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        super.onBindViewHolder(holder, position)

        when (val item = items[position]) {
            is Item<*> -> (holder as InputViewHolder<*>).bind(chooser[item])
            is User -> (holder as UserViewHolder).bind(item)
            is Team -> (holder as TeamViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (val differentiable = items[position]) {
            is Item<*> ->
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

    internal class Chooser internal constructor(private val adapterListener: AdapterListener) : TextInputStyle.InputChooser() {

        override fun invoke(item: Item<*>): TextInputStyle {
            when (item.itemType) {
                Item.INPUT, Item.NUMBER -> return TextInputStyle(
                        Item.NO_CLICK,
                        Item.NO_CLICK,
                        Item.TRUE,
                        Item.NON_EMPTY,
                        Item.NO_ICON)
                STAT_TYPE -> {
                    val stat = adapterListener.stat
                    return SpinnerTextInputStyle(
                            R.string.choose_stat,
                            stat.sport.stats,
                            { it.emojiAndName },
                            { it.code },
                            { adapterListener.canChangeStat() },
                            ALL_INPUT_VALID)
                }
                else -> return TextInputStyle(Item.NO_CLICK, Item.NO_CLICK, Item.TRUE, Item.NON_EMPTY, Item.NO_ICON)
            }
        }
    }
}
