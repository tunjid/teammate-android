package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.FeedAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.FeedItem;
import com.mainstreetcode.teammates.model.BaseModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Home screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public final class HomeFragment extends MainActivityFragment
        implements FeedAdapter.FeedItemAdapterListener {

    private RecyclerView recyclerView;

    private final List<FeedItem> feed = new ArrayList<>();

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = rootView.findViewById(R.id.feed_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new FeedAdapter(feed, this));
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        toggleFab(false);
        disposables.add(userViewModel.getMe().subscribe(
                (user) -> setToolbarTitle(getString(R.string.home_greeting, getTimeofDay(), user.getFirstName())),
                defaultErrorHandler
        ));
        disposables.add(userViewModel.getFeed().subscribe(
                (updatedFeed) -> {
                    feed.clear();
                    feed.addAll(updatedFeed);
                    recyclerView.getAdapter().notifyDataSetChanged();
                },
                defaultErrorHandler
        ));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_home, menu);
    }

    @Override
    public void onFeedItemClicked(FeedItem item) {
        BaseModel model = item.getModel();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (model instanceof Event) {
            builder.setTitle(getString(R.string.attend_event))
                    .setPositiveButton(R.string.yes, (dialog, which) -> rsvpEvent(item, (Event) model, true))
                    .setNegativeButton(R.string.no, (dialog, which) -> rsvpEvent(item, (Event) model, false))
                    .show();
        }
    }

    private void rsvpEvent(FeedItem item, Event event, boolean attending) {
        toggleProgress(true);
        disposables.add(eventViewModel.rsvpEvent(event, attending).subscribe(result -> {
                    toggleProgress(false);
                    int index = feed.indexOf(item);
                    if (index >= 0) {
                        feed.remove(index);
                        recyclerView.getAdapter().notifyItemRemoved(index);
                    }
                }, defaultErrorHandler)
        );
    }

    private static String getTimeofDay() {
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hourOfDay > 0 && hourOfDay < 12) return "morning";
        else return "evening";
    }
}
