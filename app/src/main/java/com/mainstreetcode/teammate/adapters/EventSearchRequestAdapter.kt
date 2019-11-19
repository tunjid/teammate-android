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
import com.mainstreetcode.teammate.adapters.viewholders.input.DateTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.or
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.EventSearchRequest
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noInputValidation
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate
import java.util.*

interface EventSearchAdapterListener {
    fun onLocationClicked()
}

fun eventSearchRequestAdapter(
        request: EventSearchRequest,
        delegate: EventSearchAdapterListener
): RecyclerView.Adapter<RecyclerView.ViewHolder> {
    val chooser = EventSearchRequestChooser(delegate)
    return adapterOf<Differentiable, RecyclerView.ViewHolder>(
            itemsSource = request::asItems,
            viewHolderCreator = { viewGroup: ViewGroup, _: Int ->
                InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
            },
            viewHolderBinder = { holder, item, _ ->
                if (item is Item && holder is InputViewHolder) holder.bind(chooser[item])
            },
            itemIdFunction = { it.diffId.hashCode().toLong() },
            onViewHolderRecycled = { if (it is InputViewHolder) it.clear() },
            onViewHolderDetached = { if (it is InputViewHolder) it.onDetached() },
            onViewHolderRecycleFailed = { if (it is InputViewHolder) it.clear(); false }
    )
}

private class EventSearchRequestChooser(
        private val delegate: EventSearchAdapterListener
) : TextInputStyle.InputChooser() {

    private val sports: MutableList<Sport>

    init {
        this.sports = ArrayList(Config.getSports())
        sports.add(0, Sport.empty())
    }

    override fun iconGetter(item: Item): Int = when (item.itemType) {
        Item.LOCATION -> R.drawable.ic_location_on_white_24dp
        else -> 0
    }

    override fun enabler(item: Item): Boolean = when (item.itemType) {
        Item.INFO -> false
        Item.DATE, Item.SPORT, Item.LOCATION -> true
        else -> false
    }

    override fun textChecker(item: Item): CharSequence? = when (item.itemType) {
        Item.DATE -> item.noBlankFields
        Item.SPORT,
        Item.LOCATION -> item.noInputValidation
        else -> item.noBlankFields
    }

    override fun invoke(item: Item): TextInputStyle = when (val itemType = item.itemType) {
        Item.INFO,
        Item.LOCATION -> TextInputStyle(
                delegate::onLocationClicked,
                or(itemType == Item.LOCATION, delegate::onLocationClicked, Item.noClicks),
                this::enabler,
                this::textChecker,
                this::iconGetter)
        Item.SPORT -> SpinnerTextInputStyle(
                R.string.choose_sport,
                sports,
                Sport::name,
                Sport::code,
                this::enabler,
                this::textChecker)
        Item.DATE -> DateTextInputStyle(this::enabler)
        else -> TextInputStyle(
                Item.proxyClicks,
                or(itemType == Item.LOCATION, delegate::onLocationClicked, Item.noClicks),
                this::enabler,
                this::textChecker,
                this::iconGetter)
    }
}
