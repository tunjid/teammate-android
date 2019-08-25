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
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.Team
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Adapter for [Team]
 */

open class CompetitorAdapter(
        private val items: List<Differentiable>,
        listener: AdapterListener
) : InteractiveAdapter<CompetitorViewHolder, CompetitorAdapter.AdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CompetitorViewHolder =
            CompetitorViewHolder(getItemView(R.layout.viewholder_competitor, viewGroup), adapterListener)

    override fun onBindViewHolder(viewHolder: CompetitorViewHolder, position: Int) {
        val identifiable = items[position]
        if (identifiable is Competitor) viewHolder.bind(identifiable)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    override fun getItemViewType(position: Int): Int = Item.COMPETITOR

    interface AdapterListener : InteractiveAdapter.AdapterListener {
        fun onCompetitorClicked(competitor: Competitor)

        companion object {
            fun asSAM(function: (Competitor) -> Unit) = object : CompetitorAdapter.AdapterListener {
                override fun onCompetitorClicked(competitor: Competitor) = function.invoke(competitor)
            }
        }
    }
}