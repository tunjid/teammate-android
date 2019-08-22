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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.EventAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.adapters.viewholders.EventViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.Event
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.MyEventsViewModel
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil

import com.mainstreetcode.teammate.util.getTransitionName

/**
 * Lists [events][Event]
 */

class MyEventsFragment : MainActivityFragment(), EventAdapter.EventAdapterListener {

    private lateinit var items: List<Differentiable>
    private lateinit var myEventsViewModel: MyEventsViewModel

    override val toolbarTitle: CharSequence get() = getString(R.string.attending_events)

    override val showsFab: Boolean get() = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myEventsViewModel = ViewModelProviders.of(requireActivity()).get(MyEventsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        items = myEventsViewModel.getModelList(Event::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_list_with_refresh, container, false)

        val refreshAction = Runnable{ disposables.add(myEventsViewModel.refresh(Event::class.java).subscribe(this::onEventsUpdated, defaultErrorHandler::invoke)) }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(root.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(root, R.drawable.ic_event_white_24dp, R.string.no_rsvp))
                .withRefreshLayout(root.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchEvents(false) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(EventAdapter(items, this))
                .withLinearLayoutManager()
                .build()

        return root
    }

    override fun onResume() {
        super.onResume()
        fetchEvents(true)
    }

    override fun onEventClicked(item: Event) {
        showFragment(EventEditFragment.newInstance(item))
    }

    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? {
        val superResult = super.provideFragmentTransaction(fragmentTo)

        if (fragmentTo.stableTag.contains(EventEditFragment::class.java.simpleName)) {
            val args = fragmentTo.arguments ?: return superResult

            val event = args.getParcelable<Event>(EventEditFragment.ARG_EVENT) ?: return superResult

            val viewHolder = scrollManager.findViewHolderForItemId(event.hashCode().toLong()) as? EventViewHolder
                    ?: return superResult

            return beginTransaction()
                    .addSharedElement(viewHolder.itemView, event.getTransitionName(R.id.fragment_header_background))
                    .addSharedElement(viewHolder.image, event.getTransitionName(R.id.fragment_header_thumbnail))

        }
        return superResult
    }

    private fun fetchEvents(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else toggleProgress(true)

        disposables.add(myEventsViewModel.getMany(Event::class.java, fetchLatest).subscribe(this::onEventsUpdated, defaultErrorHandler::invoke))
    }

    private fun onEventsUpdated(result: DiffUtil.DiffResult) {
        scrollManager.onDiff(result)
        toggleProgress(false)
    }

    companion object {

        fun newInstance(): MyEventsFragment = MyEventsFragment().apply { arguments = Bundle() }
    }
}
