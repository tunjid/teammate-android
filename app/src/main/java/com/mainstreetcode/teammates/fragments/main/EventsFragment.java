package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.EventAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.EventViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;

/**
 * Lists {@link com.mainstreetcode.teammates.model.Event events}
 */

public final class EventsFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        EventAdapter.EventAdapterListener {

    private static final String ARG_TEAM = "team";

    private Team team;
    private RecyclerView recyclerView;
    private EmptyViewHolder emptyViewHolder;
    private List<Event> events;

    public static EventsFragment newInstance(Team team) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Team tempTeam = getArguments().getParcelable(ARG_TEAM);

        return (tempTeam != null)
                ? superResult + "-" + tempTeam.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        team = getArguments().getParcelable(ARG_TEAM);
        events = eventViewModel.getModelList(team);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teams, container, false);
        recyclerView = rootView.findViewById(R.id.team_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new EventAdapter(events, this));

        emptyViewHolder = new EmptyViewHolder(rootView, R.drawable.ic_event_black_24dp, R.string.no_events);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getFab().setOnClickListener(this);
        setFabIcon(R.drawable.ic_add_white_24dp);
        setToolbarTitle(getString(R.string.my_events));

        disposables.add(eventViewModel.getEvents(team).subscribe(this::onEventsUpdated, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_events, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_team:
                teamViewModel.updateDefaultTeam(Team.empty());
                TeamPickerFragment.pick(getActivity(), R.id.request_event_team_pick);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        emptyViewHolder = null;
    }

    @Override
    protected boolean showsFab() {
        return true;
    }

    @Override
    public void onEventClicked(Event event) {
        showFragment(EventEditFragment.newInstance(event));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                showFragment(EventEditFragment.newInstance(Event.empty()));
                break;
        }
    }

    @Nullable
    @Override
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        FragmentTransaction superResult = super.provideFragmentTransaction(fragmentTo);

        if (fragmentTo.getStableTag().contains(EventEditFragment.class.getSimpleName())) {
            Bundle args = fragmentTo.getArguments();
            if (args == null) return superResult;

            Event event = args.getParcelable(EventEditFragment.ARG_EVENT);
            if (event == null) return superResult;

            EventViewHolder viewHolder = (EventViewHolder) recyclerView.findViewHolderForItemId(event.hashCode());
            if (viewHolder == null) return superResult;

            return beginTransaction()
                    .addSharedElement(viewHolder.itemView, getTransitionName(event, R.id.fragment_header_background))
                    .addSharedElement(viewHolder.getImage(), getTransitionName(event, R.id.fragment_header_thumbnail));

        }
        return superResult;
    }

    private void onEventsUpdated(DiffUtil.DiffResult result) {
        emptyViewHolder.toggle(events.isEmpty());
        result.dispatchUpdatesTo(recyclerView.getAdapter());
    }
}
