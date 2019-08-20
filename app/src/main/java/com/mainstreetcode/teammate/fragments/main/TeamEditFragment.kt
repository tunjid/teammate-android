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

package com.mainstreetcode.teammate.fragments.main

import android.location.Address
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamEditAdapter
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.mainstreetcode.teammate.viewmodel.gofers.TeamGofer
import com.mainstreetcode.teammate.viewmodel.gofers.TeamHostingGofer
import com.tunjid.androidbootstrap.view.util.InsetFlags

/**
 * Creates, edits or lets a [com.mainstreetcode.teammate.model.User] join a [Team]
 */

class TeamEditFragment : HeaderedFragment<Team>(), AddressPickerFragment.AddressPicker, TeamEditAdapter.TeamEditAdapterListener {

    override lateinit var headeredModel: Team
        private set

    private lateinit var gofer: TeamGofer

    override val fabStringResource: Int @StringRes get() = if (headeredModel.isEmpty) R.string.team_create else R.string.team_update

    override val fabIconResource: Int @DrawableRes get() = R.drawable.ic_check_white_24dp

    override val toolbarTitle: CharSequence get() = gofer.getToolbarTitle(this)

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = gofer.canEditTeam()

    override fun getStableTag(): String =
            Gofer.tag(super.getStableTag(), arguments!!.getParcelable(ARG_TEAM)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        headeredModel = arguments!!.getParcelable(ARG_TEAM)!!
        gofer = teamViewModel.gofer(headeredModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_headered, container, false)

        scrollManager = ScrollManager.with<InputViewHolder<*>>(root.findViewById(R.id.model_list))
                .withRefreshLayout(root.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(TeamEditAdapter(gofer.items, this))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build()

        scrollManager.recyclerView.requestFocus()
        return root
    }

    override fun onClick(view: View) {
        if (view.id != R.id.fab) return

        val wasEmpty = headeredModel.isEmpty
        toggleProgress(true)
        disposables.add(gofer.save()
                .subscribe({ result ->
                    val message = if (wasEmpty) getString(R.string.created_team, headeredModel.name) else getString(R.string.updated_team)
                    onModelUpdated(result)
                    showSnackbar(message)
                }, defaultErrorHandler::invoke))
    }

    override fun gofer(): TeamHostingGofer<Team> = gofer

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        viewHolder.bind(headeredModel)
        scrollManager.onDiff(result)
        toggleProgress(false)
    }

    override fun onPrepComplete() {
        scrollManager.notifyDataSetChanged()
        togglePersistentUi()
        super.onPrepComplete()
    }

    override fun cantGetModel(): Boolean = super.cantGetModel() || gofer.isSettingAddress

    override fun onAddressClicked() {
        gofer.isSettingAddress = true
        pickPlace()
    }

    override fun onAddressPicked(address: Address) {
        gofer.isSettingAddress = false
        disposables.add(gofer.setAddress(address).subscribe(this::onModelUpdated, emptyErrorHandler::invoke))
        toggleProgress(false)
    }

    override fun canEditFields(): Boolean = gofer.canEditTeam()

    companion object {

        private const val ARG_TEAM = "team"

        internal fun newCreateInstance(): TeamEditFragment = newInstance(Team.empty())

        internal fun newEditInstance(team: Team): TeamEditFragment = newInstance(team)

        private fun newInstance(team: Team): TeamEditFragment = TeamEditFragment().apply { arguments = bundleOf(ARG_TEAM to team) }
    }
}
