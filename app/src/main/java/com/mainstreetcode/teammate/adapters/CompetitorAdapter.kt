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
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.bind
import com.mainstreetcode.teammate.model.Ad
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Item.Companion.COMPETITOR
import com.mainstreetcode.teammate.util.CONTENT_AD
import com.mainstreetcode.teammate.util.INSTALL_AD
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.diff.Differentiable
import com.tunjid.androidx.view.util.inflate

fun competitorAdapter(
        modelSource: () -> List<Differentiable>,
        listener: (Competitor) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder> = adapterOf(
        itemsSource = modelSource,
        viewHolderCreator = { viewGroup: ViewGroup, viewType: Int ->
            when (viewType) {
                CONTENT_AD -> ContentAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_content_ad), listener)
                INSTALL_AD -> InstallAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_install_ad), listener)
                else -> CompetitorViewHolder(viewGroup.inflate(R.layout.viewholder_event), listener)
            }
        },
        viewHolderBinder = { holder, item, _ ->
            when {
                item is Ad<*> && holder is AdViewHolder<*> -> holder.bind(item)
                item is Competitor && holder is CompetitorViewHolder -> holder.bind(item)
            }
        },
        itemIdFunction = { it.hashCode().toLong() },
        viewTypeFunction = { (it as? Ad<*>)?.type ?: COMPETITOR }
)