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

package com.mainstreetcode.teammate.adapters.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Stat


class StatViewHolder(
        itemView: View,
        val delegate: (Stat) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private lateinit var model: Stat

    private val statTime: Array<TextView> = arrayOf(itemView.findViewById(R.id.home_stat_time), itemView.findViewById(R.id.away_stat_time))
    private val statUser: Array<TextView> = arrayOf(itemView.findViewById(R.id.home_stat_user), itemView.findViewById(R.id.away_stat_user))
    private val statName: Array<TextView> = arrayOf(itemView.findViewById(R.id.home_stat_name), itemView.findViewById(R.id.away_stat_name))
    private val statEmoji: Array<TextView> = arrayOf(itemView.findViewById(R.id.home_stat_emoji), itemView.findViewById(R.id.away_stat_emoji))

    init {
        itemView.setOnClickListener { delegate.invoke(model) }
    }

    fun bind(model: Stat) {
        this.model = model
        val statType = model.statType

        setText(statTime, model.time.toString())
        setText(statUser, model.user.firstName)
        setText(statName, statType.name)
        setText(statEmoji, statType.emoji)
    }

    private fun setText(textViewPair: Array<TextView>, text: CharSequence) {
        val isHome = model.isHome
        val home = if (isHome) 0 else 1
        val away = if (isHome) 1 else 0

        textViewPair[home].text = text
        textViewPair[away].text = ""
    }
}
