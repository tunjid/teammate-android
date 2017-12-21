package com.mainstreetcode.teammates.fragments.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.HeaderedModel;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

import static android.app.Activity.RESULT_OK;

/**
 * Edits a Team member
 */

public class EventEditFragment extends HeaderedFragment
        implements
        View.OnClickListener,
        EventEditAdapter.EditAdapterListener {

    public static final String ARG_EVENT = "event";
    public static final int PLACE_PICKER_REQUEST = 1;

    private boolean fromUserPickerAction;
    private Role currentRole = Role.empty();
    private Event event;

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

        ImageWorkerFragment.attach(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);
        recyclerView = rootView.findViewById(R.id.model_list);


        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position > event.size() ? 1 : 2;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new EventEditAdapter(event, true, this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
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
                disposables.add(eventViewModel.delete(event).subscribe(deleted -> {
                    Activity activity;
                    if ((activity = getActivity()) == null) return;

                    showSnackbar(getString(R.string.deleted_team, event.getName()));
                    activity.onBackPressed();
                }, defaultErrorHandler));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getFab().setOnClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(event.isEmpty() ? R.string.create_event : R.string.edit_event));

        User user = userViewModel.getCurrentUser();

        disposables.add(localRoleViewModel.getRoleInTeam(user, event.getTeam())
                .subscribe(this::onRoleUpdated, emptyErrorHandler));

        if (!event.isEmpty() && !fromUserPickerAction) {
            disposables.add(eventViewModel.getEvent(event).subscribe(updated -> recyclerView.getAdapter().notifyDataSetChanged(), defaultErrorHandler));
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
    public void onDestroy() {
        super.onDestroy();
        ImageWorkerFragment.detach(this);
    }

    @Override
    public boolean drawsBehindStatusBar() {
        return true;
    }

    @Override
    protected boolean showsFab() {
        return currentRole.isPrivilegedRole();
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
                disposables.add(eventViewModel.updateEvent(event)
                        .subscribe(updatedEvent -> {
                            event.update(updatedEvent);
                            showSnackbar(wasEmpty
                                    ? getString(R.string.added_user, event.getName())
                                    : getString(R.string.updated_user, event.getName()));
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }, defaultErrorHandler));
                break;
        }
    }

    @Override
    public void onImageClick() {
        if (showsFab()) fromUserPickerAction = true;
        super.onImageClick();
    }

    @Override
    public void onTeamClicked(Team team) {
        event.setTeam(team);
    }

    @Override
    public void selectTeam() {
        fromUserPickerAction = true;
        TeamsFragment teamsFragment = TeamsFragment.newInstance();
        teamsFragment.setTargetFragment(this, R.id.request_event_team_pick);
        showFragment(teamsFragment);
    }

    @Override
    public void rsvpToEvent(User user) {
        Activity activity;
        if ((activity = getActivity()) == null || !user.equals(currentRole.getUser())) return;

        new AlertDialog.Builder(activity).setTitle(getString(R.string.attend_event))
                .setPositiveButton(R.string.yes, (dialog, which) -> rsvpEvent(event, true))
                .setNegativeButton(R.string.no, (dialog, which) -> rsvpEvent(event, false))
                .show();
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
        disposables.add(eventViewModel.rsvpEvent(event, attending).subscribe(result -> {
            toggleProgress(false);
            recyclerView.getAdapter().notifyDataSetChanged();
        }, defaultErrorHandler));
    }

    private void onRoleUpdated(Role role) {
        currentRole.update(role);

        Activity activity;
        if ((activity = getActivity()) == null) return;

        toggleFab(showsFab());
        activity.invalidateOptionsMenu();
    }
}
