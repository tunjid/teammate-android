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

package com.mainstreetcode.teammate.adapters.viewholders;

import android.content.Context;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.EventEditAdapter;
import com.mainstreetcode.teammate.model.Guest;

public class GuestViewHolder extends ModelCardViewHolder<Guest, EventEditAdapter.EventEditAdapterListener>
        implements View.OnClickListener {

    public GuestViewHolder(View itemView, EventEditAdapter.EventEditAdapterListener listener) {
        super(itemView, listener);
        itemView.setOnClickListener(this);
    }

    @Override
    public void bind(Guest model) {
        super.bind(model);

        Context context = itemView.getContext();

        title.setText(model.getUser().getFirstName());
        subtitle.setText(context.getString(model.isAttending() ? R.string.event_attending : R.string.event_not_attending));
    }

    @Override
    public void onClick(View view) {
        adapterListener.onGuestClicked(model);
    }
}
