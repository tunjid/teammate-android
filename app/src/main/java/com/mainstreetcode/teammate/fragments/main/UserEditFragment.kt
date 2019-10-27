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
import com.mainstreetcode.teammate.adapters.UserEditAdapter
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.mainstreetcode.teammate.viewmodel.gofers.UserGofer
import com.tunjid.androidx.view.util.InsetFlags

/**
 * Edits a Team member
 */

class UserEditFragment : HeaderedFragment<User>(R.layout.fragment_headered),
        UserEditAdapter.AdapterListener {

    override lateinit var headeredModel: User
        private set

    private lateinit var gofer: UserGofer

    private val toolbarTitle: CharSequence get() = getString(if (canEdit()) R.string.user_edit else R.string.user_info)

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = canEdit()

    override val stableTag: String get() = Gofer.tag(super.stableTag, arguments!!.getParcelable(ARG_USER)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_USER)!!
        gofer = userViewModel.gofer(headeredModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = toolbarTitle,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabText = R.string.user_update,
                fabShows = showsFab
        )
        scrollManager = ScrollManager.with<InputViewHolder>(view.findViewById(R.id.model_list))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(UserEditAdapter(gofer.items, this))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build().apply { recyclerView?.requestFocus() }
    }

    override fun gofer(): Gofer<User> = gofer

    override fun canEdit(): Boolean = userViewModel.currentUser == headeredModel

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        updateUi(toolbarTitle = toolbarTitle)
        viewHolder?.bind(headeredModel)
        scrollManager.onDiff(result)
        transientBarDriver.toggleProgress(false)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                transientBarDriver.toggleProgress(true)
                disposables.add(gofer.save().subscribe({ result ->
                    transientBarDriver.showSnackBar(getString(R.string.updated_user, headeredModel.firstName))
                    transientBarDriver.toggleProgress(false)
                    viewHolder?.bind(headeredModel)
                    scrollManager.onDiff(result)
                }, defaultErrorHandler::invoke))
            }
        }
    }

    companion object {

        const val ARG_USER = "user"

        fun newInstance(user: User): UserEditFragment = UserEditFragment().apply { arguments = bundleOf(ARG_USER to user) }
    }
}
