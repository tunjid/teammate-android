package com.mainstreetcode.teammate.fragments.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.EventAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.EventViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.MyEventsViewModel;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

/**
 * Lists {@link Event events}
 */

public final class MyEventsFragment extends MainActivityFragment
        implements
        EventAdapter.EventAdapterListener {

    private List<Identifiable> items;
    private MyEventsViewModel myEventsViewModel;

    public static MyEventsFragment newInstance() {
        MyEventsFragment fragment = new MyEventsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myEventsViewModel = ViewModelProviders.of(requireActivity()).get(MyEventsViewModel.class);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = myEventsViewModel.getModelList(Event.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false);

        Runnable refreshAction = () -> disposables.add(myEventsViewModel.refresh(Event.class).subscribe(MyEventsFragment.this::onEventsUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.<InteractiveViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(rootView, R.drawable.ic_event_white_24dp, R.string.no_rsvp))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll(() -> fetchEvents(false))
                .addScrollListener((dx, dy) -> updateTopSpacerElevation())
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new EventAdapter(items, this))
                .withLinearLayoutManager()
                .build();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchEvents(true);
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setToolbarTitle(getString(R.string.attending_events));
    }

    @Override
    public boolean showsFab() {
        return false;
    }

    @Override
    public void onEventClicked(Event event) {
        showFragment(EventEditFragment.newInstance(event));
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

            EventViewHolder viewHolder = (EventViewHolder) scrollManager.findViewHolderForItemId(event.hashCode());
            if (viewHolder == null) return superResult;

            return beginTransaction()
                    .addSharedElement(viewHolder.itemView, getTransitionName(event, R.id.fragment_header_background))
                    .addSharedElement(viewHolder.getImage(), getTransitionName(event, R.id.fragment_header_thumbnail));

        }
        return superResult;
    }

    private void fetchEvents(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(myEventsViewModel.getMany(Event.class, fetchLatest).subscribe(this::onEventsUpdated, defaultErrorHandler));
    }

    private void onEventsUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
        toggleProgress(false);
    }
}
