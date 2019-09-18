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
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.input.SpinnerTextInputStyle
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.enums.TournamentStyle
import com.mainstreetcode.teammate.model.enums.TournamentType
import com.mainstreetcode.teammate.model.neverEnabled
import com.mainstreetcode.teammate.model.noBlankFields
import com.mainstreetcode.teammate.model.noInputValidation
import com.mainstreetcode.teammate.util.ITEM
import com.mainstreetcode.teammate.util.TOURNAMENT
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.tunjid.androidbootstrap.view.util.inflate

/**
 * Adapter for [com.mainstreetcode.teammate.model.Tournament]
 */

class TournamentEditAdapter(
        private val items: List<Differentiable>,
        listener: AdapterListener
) : BaseAdapter<BaseViewHolder<*>, TournamentEditAdapter.AdapterListener>(listener) {

    private val chooser: TextInputStyle.InputChooser
    private val listener = CompetitorAdapter.AdapterListener.asSAM { }

    init {
        chooser = Chooser(delegate)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseViewHolder<*> = when (viewType) {
        ITEM -> InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
        TOURNAMENT -> CompetitorViewHolder(viewGroup.inflate(R.layout.viewholder_competitor), listener)
        else -> InputViewHolder(viewGroup.inflate(R.layout.viewholder_simple_input))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : Any> updateListener(viewHolder: BaseViewHolder<S>): S =
            listener as S

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        super.onBindViewHolder(holder, position)

        when (val item = items[position]) {
            is Item -> (holder as InputViewHolder).bind(chooser[item])
            is Competitor -> (holder as CompetitorViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val thing = items[position]
        return if (thing is Item) ITEM else TOURNAMENT
    }

    interface AdapterListener : ImageWorkerFragment.ImagePickerListener {

        val sport: Sport

        fun canEditBeforeCreation(): Boolean

        fun canEditAfterCreation(): Boolean
    }

    private class Chooser internal constructor(private val delegate: AdapterListener) : TextInputStyle.InputChooser() {

        override fun iconGetter(item: Item): Int = when {
            item.stringRes == R.string.tournament_name && delegate.canEditAfterCreation() -> R.drawable.ic_picture_white_24dp
            else -> 0
        }

        override fun enabler(item: Item): Boolean = when (item.itemType) {
            Item.ABOUT -> item.neverEnabled
            Item.INFO,
            Item.INPUT,
            Item.NUMBER,
            Item.TOURNAMENT_TYPE,
            Item.TOURNAMENT_STYLE -> delegate.canEditBeforeCreation()
            Item.DESCRIPTION -> delegate.canEditAfterCreation()
            else -> item.neverEnabled
        }

        override fun textChecker(item: Item): CharSequence? = when (item.itemType) {
            Item.INPUT,
            Item.NUMBER -> item.noBlankFields
            Item.INFO,
            Item.DESCRIPTION,
            Item.TOURNAMENT_TYPE,
            Item.TOURNAMENT_STYLE -> item.noInputValidation
            else -> item.noBlankFields
        }

        override fun invoke(item: Item): TextInputStyle = when (item.itemType) {
            Item.INPUT,
            Item.NUMBER,
            Item.DESCRIPTION -> TextInputStyle(
                    Item.noClicks,
                    delegate::onImageClick,
                    this::enabler,
                    this::textChecker,
                    this::iconGetter)
            Item.TOURNAMENT_TYPE -> SpinnerTextInputStyle(
                    R.string.tournament_type,
                    Config.getTournamentTypes(delegate.sport::supportsTournamentType),
                    TournamentType::name,
                    TournamentType::code,
                    this::enabler,
                    Item::noInputValidation)
            Item.TOURNAMENT_STYLE -> SpinnerTextInputStyle(
                    R.string.tournament_style,
                    Config.getTournamentStyles(delegate.sport::supportsTournamentStyle),
                    TournamentStyle::name,
                    TournamentStyle::code,
                    this::enabler,
                    Item::noInputValidation)
            Item.INFO -> {
                val resources = App.instance.resources
                SpinnerTextInputStyle(
                        R.string.tournament_single_final,
                        listOf(true, false),
                        { flag -> resources.getString(if (flag) R.string.yes else R.string.no) },
                        Boolean::toString,
                        this::enabler,
                        Item::noInputValidation)
            }
            else -> TextInputStyle(
                    Item.noClicks,
                    delegate::onImageClick,
                    this::enabler,
                    this::textChecker,
                    this::iconGetter)
        }
    }
}
