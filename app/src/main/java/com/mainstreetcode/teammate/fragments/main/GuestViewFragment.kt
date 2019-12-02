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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.guestAdapter
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.mainstreetcode.teammate.viewmodel.gofers.GuestGofer
import com.tunjid.androidx.view.util.InsetFlags

class GuestViewFragment : HeaderedFragment<Guest>(R.layout.fragment_headered) {

    override lateinit var headeredModel: Guest
        private set

    private lateinit var gofer: GuestGofer

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = false

    override val stableTag: String get() = Gofer.tag(super.stableTag, arguments!!.getParcelable(ARG_GUEST)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_GUEST)!!
        gofer = eventViewModel.gofer(headeredModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.event_guest),
                toolBarMenu = R.menu.fragment_guest_view,
                fabShows = showsFab
        )

        scrollManager = ScrollManager.with<InputViewHolder>(view.findViewById(R.id.model_list))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(guestAdapter(gofer::items))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build()

        scrollManager.recyclerView?.requestFocus()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_block)?.isVisible = gofer.canBlockUser()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when {
        item.itemId == R.id.action_block -> blockUser(headeredModel.user, headeredModel.event.team).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onImageClick() = transientBarDriver.showSnackBar(getString(R.string.no_permission))

    override fun gofer(): Gofer<Guest> = gofer

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        viewHolder?.bind(headeredModel)
        scrollManager.onDiff(result)
        transientBarDriver.toggleProgress(false)
    }

    override fun onPrepComplete() {
        updateUi(toolbarInvalidated = true)
        super.onPrepComplete()
    }

    companion object {

        private const val ARG_GUEST = "guest"

        fun newInstance(guest: Guest): GuestViewFragment = GuestViewFragment().apply { arguments = bundleOf(ARG_GUEST to guest) }
    }
}
