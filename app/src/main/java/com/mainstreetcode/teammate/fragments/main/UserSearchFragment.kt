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
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.SearchView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.UserAdapter
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.InstantSearch
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidx.recyclerview.InteractiveViewHolder

/**
 * Searches for users
 */

class UserSearchFragment : TeammatesBaseFragment(R.layout.fragment_user_search),
        SearchView.OnQueryTextListener,
        UserAdapter.AdapterListener {

    private var searchView: SearchView? = null
    private lateinit var instantSearch: InstantSearch<String, User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instantSearch = userViewModel.instantSearch()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarShows = false
        )
        searchView = view.findViewById(R.id.searchView)

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(view.findViewById(R.id.list_layout))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(UserAdapter(instantSearch.currentItems, this))
                .withGridLayoutManager(2)
                .build()

        searchView?.setOnQueryTextListener(this)
        searchView?.setIconifiedByDefault(false)
        searchView?.isIconified = false

        if (targetRequestCode != 0) {
            val items = instantSearch.currentItems
            items.clear()
            items.apply {
                add(userViewModel.currentUser)
                addAll(teamMemberViewModel.allUsers)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        subScribeToSearch()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView?.clearFocus()
        searchView = null
    }

    override fun onUserClicked(item: User) {
        val target = targetFragment
        val canPick = target is UserAdapter.AdapterListener

        if (canPick) (target as UserAdapter.AdapterListener).onUserClicked(item)
        else navigator.show(UserEditFragment.newInstance(item))
    }

    override fun onQueryTextSubmit(s: String): Boolean = false

    override fun onQueryTextChange(queryText: String): Boolean {
        if (view == null || TextUtils.isEmpty(queryText)) return true
        instantSearch.postSearch(queryText)
        return true
    }

    private fun subScribeToSearch() {
        disposables.add(instantSearch.subscribe()
                .subscribe(scrollManager::onDiff, defaultErrorHandler::invoke))
    }

    companion object {
        fun newInstance(): UserSearchFragment = UserSearchFragment().apply { arguments = Bundle() }
    }
}
