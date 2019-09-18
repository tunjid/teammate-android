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
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder
import com.mainstreetcode.teammate.util.CONTENT_AD
import com.mainstreetcode.teammate.util.INSTALL_AD
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.tunjid.androidbootstrap.view.util.inflate
import com.tunjid.androidbootstrap.view.util.marginLayoutParams

class TeamSearchAdapter(items: List<Differentiable>, listener: AdapterListener) : TeamAdapter(items, listener) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): InteractiveViewHolder<*> = when (viewType) {
        CONTENT_AD -> ContentAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_content_ad), delegate)
        INSTALL_AD -> InstallAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_install_ad), delegate)
        else -> getTeamViewHolder(viewGroup)
    }

    private fun getTeamViewHolder(viewGroup: ViewGroup): TeamViewHolder {
        val itemView = viewGroup.inflate(R.layout.viewholder_list_item)
        val resources = itemView.resources
        val params = itemView.marginLayoutParams
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.half_margin)
        params.leftMargin = params.rightMargin
        return TeamViewHolder(itemView, delegate)
    }
}
