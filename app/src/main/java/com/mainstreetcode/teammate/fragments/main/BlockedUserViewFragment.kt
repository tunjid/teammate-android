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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.BlockedUserViewAdapter
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.BlockedUserGofer
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.tunjid.androidbootstrap.view.util.InsetFlags

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil

class BlockedUserViewFragment : HeaderedFragment<BlockedUser>() {

    override lateinit var headeredModel: BlockedUser
        private set

    private lateinit var gofer: BlockedUserGofer

    override val fabStringResource: Int
        @StringRes
        get() = R.string.unblock_user

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_unlock_white

    override val toolbarTitle: CharSequence
        get() = getString(R.string.blocked_user)

    override fun getStableTag(): String =
            Gofer.tag(super.getStableTag(), arguments!!.getParcelable(ARG_BLOCKED_USER)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_BLOCKED_USER)!!
        gofer = blockedUserViewModel.gofer(headeredModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_headered, container, false)

        scrollManager = ScrollManager.with<InputViewHolder<*>>(rootView.findViewById(R.id.model_list))
                .withAdapter(BlockedUserViewAdapter(gofer.items))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build()

        scrollManager.recyclerView.requestFocus()

        return rootView
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fab)
            disposables.add(gofer.delete().subscribe(this::onUserUnblocked, defaultErrorHandler::accept))
    }

    override fun showsFab(): Boolean = gofer.hasPrivilegedRole()

    override fun onImageClick() {
        showSnackbar(getString(R.string.no_permission))
    }

    override fun insetFlags(): InsetFlags = NO_TOP

    override fun gofer(): Gofer<BlockedUser> = gofer

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
    }

    override fun onPrepComplete() {
        requireActivity().invalidateOptionsMenu()
        super.onPrepComplete()
    }

    private fun onUserUnblocked() {
        showSnackbar(getString(R.string.unblocked_user, headeredModel.user.firstName))
        requireActivity().onBackPressed()
    }

    companion object {

        internal const val ARG_BLOCKED_USER = "blockedUser"

        fun newInstance(guest: BlockedUser): BlockedUserViewFragment {
            val fragment = BlockedUserViewFragment()
            val args = Bundle()

            args.putParcelable(ARG_BLOCKED_USER, guest)
            fragment.arguments = args
            return fragment
        }
    }
}
