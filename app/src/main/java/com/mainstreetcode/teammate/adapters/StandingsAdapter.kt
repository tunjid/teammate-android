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
import com.mainstreetcode.teammate.adapters.viewholders.StandingRowViewHolder
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Row
import com.mainstreetcode.teammate.util.SyncedScrollView
import com.tunjid.androidx.recyclerview.InteractiveAdapter
import com.tunjid.androidx.view.util.inflate

/**
 * Adapter for [Event]
 */

class StandingsAdapter(
        private val items: List<Row>,
        listener: AdapterListener
) : InteractiveAdapter<StandingRowViewHolder, StandingsAdapter.AdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): StandingRowViewHolder =
            StandingRowViewHolder(viewGroup.inflate(R.layout.viewholder_standings_row), delegate)

    override fun onBindViewHolder(viewHolder: StandingRowViewHolder, position: Int) {
        val row = items[position]
        viewHolder.bind(row)
        viewHolder.bindColumns(row.columns)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    interface AdapterListener {
        fun addScrollNotifier(notifier: SyncedScrollView)

        fun onCompetitorClicked(competitor: Competitor)
    }

}
