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

import android.content.Intent
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.EventEditAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.util.ScrollManager
import com.mainstreetcode.teammate.viewmodel.gofers.EventGofer
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer
import com.tunjid.androidbootstrap.view.util.InsetFlags

/**
 * Edits a Team member
 */

class EventEditFragment : HeaderedFragment<Event>(R.layout.fragment_headered),
        EventEditAdapter.EventEditAdapterListener,
        AddressPickerFragment.AddressPicker {

    override lateinit var headeredModel: Event
        private set

    private lateinit var gofer: EventGofer

    override val insetFlags: InsetFlags get() = NO_TOP

    override val showsFab: Boolean get() = !bottomSheetDriver.isBottomSheetShowing && gofer.hasPrivilegedRole()

    private val eventUri: Uri?
        get() = headeredModel.location?.run {
            Uri.Builder()
                    .scheme("https")
                    .authority("www.google.com")
                    .appendPath("maps")
                    .appendPath("dir")
                    .appendPath("")
                    .appendQueryParameter("api", "1")
                    .appendQueryParameter("destination", "$latitude,$longitude")
                    .build()
        }

    override val stableTag: String
        get() {
            val args = arguments
            val model = args!!.getParcelable<Model<*>>(if (args.containsKey(ARG_EVENT)) ARG_EVENT else ARG_GAME)
            return Gofer.tag(super.stableTag, model!!)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        val game = args!!.getParcelable<Game>(ARG_GAME)
        headeredModel = game?.event ?: args.getParcelable(ARG_EVENT)!!

        if (game != null && headeredModel.isEmpty) headeredModel.setName(game)
        gofer = eventViewModel.gofer(headeredModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        defaultUi(
                toolbarTitle =  gofer.getToolbarTitle(this),
                toolBarMenu = R.menu.fragment_event_edit,
                fabShows = false,
                fabIcon = R.drawable.ic_check_white_24dp,
                fabText = if (headeredModel.isEmpty) R.string.event_create else R.string.event_update
        )

        scrollManager = ScrollManager.with<BaseViewHolder<*>>(view.findViewById(R.id.model_list))
                .withRefreshLayout(view.findViewById(R.id.refresh_layout), this::refresh)
                .withAdapter(EventEditAdapter(gofer.items, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .onLayoutManager(this::setSpanSizeLookUp)
                .withGridLayoutManager(2)
                .setHasFixedSize()
                .build()

        scrollManager.recyclerView?.requestFocus()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        if (canEditEvent() || headeredModel.isPublic) menu.findItem(R.id.action_delete)?.isVisible = true

        val item = menu.findItem(R.id.action_rsvp) ?: return
        disposables.add(gofer.rsvpStatus.subscribe({ item.setIcon(it) }, emptyErrorHandler::invoke))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_navigate -> {
            val uri = eventUri
            if (uri == null) transientBarDriver.showSnackBar(getString(R.string.event_no_location))
            else startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        }
        R.id.action_rsvp -> rsvpToEvent().let { true }
        R.id.action_delete -> AlertDialog.Builder(requireContext()).setTitle(getString(R.string.delete_event_prompt))
                .setPositiveButton(R.string.yes) { _, _ -> deleteEvent() }
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .show().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        eventViewModel.clearNotifications(headeredModel)
    }

    override fun gofer(): Gofer<Event> = gofer

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        transientBarDriver.toggleProgress(false)
        scrollManager.onDiff(result)
        viewHolder.bind(headeredModel)
    }

    override fun onPrepComplete() {
        scrollManager.notifyDataSetChanged()
        requireActivity().invalidateOptionsMenu()
        super.onPrepComplete()
    }

    override fun cantGetModel(): Boolean = super.cantGetModel() || gofer.isSettingLocation

    override fun onClick(view: View) {
        if (view.id == R.id.fab) {
            val wasEmpty = headeredModel.isEmpty
            transientBarDriver.toggleProgress(true)
            disposables.add(gofer.save().subscribe({ diffResult ->
                val stringRes = if (wasEmpty) R.string.added_user else R.string.updated_user
                updateUi(fabText = R.string.event_update)
                onModelUpdated(diffResult)
                transientBarDriver.showSnackBar(getString(stringRes, headeredModel.name))
            }, defaultErrorHandler::invoke))
        }
    }

    override fun onImageClick() {
        if (showsFab) onLocationClicked()
    }

    override fun onTeamClicked(item: Team) {
        eventViewModel.onEventTeamChanged(headeredModel, item)
        bottomSheetDriver.hideBottomSheet()

        val index = gofer.items.indexOf(item)
        if (index > -1) scrollManager.notifyItemChanged(index)
    }

    override fun selectTeam() = when {
        headeredModel.gameId.isNotBlank() -> transientBarDriver.showSnackBar(getString(R.string.game_event_team_change))
        gofer.hasPrivilegedRole() -> chooseTeam()
        !gofer.hasRole() -> navigator.show(JoinRequestFragment.joinInstance(headeredModel.team, userViewModel.currentUser)).let { Unit }
        else -> Unit
    }

    override fun onGuestClicked(guest: Guest) {
        val current = userViewModel.currentUser
        if (current == guest.user) rsvpToEvent()
        else navigator.show(GuestViewFragment.newInstance(guest))
    }

    override fun canEditEvent(): Boolean = headeredModel.isEmpty || gofer.hasPrivilegedRole()

    override fun onLocationClicked() {
        gofer.isSettingLocation = true
        pickPlace()
    }

    override fun onAddressPicked(address: Address) {
        gofer.isSettingLocation = false
        disposables.add(gofer.setAddress(address).subscribe(this::onModelUpdated, emptyErrorHandler::invoke))
    }

    private fun rsvpEvent(attending: Boolean) {
        transientBarDriver.toggleProgress(true)
        disposables.add(gofer.rsvpEvent(attending).subscribe(this::onModelUpdated, defaultErrorHandler::invoke))
    }

    private fun deleteEvent() {
        disposables.add(gofer.remove().subscribe(this::onEventDeleted, defaultErrorHandler::invoke))
    }

    private fun onEventDeleted() {
        transientBarDriver.showSnackBar(getString(R.string.deleted_team, headeredModel.name))
        removeEnterExitTransitions()
        requireActivity().onBackPressed()
    }

    private fun rsvpToEvent() {
        activity ?: return

        AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.attend_event))
                .setPositiveButton(R.string.yes) { _, _ -> rsvpEvent(true) }
                .setNegativeButton(R.string.no) { _, _ -> rsvpEvent(false) }
                .show()
    }

    private fun chooseTeam() = bottomSheetDriver.showBottomSheet {
        val teamsFragment = TeamsFragment.newInstance()
        teamsFragment.setTargetFragment(this@EventEditFragment, R.id.request_event_edit_pick)

        menuRes = R.menu.empty
        title = getString(R.string.pick_team)
        fragment = teamsFragment
    }

    private fun setSpanSizeLookUp(layoutManager: RecyclerView.LayoutManager) {
        (layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                    if (gofer.items[position] is Guest) 1 else 2
        }
    }

    companion object {

        internal const val ARG_EVENT = "event"
        private const val ARG_GAME = "game"

        fun newInstance(event: Event): EventEditFragment = EventEditFragment().apply {
            arguments = bundleOf(ARG_EVENT to event)
            setEnterExitTransitions()
        }

        fun newInstance(game: Game): EventEditFragment = newInstance(game.event).apply {
            arguments?.putParcelable(ARG_GAME, game)
        }
    }
}
