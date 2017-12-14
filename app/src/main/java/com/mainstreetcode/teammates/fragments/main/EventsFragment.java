package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.EventAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.EventViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Event;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;

/**
 * Lists {@link com.mainstreetcode.teammates.model.Event events}
 */

public final class EventsFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        EventAdapter.EventAdapterListener {

    private RecyclerView recyclerView;
    private EmptyViewHolder emptyViewHolder;
    private final List<Event> events = new ArrayList<>();

    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        String userId = userViewModel.getCurrentUser().getId();
        disposables.add(eventViewModel.getEvents(events, userId).subscribe(this::onEventsUpdated, defaultErrorHandler));
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
            Event event = fragmentTo.getArguments().getParcelable(EventEditFragment.ARG_EVENT);
            if (event == null) return superResult;

            EventViewHolder viewHolder = (EventViewHolder)recyclerView.findViewHolderForItemId(event.hashCode());
            if(viewHolder == null) return superResult;

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
