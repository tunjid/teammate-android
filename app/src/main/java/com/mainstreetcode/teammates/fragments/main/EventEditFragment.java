package com.mainstreetcode.teammates.fragments.main;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.EventEditAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

/**
 * Edits a Team member
 */

public class EventEditFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        ImageWorkerFragment.CropListener,
        EventEditAdapter.EditAdapterListener {

    private static final String ARG_EVENT = "event";

    private Event event;

    @Nullable
    private Role currentRole;

    private RecyclerView recyclerView;

    public static EventEditFragment newInstance(Event event) {
        EventEditFragment fragment = new EventEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        Event tempEvent = getArguments().getParcelable(ARG_EVENT);

        return (tempEvent != null)
                ? superResult + "-" + tempEvent.hashCode()
                : superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        event = getArguments().getParcelable(ARG_EVENT);

        ImageWorkerFragment.attach(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_edit, container, false);
        recyclerView = rootView.findViewById(R.id.user_edit);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
        if (currentRole != null && currentRole.isTeamAdmin()) {
            inflater.inflate(R.menu.fragment_event_edit, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                disposables.add(eventViewModel.delete(event).subscribe(deleted -> {
                    showSnackbar(getString(R.string.deleted_team, event.getName()));
                    getActivity().onBackPressed();
                }, defaultErrorHandler));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setOnClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(event.isEmpty() ? R.string.create_event : R.string.edit_event));

        User user = userViewModel.getCurrentUser();

        disposables.add(roleViewModel.getRoleInTeam(user.getId(), event.getTeam().getId()).subscribe(role -> {
            currentRole = role;
            getActivity().invalidateOptionsMenu();
        }, emptyErrorHandler));

        if (!event.isEmpty()) {
            disposables.add(eventViewModel.getEvent(event).subscribe(updated -> recyclerView.getAdapter().notifyDataSetChanged(), defaultErrorHandler));
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
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                disposables.add(eventViewModel.updateEvent(event)
                        .subscribe(updatedEvent -> {
                            event.update(updatedEvent);
                            showSnackbar(getString(R.string.updated_user, event.getName()));
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }, defaultErrorHandler));
                break;
        }
    }

    @Override
    public void onImageCropped(Uri uri) {
        event.get(Event.LOGO_POSITION).setValue(uri.getPath());
        recyclerView.getAdapter().notifyItemChanged(Event.LOGO_POSITION);
    }

    @Override
    public void onImageClick() {
        ImageWorkerFragment.requestCrop(this);
    }

    @Override
    public void onTeamClicked(Team team) {
        event.setTeam(team);
    }

    @Override
    public void selectTeam() {
        TeamsFragment teamsFragment = TeamsFragment.newInstance();
        teamsFragment.setTargetFragment(this, R.id.request_event_team_pick);
        showFragment(teamsFragment);
    }

    @Override
    public void rsvpToEvent(User user) {
        if (currentRole == null || !user.equals(currentRole.getUser())) return;

        new AlertDialog.Builder(getContext()).setTitle(getString(R.string.attend_event))
                .setPositiveButton(R.string.yes, (dialog, which) -> rsvpEvent(event, true))
                .setNegativeButton(R.string.no, (dialog, which) -> rsvpEvent(event, false))
                .show();
    }

    private void rsvpEvent(Event event, boolean attending) {
        toggleProgress(true);
        disposables.add(eventViewModel.rsvpEvent(event, attending).subscribe(result -> {
                    toggleProgress(false);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }, defaultErrorHandler)
        );
    }
}
