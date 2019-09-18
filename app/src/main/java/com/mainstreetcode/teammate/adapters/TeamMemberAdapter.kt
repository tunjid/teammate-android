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
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.JoinRequestViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.RoleViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.bind
import com.mainstreetcode.teammate.model.Ad
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.TeamMember
import com.mainstreetcode.teammate.util.CONTENT_AD
import com.mainstreetcode.teammate.util.INSTALL_AD
import com.mainstreetcode.teammate.util.JOIN_REQUEST
import com.mainstreetcode.teammate.util.ROLE
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.tunjid.androidbootstrap.view.util.inflate

/**
 * Adapter for [Team]
 */

class TeamMemberAdapter(
        private val teamModels: List<Differentiable>,
        listener: UserAdapterListener
) : InteractiveAdapter<InteractiveViewHolder<*>, TeamMemberAdapter.UserAdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): InteractiveViewHolder<*> =
            when (viewType) {
                CONTENT_AD -> ContentAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_content_ad), delegate)
                INSTALL_AD -> InstallAdViewHolder(viewGroup.inflate(R.layout.viewholder_grid_install_ad), delegate)
                ROLE -> RoleViewHolder(viewGroup.inflate(R.layout.viewholder_grid_item), delegate)
                else -> JoinRequestViewHolder(viewGroup.inflate(R.layout.viewholder_grid_item), delegate)
            }

    override fun onBindViewHolder(viewHolder: InteractiveViewHolder<*>, position: Int) {
        val item = teamModels[position]

        if (item is Ad<*>) (viewHolder as AdViewHolder<*>).bind(item)
        if (item !is TeamMember) return

        when (val wrapped = item.wrappedModel) {
            is Role -> (viewHolder as RoleViewHolder).bind(wrapped)
            is JoinRequest -> (viewHolder as JoinRequestViewHolder).bind(wrapped)
        }
    }

    override fun getItemId(position: Int): Long = teamModels[position].hashCode().toLong()

    override fun getItemCount(): Int = teamModels.size

    override fun getItemViewType(position: Int): Int {
        val item = teamModels[position]
        if (item is Ad<*>) return item.type
        if (item !is TeamMember) return JOIN_REQUEST

        val wrapped = item.wrappedModel
        return if (wrapped is Role) ROLE else JOIN_REQUEST
    }

    interface UserAdapterListener {
        fun onRoleClicked(role: Role)

        fun onJoinRequestClicked(request: JoinRequest)
    }

}
