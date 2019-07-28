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

package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.EventEditAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.EventGofer;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Edits a Team member
 */

public class EventEditFragment extends HeaderedFragment<Event>
        implements
        EventEditAdapter.EventEditAdapterListener,
        AddressPickerFragment.AddressPicker {

    static final String ARG_EVENT = "event";
    private static final String ARG_GAME = "game";

    private Event event;
    private EventGofer gofer;

    public static EventEditFragment newInstance(Event event) {
        EventEditFragment fragment = new EventEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_EVENT, event);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    public static EventEditFragment newInstance(Game game) {
        EventEditFragment fragment = newInstance(game.getEvent());
        fragment.getArguments().putParcelable(ARG_GAME, game);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        Bundle args = getArguments();
        Model model = args.getParcelable(args.containsKey(ARG_EVENT) ? ARG_EVENT : ARG_GAME);
        return Gofer.tag(super.getStableTag(), model);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        Game game = args.getParcelable(ARG_GAME);
        event = game != null ? game.getEvent() : args.getParcelable(ARG_EVENT);

        assert event != null;
        if (game != null && event.isEmpty()) event.setName(game);
        gofer = eventViewModel.gofer(event);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.<BaseViewHolder>with(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::refresh)
                .withAdapter(new EventEditAdapter(gofer.getItems(), this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .onLayoutManager(this::setSpanSizeLookUp)
                .withGridLayoutManager(2)
                .setHasFixedSize()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_rsvp);
        if (item == null) return;

        if (canEditEvent() || event.isPublic()) menu.findItem(R.id.action_delete).setVisible(true);

        disposables.add(gofer.getRSVPStatus().subscribe(item::setIcon, emptyErrorHandler));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_navigate:
                Uri uri = getEventUri();
                if (uri == null) {
                    showSnackbar(getString(R.string.event_no_location));
                    return true;
                }

                Intent maps = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(maps);
                return true;
            case R.id.action_rsvp:
                rsvpToEvent();
                return true;
            case R.id.action_delete:
                Context context = getContext();
                if (context == null) return true;
                new AlertDialog.Builder(context).setTitle(getString(R.string.delete_event_prompt))
                        .setPositiveButton(R.string.yes, (dialog, which) -> deleteEvent())
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventViewModel.clearNotifications(event);
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return event.isEmpty() ? R.string.event_create : R.string.event_update; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_check_white_24dp; }

    @Override
    protected int getToolbarMenu() { return R.menu.fragment_event_edit; }

    @Override
    public InsetFlags insetFlags() {return NO_TOP;}

    @Override
    protected CharSequence getToolbarTitle() {
        return gofer.getToolbarTitle(this);
    }

    @Override
    public boolean showsFab() { return !isBottomSheetShowing() && gofer.hasPrivilegedRole(); }

    @Override
    protected Event getHeaderedModel() {return event;}

    @Override
    protected Gofer<Event> gofer() {return gofer;}

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        toggleProgress(false);
        scrollManager.onDiff(result);
        viewHolder.bind(getHeaderedModel());
    }

    @Override
    protected void onPrepComplete() {
        scrollManager.notifyDataSetChanged();
        requireActivity().invalidateOptionsMenu();
        super.onPrepComplete();
    }

    @Override
    protected boolean cantGetModel() {
        return super.cantGetModel() || gofer.isSettingLocation();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            boolean wasEmpty = event.isEmpty();
            toggleProgress(true);
            disposables.add(gofer.save().subscribe(diffResult -> {
                int stringRes = wasEmpty ? R.string.added_user : R.string.updated_user;
                onModelUpdated(diffResult);
                showSnackbar(getString(stringRes, event.getName()));
            }, defaultErrorHandler));
        }
    }

    @Override
    public void onImageClick() {
        if (showsFab()) onLocationClicked();
    }

    @Override
    public void onTeamClicked(Team team) {
        eventViewModel.onEventTeamChanged(event, team);
        hideBottomSheet();

        int index = gofer.getItems().indexOf(team);
        if (index > -1) scrollManager.notifyItemChanged(index);
    }

    @Override
    public void selectTeam() {
        if (!TextUtils.isEmpty(event.getGameId()))
            showSnackbar(getString(R.string.game_event_team_change));
        else if (gofer.hasPrivilegedRole()) chooseTeam();
        else if (!gofer.hasRole())
            showFragment(JoinRequestFragment.joinInstance(event.getTeam(), userViewModel.getCurrentUser()));
    }

    @Override
    public void onGuestClicked(Guest guest) {
        User current = userViewModel.getCurrentUser();
        if (current.equals(guest.getUser())) rsvpToEvent();
        else showFragment(GuestViewFragment.newInstance(guest));
    }

    @Override
    public boolean canEditEvent() {
        return event.isEmpty() || gofer.hasPrivilegedRole();
    }

    @Override
    public void onLocationClicked() {
        gofer.setSettingLocation(true);
        pickPlace();
    }

    @Override
    public void onAddressPicked(Address address) {
        gofer.setSettingLocation(false);
        disposables.add(gofer.setAddress(address).subscribe(this::onModelUpdated, emptyErrorHandler));
    }

    private void rsvpEvent(boolean attending) {
        toggleProgress(true);
        disposables.add(gofer.rsvpEvent(attending).subscribe(this::onModelUpdated, defaultErrorHandler));
    }

    private void deleteEvent() {
        disposables.add(gofer.remove().subscribe(this::onEventDeleted, defaultErrorHandler));
    }

    private void onEventDeleted() {
        showSnackbar(getString(R.string.deleted_team, event.getName()));
        removeEnterExitTransitions();
        requireActivity().onBackPressed();
    }

    private void rsvpToEvent() {
        Activity activity;
        if ((activity = getActivity()) == null) return;

        new AlertDialog.Builder(activity).setTitle(getString(R.string.attend_event))
                .setPositiveButton(R.string.yes, (dialog, which) -> rsvpEvent(true))
                .setNegativeButton(R.string.no, (dialog, which) -> rsvpEvent(false))
                .show();
    }

    private void chooseTeam() {
        TeamsFragment teamsFragment = TeamsFragment.newInstance();
        teamsFragment.setTargetFragment(this, R.id.request_event_edit_pick);

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setTitle(getString(R.string.pick_team))
                .setFragment(teamsFragment)
                .build());
    }

    @Nullable
    private Uri getEventUri() {
        LatLng latLng = event.getLocation();
        if (latLng == null) return null;

        return new Uri.Builder()
                .scheme("https")
                .authority("www.google.com")
                .appendPath("maps")
                .appendPath("dir")
                .appendPath("")
                .appendQueryParameter("api", "1")
                .appendQueryParameter("destination", latLng.latitude + "," + latLng.longitude)
                .build();
    }

    private void setSpanSizeLookUp(RecyclerView.LayoutManager layoutManager) {
        ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return gofer.getItems().get(position) instanceof Guest ? 1 : 2;
            }
        });
    }
}
