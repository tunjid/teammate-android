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

import android.text.TextUtils
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.StandingsAdapter
import com.mainstreetcode.teammate.model.Row
import com.mainstreetcode.teammate.util.SyncedScrollView
import com.squareup.picasso.Picasso
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder


class StandingRowViewHolder(
        itemView: View,
        adapterListener: StandingsAdapter.AdapterListener
) : InteractiveViewHolder<StandingsAdapter.AdapterListener>(itemView, adapterListener) {

    private var row: Row? = null

    var title: TextView = itemView.findViewById(R.id.item_title)
    var position: TextView = itemView.findViewById(R.id.item_position)
    var thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
    private val columns: LinearLayout = itemView.findViewById(R.id.item_row)
    private val scrollView: SyncedScrollView = itemView.findViewById(R.id.synced_scrollview)

    init {
        val listener = View.OnClickListener { row?.let { adapterListener.onCompetitorClicked(it.competitor) } }

        title.setOnClickListener(listener)
        position.setOnClickListener(listener)
        thumbnail.setOnClickListener(listener)

        adapterListener.addScrollNotifier(scrollView)
    }

    fun bind(model: Row) {
        this.row = model
        title.text = model.name

        val adapterPosition = adapterPosition
        position.text = (adapterPosition + 1).toString()
        scrollView.isHorizontalScrollBarEnabled = adapterPosition < 0


        val imageUrl = model.imageUrl

        if (TextUtils.isEmpty(imageUrl)) thumbnail.setImageResource(R.color.dark_grey)
        else Picasso.get().load(imageUrl).fit().centerCrop().into(thumbnail)
    }

    fun bindColumns(columns: List<String>) {
        val count = columns.size
        this.columns.weightSum = count.toFloat()
        for (i in 0 until count) getItem(i).text = columns[i]
    }

    private fun getItem(position: Int): TextView {
        val max = columns.childCount - 1
        val margin = itemView.resources.getDimensionPixelSize(R.dimen.double_margin)
        if (position <= max) return columns.getChildAt(position) as TextView

        val textView = TextView(itemView.context)
        textView.gravity = CENTER

        val params = LinearLayout.LayoutParams(margin, MATCH_PARENT)
        columns.addView(textView, params)

        return textView
    }
}
