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
import com.mainstreetcode.teammate.adapters.viewholders.GuestViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.DateTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.enums.Visibility
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.util.GUEST
import com.mainstreetcode.teammate.util.ITEM
import com.mainstreetcode.teammate.util.TEAM
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

/**
 * Adapter for [com.mainstreetcode.teammate.model.Event]
 */

class EventEditAdapter(
        private val identifiables: List<Differentiable>,
        listener: EventEditAdapterListener
) : BaseAdapter<BaseViewHolder<*>, EventEditAdapter.EventEditAdapterListener>(listener) {

    private val chooser = Chooser(delegate)

    private val teamListener = TeamAdapter.AdapterListener.asSAM { delegate.selectTeam() }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseViewHolder<*> =
            when (viewType) {
                ITEM -> InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
                GUEST -> GuestViewHolder(viewGroup.inflate(R.layout.viewholder_event_guest), delegate)
                TEAM -> TeamViewHolder(viewGroup.inflate(R.layout.viewholder_list_item), teamListener)
                else -> InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
            }

    @Suppress("UNCHECKED_CAST")
    override fun <S : Any> updateListener(viewHolder: BaseViewHolder<S>): S =
            if (viewHolder is TeamViewHolder) teamListener as S else delegate as S

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        super.onBindViewHolder(holder, position)
        when (val item = identifiables[position]) {
            is Item -> (holder as InputViewHolder).bind(chooser[item])
            is Team -> (holder as TeamViewHolder).bind(item)
            is Guest -> (holder as GuestViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = identifiables.size

    override fun getItemId(position: Int): Long = identifiables[position].diffId.hashCode().toLong()

    override fun getItemViewType(position: Int): Int = when (identifiables[position]) {
        is Item -> ITEM
        is Team -> TEAM
        else -> GUEST
    }

    interface EventEditAdapterListener : TeamAdapter.AdapterListener, ImageWorkerFragment.ImagePickerListener {

        fun selectTeam()

        fun onLocationClicked()

        fun onGuestClicked(guest: Guest)

        fun canEditEvent(): Boolean
    }

    internal class Chooser internal constructor(private val delegate: EventEditAdapterListener) : TextInputStyle.InputChooser() {

        override fun iconGetter(item: Item): Int =
                if (item.itemType == Item.LOCATION) R.drawable.ic_location_on_white_24dp else 0

        override fun enabler(item: Item): Boolean = delegate.canEditEvent()

        override fun textChecker(item: Item): CharSequence? = when (item.itemType) {
            Item.INPUT,
            Item.DATE -> item.noBlankFields
            Item.TEXT,
            Item.NUMBER,
            Item.LOCATION -> item.noInputValidation
            else -> item.noBlankFields
        }

        override fun invoke(item: Item): TextInputStyle = when (item.itemType) {
            Item.INPUT, Item.TEXT, Item.NUMBER, Item.LOCATION -> TextInputStyle(
                    Item.noClicks,
                    delegate::onLocationClicked,
                    { delegate.canEditEvent() },
                    this::textChecker,
                    this::iconGetter)
            Item.VISIBILITY -> SpinnerTextInputStyle(
                    R.string.event_visibility_selection,
                    Config.getVisibilities(),
                    Visibility::name,
                    Visibility::code
            ) { delegate.canEditEvent() }
            Item.DATE -> DateTextInputStyle { delegate.canEditEvent() }
            else -> TextInputStyle(
                    Item.noClicks,
                    delegate::onLocationClicked,
                    { delegate.canEditEvent() },
                    this::textChecker,
                    this::iconGetter
            )
        }
    }
}
