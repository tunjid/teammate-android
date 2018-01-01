package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.FeedAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.notifications.FeedItem;

import java.util.Calendar;

import io.reactivex.Single;

/**
 * Home screen
 */

public final class HomeFragment extends MainActivityFragment
        implements FeedAdapter.FeedItemAdapterListener {

    private RecyclerView recyclerView;
    private EmptyViewHolder emptyViewHolder;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = rootView.findViewById(R.id.feed_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new FeedAdapter(feedViewModel.getFeedItems(), this));

        emptyViewHolder = new EmptyViewHolder(rootView, R.drawable.ic_notifications_white_24dp, R.string.no_feed);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        disposables.add(userViewModel.getMe().subscribe(
                (user) -> setToolbarTitle(getString(R.string.home_greeting, getTimeOfDay(), user.getFirstName())),
                defaultErrorHandler
        ));
        disposables.add(feedViewModel.getFeed().subscribe((diffResult) -> {
                    diffResult.dispatchUpdatesTo(recyclerView.getAdapter());
                    emptyViewHolder.toggle(feedViewModel.getFeedItems().isEmpty());
                },
                defaultErrorHandler
        ));
    }

    @Override
    public void onDestroyView() {
        recyclerView = null;
        emptyViewHolder = null;
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_home, menu);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onFeedItemClicked(FeedItem item) {
        Model model = item.getModel();

        AlertDialog.Builder builder = new AlertDialog.Builder(recyclerView.getContext());

        if (model instanceof Event) {
            builder.setTitle(getString(R.string.attend_event))
                    .setPositiveButton(R.string.yes, (dialog, which) -> onFeedItemAction(feedViewModel.rsvpEvent(item, true)))
                    .setNegativeButton(R.string.no, (dialog, which) -> onFeedItemAction(feedViewModel.rsvpEvent(item, false)))
                    .show();
        }
        if (model instanceof JoinRequest) {
            builder.setTitle(getString(R.string.add_user_to_team, ((JoinRequest) model).getUser().getFirstName()))
                    .setPositiveButton(R.string.yes, (dialog, which) -> onFeedItemAction(feedViewModel.processJoinRequest(item, true)))
                    .setNegativeButton(R.string.no, (dialog, which) -> onFeedItemAction(feedViewModel.processJoinRequest(item, false)))
                    .show();
        }
    }

    @Override
    protected boolean showsFab() {
        return false;
    }

    private void onFeedItemAction(Single<DiffUtil.DiffResult> diffResultSingle) {
        toggleProgress(true);
        disposables.add(diffResultSingle.subscribe(diffResult -> {
                    diffResult.dispatchUpdatesTo(recyclerView.getAdapter());
                    toggleProgress(false);
                }, defaultErrorHandler)
        );
    }

    private static String getTimeOfDay() {
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hourOfDay > 0 && hourOfDay < 12) return "morning";
        else return "evening";
    }
}
