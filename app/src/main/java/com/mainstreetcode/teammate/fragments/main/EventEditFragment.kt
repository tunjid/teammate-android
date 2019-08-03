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
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.EventEditAdapter
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
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

class EventEditFragment : HeaderedFragment<Event>(), EventEditAdapter.EventEditAdapterListener, AddressPickerFragment.AddressPicker {

    override lateinit var headeredModel: Event
        private set

    private lateinit var gofer: EventGofer

    override val fabStringResource: Int
        @StringRes
        get() = if (headeredModel.isEmpty) R.string.event_create else R.string.event_update

    override val fabIconResource: Int
        @DrawableRes
        get() = R.drawable.ic_check_white_24dp

    override val toolbarMenu: Int
        get() = R.menu.fragment_event_edit

    override val toolbarTitle: CharSequence
        get() = gofer.getToolbarTitle(this)

    private val eventUri: Uri?
        get() {
            val latLng = headeredModel.location ?: return null

            return Uri.Builder()
                    .scheme("https")
                    .authority("www.google.com")
                    .appendPath("maps")
                    .appendPath("dir")
                    .appendPath("")
                    .appendQueryParameter("api", "1")
                    .appendQueryParameter("destination", latLng.latitude.toString() + "," + latLng.longitude)
                    .build()
        }

    override fun getStableTag(): String {
        val args = arguments
        val model = args!!.getParcelable<Model<*>>(if (args.containsKey(ARG_EVENT)) ARG_EVENT else ARG_GAME)
        return Gofer.tag(super.getStableTag(), model!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        val game = args!!.getParcelable<Game>(ARG_GAME)
        headeredModel = if (game != null) game.event else args.getParcelable(ARG_EVENT)!!

        if (game != null && headeredModel.isEmpty) headeredModel.setName(game)
        gofer = eventViewModel.gofer(headeredModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_headered, container, false)

        scrollManager = ScrollManager.with<BaseViewHolder<*>>(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout)) { this.refresh() }
                .withAdapter(EventEditAdapter(gofer.items, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .onLayoutManager(this::setSpanSizeLookUp)
                .withGridLayoutManager(2)
                .setHasFixedSize()
                .build()

        scrollManager.recyclerView.requestFocus()
        return rootView
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.action_rsvp) ?: return

        if (canEditEvent() || headeredModel.isPublic) menu.findItem(R.id.action_delete).isVisible = true

        disposables.add(gofer.rsvpStatus.subscribe({ item.setIcon(it) }, emptyErrorHandler::accept))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_navigate -> {
                val uri = eventUri
                if (uri == null) {
                    showSnackbar(getString(R.string.event_no_location))
                    return true
                }

                val maps = Intent(Intent.ACTION_VIEW, uri)
                startActivity(maps)
                return true
            }
            R.id.action_rsvp -> {
                rsvpToEvent()
                return true
            }
            R.id.action_delete -> {
                val context = context ?: return true
                AlertDialog.Builder(context).setTitle(getString(R.string.delete_event_prompt))
                        .setPositiveButton(R.string.yes) { _, _ -> deleteEvent() }
                        .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                        .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        eventViewModel.clearNotifications(headeredModel)
    }

    override fun insetFlags(): InsetFlags = NO_TOP

    override fun showsFab(): Boolean = !isBottomSheetShowing && gofer.hasPrivilegedRole()

    override fun gofer(): Gofer<Event> = gofer

    override fun onModelUpdated(result: DiffUtil.DiffResult) {
        toggleProgress(false)
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
            toggleProgress(true)
            disposables.add(gofer.save().subscribe({ diffResult ->
                val stringRes = if (wasEmpty) R.string.added_user else R.string.updated_user
                onModelUpdated(diffResult)
                showSnackbar(getString(stringRes, headeredModel.name))
            }, defaultErrorHandler::accept))
        }
    }

    override fun onImageClick() {
        if (showsFab()) onLocationClicked()
    }

    override fun onTeamClicked(item: Team) {
        eventViewModel.onEventTeamChanged(headeredModel, item)
        hideBottomSheet()

        val index = gofer.items.indexOf(item)
        if (index > -1) scrollManager.notifyItemChanged(index)
    }

    override fun selectTeam() {
        when {
            !TextUtils.isEmpty(headeredModel.gameId) -> showSnackbar(getString(R.string.game_event_team_change))
            gofer.hasPrivilegedRole() -> chooseTeam()
            !gofer.hasRole() -> showFragment(JoinRequestFragment.joinInstance(headeredModel.team, userViewModel.currentUser))
        }
    }

    override fun onGuestClicked(guest: Guest) {
        val current = userViewModel.currentUser
        if (current == guest.user) rsvpToEvent()
        else showFragment(GuestViewFragment.newInstance(guest))
    }

    override fun canEditEvent(): Boolean = headeredModel.isEmpty || gofer.hasPrivilegedRole()

    override fun onLocationClicked() {
        gofer.isSettingLocation = true
        pickPlace()
    }

    override fun onAddressPicked(address: Address) {
        gofer.isSettingLocation = false
        disposables.add(gofer.setAddress(address).subscribe(this::onModelUpdated, emptyErrorHandler::accept))
    }

    private fun rsvpEvent(attending: Boolean) {
        toggleProgress(true)
        disposables.add(gofer.rsvpEvent(attending).subscribe(this::onModelUpdated, defaultErrorHandler::accept))
    }

    private fun deleteEvent() {
        disposables.add(gofer.remove().subscribe(this::onEventDeleted, defaultErrorHandler::accept))
    }

    private fun onEventDeleted() {
        showSnackbar(getString(R.string.deleted_team, headeredModel.name))
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

    private fun chooseTeam() {
        val teamsFragment = TeamsFragment.newInstance()
        teamsFragment.setTargetFragment(this, R.id.request_event_edit_pick)

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setTitle(getString(R.string.pick_team))
                .setFragment(teamsFragment)
                .build())
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

        fun newInstance(event: Event): EventEditFragment {
            val fragment = EventEditFragment()
            val args = Bundle()

            args.putParcelable(ARG_EVENT, event)
            fragment.arguments = args
            fragment.setEnterExitTransitions()

            return fragment
        }

        fun newInstance(game: Game): EventEditFragment {
            val fragment = newInstance(game.event)
            fragment.arguments!!.putParcelable(ARG_GAME, game)
            return fragment
        }
    }
}
