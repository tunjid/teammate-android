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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.EventAdapter
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.util.getTransitionName


class EventViewHolder(
        itemView: View,
        delegate: EventAdapter.EventAdapterListener
) : ModelCardViewHolder<Event, EventAdapter.EventAdapterListener>(itemView, delegate), View.OnClickListener {

    private val eventLocation: TextView = itemView.findViewById(R.id.location)

    val image: ImageView
        get() = thumbnail

    init {
        itemView.setOnClickListener(this)
    }

    override fun bind(model: Event) {
        super.bind(model)
        title.text = model.name
        subtitle.text = model.time
        eventLocation.text = model.locationName

        ViewCompat.setTransitionName(itemView, model.getTransitionName(R.id.fragment_header_background))
        ViewCompat.setTransitionName(thumbnail, model.getTransitionName(R.id.fragment_header_thumbnail))
    }

    override fun onClick(view: View) {
        delegate?.onEventClicked(model)
    }
}
