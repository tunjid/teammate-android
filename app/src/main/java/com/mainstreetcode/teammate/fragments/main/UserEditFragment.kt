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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import com.tunjid.androidbootstrap.view.util.InsetFlags

/**
 * Edits a Team member
 */

class UserEditFragment : HeaderedFragment<User>(), UserEditAdapter.AdapterListener {

    override lateinit var headeredModel: User
        private set

    private lateinit var gofer: UserGofer

    override val fabStringResource: Int @StringRes get() = R.string.user_update

    override val fabIconResource: Int @DrawableRes get() = R.drawable.ic_check_white_24dp

    override val toolbarTitle: CharSequence get() = getString(if (canEdit()) R.string.user_edit else R.string.user_info)

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = canEdit()

    override fun getStableTag(): String =
            Gofer.tag(super.getStableTag(), arguments!!.getParcelable(ARG_USER)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headeredModel = arguments!!.getParcelable(ARG_USER)!!
        gofer = userViewModel.gofer(headeredModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_headered, container, false)

        scrollManager = ScrollManager.with<InputViewHolder<*>>(root.findViewById(R.id.model_list))
                .withRefreshLayout(root.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(UserEditAdapter(gofer.items, this))
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build()

        scrollManager.recyclerView.requestFocus()

        return root
    }

    override fun gofer(): Gofer<User> = gofer

    override fun canEdit(): Boolean = userViewModel.currentUser == headeredModel

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        viewHolder.bind(headeredModel)
        scrollManager.onDiff(result)
        toggleProgress(false)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                toggleProgress(true)
                disposables.add(gofer.save().subscribe({ result ->
                    showSnackbar(getString(R.string.updated_user, headeredModel.firstName))
                    toggleProgress(false)
                    viewHolder.bind(headeredModel)
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
