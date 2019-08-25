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

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.AutoCompleteViewHolder
import com.mainstreetcode.teammate.baseclasses.BaseAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter

import com.mainstreetcode.teammate.util.ITEM

class AutoCompleteAdapter(
        private val predictions: List<AutocompletePrediction>,
        listener: AdapterListener
) : BaseAdapter<AutoCompleteViewHolder, AutoCompleteAdapter.AdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): AutoCompleteViewHolder =
            AutoCompleteViewHolder(getItemView(R.layout.viewholder_auto_complete, viewGroup), adapterListener)

    override fun onBindViewHolder(holder: AutoCompleteViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bind(predictions[position])
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : InteractiveAdapter.AdapterListener> updateListener(viewHolder: BaseViewHolder<S>): S =
            adapterListener as S

    override fun getItemCount(): Int = predictions.size

    override fun getItemId(position: Int): Long = predictions[position].hashCode().toLong()

    override fun getItemViewType(position: Int): Int = ITEM

    interface AdapterListener : InteractiveAdapter.AdapterListener {
        fun onPredictionClicked(prediction: AutocompletePrediction)
    }

}
