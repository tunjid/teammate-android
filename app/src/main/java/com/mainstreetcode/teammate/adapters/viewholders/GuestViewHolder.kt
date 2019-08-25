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
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.EventEditAdapter
import com.mainstreetcode.teammate.model.Guest

class GuestViewHolder(
        itemView: View,
        listener: EventEditAdapter.EventEditAdapterListener
) : ModelCardViewHolder<Guest, EventEditAdapter.EventEditAdapterListener>(itemView, listener), View.OnClickListener {

    init {
        itemView.setOnClickListener(this)
    }

    override fun bind(model: Guest) {
        super.bind(model)

        val context = itemView.context

        title.text = model.user.firstName
        subtitle.text = context.getString(if (model.isAttending) R.string.event_attending else R.string.event_not_attending)
    }

    override fun onClick(view: View) {
        adapterListener.onGuestClicked(model)
    }
}
