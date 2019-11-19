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
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.blockedUserViewAdapter
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.BlockedUserGofer
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.tunjid.androidx.view.util.InsetFlags

class BlockedUserViewFragment : HeaderedFragment<BlockedUser>(R.layout.fragment_headered) {

    override lateinit var headeredModel: BlockedUser
        private set

    private lateinit var gofer: BlockedUserGofer

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = gofer.hasPrivilegedRole()

    override val stableTag
        get() = Gofer.tag(super.stableTag, arguments!!.getParcelable(ARG_BLOCKED_USER)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_BLOCKED_USER)!!
        gofer = blockedUserViewModel.gofer(headeredModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.blocked_user),
                fabShows = showsFab,
                fabIcon = R.drawable.ic_unlock_white,
                fabText = R.string.unblock_user
        )

        scrollManager = ScrollManager.with<InputViewHolder>(view.findViewById(R.id.model_list))
                .withAdapter(blockedUserViewAdapter(gofer::items))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build()

        scrollManager.recyclerView?.requestFocus()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fab)
            disposables.add(gofer.delete().subscribe(this::onUserUnblocked, defaultErrorHandler::invoke))
    }

    override fun onImageClick() = transientBarDriver.showSnackBar(getString(R.string.no_permission))

    override fun gofer(): Gofer<BlockedUser> = gofer

    override fun onModelUpdated(result: DiffUtil.DiffResult) = scrollManager.onDiff(result)

    override fun onPrepComplete() {
        updateUi(toolbarInvalidated = true)
        super.onPrepComplete()
    }

    private fun onUserUnblocked() {
        transientBarDriver.showSnackBar(getString(R.string.unblocked_user, headeredModel.user.firstName))
        requireActivity().onBackPressed()
    }

    companion object {

        internal const val ARG_BLOCKED_USER = "blockedUser"

        fun newInstance(guest: BlockedUser): BlockedUserViewFragment = BlockedUserViewFragment().apply { arguments = bundleOf(ARG_BLOCKED_USER to guest) }
    }
}
