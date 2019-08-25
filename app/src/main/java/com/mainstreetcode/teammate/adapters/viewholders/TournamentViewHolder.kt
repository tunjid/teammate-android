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

import androidx.core.view.ViewCompat
import android.view.View
import android.widget.ImageView

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TournamentAdapter
import com.mainstreetcode.teammate.model.Tournament

import com.mainstreetcode.teammate.util.getTransitionName


class TournamentViewHolder(
        itemView: View,
        adapterListener: TournamentAdapter.TournamentAdapterListener
) : ModelCardViewHolder<Tournament, TournamentAdapter.TournamentAdapterListener>(itemView, adapterListener), View.OnClickListener {

    val image: ImageView
        get() = thumbnail

    init {
        itemView.setOnClickListener(this)
    }

    override fun bind(model: Tournament) {
        super.bind(model)
        title.text = model.name
        subtitle.text = model.description

        ViewCompat.setTransitionName(itemView, model.getTransitionName(R.id.fragment_header_background))
        ViewCompat.setTransitionName(thumbnail, model.getTransitionName(R.id.fragment_header_thumbnail))
    }

    override fun onClick(view: View) {
        adapterListener.onTournamentClicked(model)
    }
}
