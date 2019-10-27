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

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.EventAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseFragment
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.MyEventsViewModel
import com.tunjid.androidx.recyclerview.InteractiveViewHolder
import com.tunjid.androidx.recyclerview.diff.Differentiable

/**
 * Lists [events][Event]
 */

class MyEventsFragment : TeammatesBaseFragment(R.layout.fragment_list_with_refresh),
        EventAdapter.EventAdapterListener {

    private lateinit var items: List<Differentiable>
    private lateinit var myEventsViewModel: MyEventsViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myEventsViewModel = ViewModelProviders.of(requireActivity()).get(MyEventsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        items = myEventsViewModel.getModelList(Event::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultUi(
                toolbarTitle = getString(R.string.attending_events)
        )

        val refreshAction = {
            disposables.add(myEventsViewModel.refresh(Event::class.java)
                    .subscribe(this::onEventsUpdated, defaultErrorHandler::invoke)).let { Unit }
        }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(view.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(view, R.drawable.ic_event_white_24dp, R.string.no_rsvp))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchEvents(false) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(EventAdapter(::items, this))
                .withLinearLayoutManager()
                .build()
    }

    override fun onResume() {
        super.onResume()
        fetchEvents(true)
    }

    override fun onEventClicked(item: Event) {
        navigator.push(EventEditFragment.newInstance(item))
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) = when (incomingFragment) {
        is EventEditFragment ->
            transaction.listDetailTransition(EventEditFragment.ARG_EVENT, incomingFragment)

        else -> super.augmentTransaction(transaction, incomingFragment)
    }

    private fun fetchEvents(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else transientBarDriver.toggleProgress(true)

        disposables.add(myEventsViewModel.getMany(Event::class.java, fetchLatest).subscribe(this::onEventsUpdated, defaultErrorHandler::invoke))
    }

    private fun onEventsUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        transientBarDriver.toggleProgress(false)
    }

    companion object {

        fun newInstance(): MyEventsFragment = MyEventsFragment().apply { arguments = Bundle() }
    }
}
