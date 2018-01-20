package com.mainstreetcode.teammates.fragments.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.EventEditAdapter;
import com.mainstreetcode.teammates.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Guest;
import com.mainstreetcode.teammates.model.HeaderedModel;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Edits a Team member
 */

public class EventEditFragment extends HeaderedFragment
        implements
        View.OnClickListener,
        EventEditAdapter.EventEditAdapterListener {

    public static final String ARG_EVENT = "event";
    public static final int PLACE_PICKER_REQUEST = 1;
    private static final int[] EXCLUDED_VIEWS = {R.id.model_list};

    private boolean fromUserPickerAction;
    private Event event;
    private List<Identifiable> eventItems;

    private RecyclerView recyclerView;

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
        String superResult = super.getStableTag();
        Event tempEvent = getArguments().getParcelable(ARG_EVENT);

        return (tempEvent != null)
                ? superResult + "-" + tempEvent.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        event = getArguments().getParcelable(ARG_EVENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        eventItems = eventViewModel.fromEvent(event);

        recyclerView = rootView.findViewById(R.id.model_list);
        recyclerView.setLayoutManager(getGridLayoutManager());
        recyclerView.setAdapter(new EventEditAdapter(eventItems, this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!canEditEvent()) return;
                if (Math.abs(dy) < 3) return;
                toggleFab(dy < 0);
            }
        });

        recyclerView.requestFocus();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (showsFab()) inflater.inflate(R.menu.fragment_event_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                disposables.add(eventViewModel.delete(event).subscribe(this::onEventDeleted, defaultErrorHandler));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(event.isEmpty() ? R.string.create_event : R.string.edit_event));

        User user = userViewModel.getCurrentUser();

        disposables.add(localRoleViewModel.getRoleInTeam(user, event.getTeam()).subscribe(this::onRoleUpdated, emptyErrorHandler));

        if (!event.isEmpty() && !fromUserPickerAction) {
            disposables.add(eventViewModel.getEvent(event, eventItems).subscribe(this::onEventChanged, defaultErrorHandler));
        }
        fromUserPickerAction = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PLACE_PICKER_REQUEST) return;
        if (resultCode == RESULT_OK) {
            Activity activity;
            if ((activity = getActivity()) == null) return;

            Place place = PlacePicker.getPlace(activity, data);
            event.setPlace(place);
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    @Override
    public boolean drawsBehindStatusBar() {
        return true;
    }

    @Override
    protected boolean showsFab() {
        return canEditEvent();
    }

    @Override
    public int[] staticViews() {
        return EXCLUDED_VIEWS;
    }

    @Override
    protected HeaderedModel getHeaderedModel() {
        return event;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                boolean wasEmpty = event.isEmpty();
                toggleProgress(true);
                disposables.add(eventViewModel.createOrUpdateEvent(event, eventItems).subscribe(diffResult -> {
                    int stringRes = wasEmpty ? R.string.added_user : R.string.updated_user;
                    onEventChanged(diffResult);
                    showSnackbar(getString(stringRes, event.getName()));
                }, defaultErrorHandler));
                break;
        }
    }

    @Override
    public void onImageClick() {
        onLocationClicked();
    }

    @Override
    public void onTeamClicked(Team team) {
        eventViewModel.onEventTeamChanged(event, team);
    }

    @Override
    public void selectTeam() {
        fromUserPickerAction = true;
        TeamsFragment teamsFragment = TeamsFragment.newInstance();
        teamsFragment.setTargetFragment(this, R.id.request_event_edit_pick);
        showFragment(teamsFragment);
    }

    @Override
    public void rsvpToEvent(Guest guest) {
        Activity activity;
        User roleUser = localRoleViewModel.getCurrentRole().getUser();
        User guestUser = guest.getUser();
        if ((activity = getActivity()) == null || !guestUser.equals(roleUser))
            return;

        new AlertDialog.Builder(activity).setTitle(getString(R.string.attend_event))
                .setPositiveButton(R.string.yes, (dialog, which) -> rsvpEvent(event, true))
                .setNegativeButton(R.string.no, (dialog, which) -> rsvpEvent(event, false))
                .show();
    }

    @Override
    public boolean canEditEvent() {
        return event.isEmpty() || localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onLocationClicked() {
        fromUserPickerAction = true;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        Activity activity;
        if ((activity = getActivity()) == null) return;

        try {startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST);}
        catch (Exception e) {e.printStackTrace();}
    }

    private void rsvpEvent(Event event, boolean attending) {
        toggleProgress(true);
        disposables.add(eventViewModel.rsvpEvent(event, eventItems, attending).subscribe(this::onEventChanged, defaultErrorHandler));
    }

    private void onEventChanged(DiffUtil.DiffResult result) {
        toggleProgress(false);
        result.dispatchUpdatesTo(recyclerView.getAdapter());
        viewHolder.bind(getHeaderedModel());
    }

    private void onRoleUpdated() {
        Activity activity;
        if ((activity = getActivity()) == null) return;

        viewHolder.bind(getHeaderedModel());
        recyclerView.getAdapter().notifyDataSetChanged();
        activity.invalidateOptionsMenu();
        toggleFab(canEditEvent());
    }

    private void onEventDeleted(Event deleted) {
        showSnackbar(getString(R.string.deleted_team, deleted.getName()));
        removeEnterExitTransitions();

        Activity activity;
        if ((activity = getActivity()) == null) return;

        activity.onBackPressed();
    }

    @NonNull
    private GridLayoutManager getGridLayoutManager() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return eventItems.get(position) instanceof Guest ? 1 : 2;
            }
        });
        return layoutManager;
    }
}
