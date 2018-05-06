package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.EventEditAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.EventGofer;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;

import static android.app.Activity.RESULT_OK;

/**
 * Edits a Team member
 */

public class EventEditFragment extends HeaderedFragment<Event>
        implements
        EventEditAdapter.EventEditAdapterListener {

    public static final String ARG_EVENT = "event";
    private static final int[] EXCLUDED_VIEWS = {R.id.model_list};

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

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_EVENT));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        event = getArguments().getParcelable(ARG_EVENT);
        gofer = eventViewModel.gofer(event);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withAdapter(new EventEditAdapter(gofer.getItems(), this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .onLayoutManager(this::setSpanSizeLookUp)
                .withGridLayoutManager(2)
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_event_edit, menu);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean failed = resultCode != RESULT_OK;
        boolean isFromPlacePicker = requestCode == PLACE_PICKER_REQUEST;

        if (failed && isFromPlacePicker) gofer.setSettingLocation(false);
        if (failed || !isFromPlacePicker) return;

        Place place = PlacePicker.getPlace(requireContext(), data);
        disposables.add(gofer.setPlace(place).subscribe(this::onModelUpdated, emptyErrorHandler));
    }

    @Override
    public void togglePersistentUi() {
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(event.isEmpty() ? R.string.create_event : R.string.edit_event));
        super.togglePersistentUi();
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return canEditEvent();}

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    protected Event getHeaderedModel() {return event;}

    @Override
    protected Gofer<Event> gofer() {return gofer;}

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        toggleProgress(false);
        scrollManager.onDiff(result);
        viewHolder.bind(getHeaderedModel());
        Activity activity;
        if ((activity = getActivity()) != null) activity.invalidateOptionsMenu();
    }

    @Override
    protected void onPrepComplete() {
        scrollManager.notifyDataSetChanged();
        requireActivity().invalidateOptionsMenu();
        super.onPrepComplete();
    }

    @Override
    protected boolean canGetModel() {
        return super.canGetModel() && !gofer.isSettingLocation();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                boolean wasEmpty = event.isEmpty();
                toggleProgress(true);
                disposables.add(gofer.save().subscribe(diffResult -> {
                    int stringRes = wasEmpty ? R.string.added_user : R.string.updated_user;
                    onModelUpdated(diffResult);
                    showSnackbar(getString(stringRes, event.getName()));
                }, defaultErrorHandler));
                break;
        }
    }

    @Override
    public void onImageClick() {
        if (showsFab()) onLocationClicked();
    }

    @Override
    public void onTeamClicked(Team team) {
        eventViewModel.onEventTeamChanged(event, team);
        toggleBottomSheet(false);

        int index = event.asIdentifiables().indexOf(team);
        if (index > -1) scrollManager.notifyItemChanged(index);
    }

    @Override
    public void selectTeam() {
        if (!gofer.hasPrivilegedRole()) {
            showFragment(JoinRequestFragment.joinInstance(event.getTeam(), userViewModel.getCurrentUser()));
            return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null) return;

        TeamsFragment teamsFragment = TeamsFragment.newInstance();
        teamsFragment.setTargetFragment(this, R.id.request_event_edit_pick);

        beginTransaction()
                .replace(R.id.bottom_sheet, teamsFragment, teamsFragment.getStableTag())
                .commit();

        toggleBottomSheet(true);
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
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {startActivityForResult(builder.build(requireActivity()), PLACE_PICKER_REQUEST);}
        catch (Exception e) {Logger.log(getStableTag(), "Unable to start places api", e);}
    }

    private void rsvpEvent(boolean attending) {
        toggleProgress(true);
        disposables.add(gofer.rsvpEvent(attending).subscribe(this::onModelUpdated, defaultErrorHandler));
    }

    private void deleteEvent() {
        disposables.add(eventViewModel.delete(event).subscribe(this::onEventDeleted, defaultErrorHandler));
    }

    private void onEventDeleted(Event deleted) {
        showSnackbar(getString(R.string.deleted_team, deleted.getName()));
        removeEnterExitTransitions();

        Activity activity;
        if ((activity = getActivity()) == null) return;

        activity.onBackPressed();
    }

    private void rsvpToEvent() {
        Activity activity;
        if ((activity = getActivity()) == null) return;

        new AlertDialog.Builder(activity).setTitle(getString(R.string.attend_event))
                .setPositiveButton(R.string.yes, (dialog, which) -> rsvpEvent(true))
                .setNegativeButton(R.string.no, (dialog, which) -> rsvpEvent(false))
                .show();
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
