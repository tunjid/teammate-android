package com.mainstreetcode.teammates.fragments.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.FeedAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.FeedItemViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.notifications.FeedItem;
import com.mainstreetcode.teammates.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;

/**
 * Home screen
 */

public final class FeedFragment extends MainActivityFragment
        implements FeedAdapter.FeedItemAdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.feed_list};
    private AtomicBoolean bottomBarState;

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        bottomBarState = new AtomicBoolean();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.feed_list))
                .withLayoutManager(new LinearLayoutManager(getContext()))
                .withAdapter(new FeedAdapter(feedViewModel.getModelList(FeedItem.class), this))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_notifications_white_24dp, R.string.no_feed))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .build();

        bottomBarState.set(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        disposables.add(userViewModel.getMe().subscribe(
                (user) -> setToolbarTitle(getString(R.string.home_greeting, getTimeOfDay(), user.getFirstName())),
                defaultErrorHandler
        ));
        disposables.add(feedViewModel.refresh(FeedItem.class).subscribe(this::onFeedUpdated, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_feed, menu);
    }

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    @SuppressWarnings("unchecked")
    public void onFeedItemClicked(FeedItem item) {
        Model model = item.getModel();
        Context context = getContext();
        if (context == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (model instanceof Event) {
            builder.setTitle(getString(R.string.attend_event))
                    .setPositiveButton(R.string.yes, (dialog, which) -> onFeedItemAction(feedViewModel.rsvpEvent(item, true)))
                    .setNegativeButton(R.string.no, (dialog, which) -> onFeedItemAction(feedViewModel.rsvpEvent(item, false)))
                    .setNeutralButton(R.string.event_details, ((dialog, which) -> showFragment(EventEditFragment.newInstance((Event) model))))
                    .show();
        }
        else if (model instanceof JoinRequest) {
            JoinRequest request = ((JoinRequest) model);
            String title = request.isTeamApproved()
                    ? getString(R.string.accept_invitation, request.getTeam().getName())
                    : getString(R.string.add_user_to_team, request.getUser().getFirstName());

            builder.setTitle(title)
                    .setPositiveButton(R.string.yes, (dialog, which) -> onFeedItemAction(feedViewModel.processJoinRequest(item, true)))
                    .setNegativeButton(R.string.no, (dialog, which) -> onFeedItemAction(feedViewModel.processJoinRequest(item, false)))
                    .show();
        }
        else if (model instanceof Media) {
            bottomBarState.set(false);
            toggleBottombar(false);
            showFragment(MediaDetailFragment.newInstance((Media) model));
        }
    }

    @Override
    public boolean showsFab() {return false;}

    @Override
    public boolean showsBottomNav() {return bottomBarState.get();}

    @Override
    @Nullable
    @SuppressLint("CommitTransaction")
    @SuppressWarnings("ConstantConditions")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        if (fragmentTo.getStableTag().contains(MediaDetailFragment.class.getSimpleName())) {
            Media media = fragmentTo.getArguments().getParcelable(MediaDetailFragment.ARG_MEDIA);

            if (media == null) return null;
            FeedItemViewHolder holder = (FeedItemViewHolder) scrollManager.findViewHolderForItemId(media.hashCode());
            if (holder == null) return null;

            return beginTransaction()
                    .addSharedElement(holder.itemView, getTransitionName(media, R.id.fragment_media_background))
                    .addSharedElement(holder.thumbnail, getTransitionName(media, R.id.fragment_media_thumbnail));
        }
        else if (fragmentTo.getStableTag().contains(EventEditFragment.class.getSimpleName())) {
            Bundle args = fragmentTo.getArguments();
            if (args == null) return super.provideFragmentTransaction(fragmentTo);

            Event event = args.getParcelable(EventEditFragment.ARG_EVENT);
            if (event == null) return super.provideFragmentTransaction(fragmentTo);

            FeedItemViewHolder holder = (FeedItemViewHolder) scrollManager.findViewHolderForItemId(event.hashCode());
            if (holder == null) return super.provideFragmentTransaction(fragmentTo);

            return beginTransaction()
                    .addSharedElement(holder.itemView, getTransitionName(event, R.id.fragment_header_background))
                    .addSharedElement(holder.thumbnail, getTransitionName(event, R.id.fragment_header_thumbnail));

        }
        return super.provideFragmentTransaction(fragmentTo);
    }

    private void onFeedItemAction(Single<DiffUtil.DiffResult> diffResultSingle) {
        toggleProgress(true);
        disposables.add(diffResultSingle.subscribe(this::onFeedUpdated, defaultErrorHandler));
    }

    private void onFeedUpdated(DiffUtil.DiffResult diffResult) {
        toggleProgress(false);
        scrollManager.onDiff(diffResult);
    }

    private static String getTimeOfDay() {
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hourOfDay > 0 && hourOfDay < 12) return "morning";
        else return "evening";
    }
}
