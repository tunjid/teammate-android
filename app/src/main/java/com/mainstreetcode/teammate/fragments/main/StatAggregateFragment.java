package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StatAggregateAdapter;
import com.mainstreetcode.teammate.adapters.StatAggregateRequestAdapter;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.StatAggregate;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ExpandingToolbar;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;

public class StatAggregateFragment extends MainActivityFragment
        implements
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener,
        StatAggregateRequestAdapter.AdapterListener {

    private StatAggregate.Request request;
    private ExpandingToolbar expandingToolbar;
    private ScrollManager searchScrollManager;

    private List<Identifiable> items;

    public static StatAggregateFragment newInstance() {
        StatAggregateFragment fragment = new StatAggregateFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = StatAggregate.Request.empty();
        items = statViewModel.getStatAggregates();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stat_aggregate, container, false);

        searchScrollManager = ScrollManager.withRecyclerView(root.findViewById(R.id.search_options))
                .withRefreshLayout(root.findViewById(R.id.refresh_layout), this::fetchAggregates)
                .withAdapter(new StatAggregateRequestAdapter(request, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager = ScrollManager.withRecyclerView(root.findViewById(R.id.team_list))
                .withEmptyViewholder(new EmptyViewHolder(root, R.drawable.ic_stat_white_24dp, R.string.stat_aggregate_empty))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new StatAggregateAdapter(items))
                .withLinearLayoutManager()
                .build();

        expandingToolbar = ExpandingToolbar.create(root.findViewById(R.id.card_view_wrapper), this::fetchAggregates);
        expandingToolbar.setTitleIcon(false);
        expandingToolbar.changeVisibility(false);
        expandingToolbar.setTitle(R.string.stat_aggregate_get);

        scrollManager.notifyDataSetChanged();

        return root;
    }

    @Override
    public void onDestroyView() {
        expandingToolbar = null;
        searchScrollManager = null;
        super.onDestroyView();
    }

    @Override
    public boolean showsToolBar() { return false; }

    @Override
    public boolean showsFab() { return false; }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        updateFabIcon();
        setFabClickListener(this);
    }

    @Override
    public void onUserPicked(User item) {
        pick(UserSearchFragment.newInstance());
    }

    @Override
    public void onTeamPicked(Team item) {
        pick(TeamSearchFragment.newInstance(request.getSport()));
    }

    @Override
    public void onTeamClicked(Team item) {
        updateEntity(item);
    }

    @Override
    public void onUserClicked(User item) {
        updateEntity(item);
    }

    private void fetchAggregates() {
        toggleProgress(true);
        disposables.add(statViewModel.aggregate(request).subscribe(result -> {
            toggleProgress(false);
            scrollManager.onDiff(result);
        }, defaultErrorHandler));
    }

    private void updateEntity(Competitive item) {
        if (item instanceof User) request.updateUser((User) item);
        else if (item instanceof Team) request.updateTeam((Team) item);
        else return;

        searchScrollManager.notifyDataSetChanged();
        searchScrollManager.getRecyclerView().postDelayed(this::hideBottomSheet, 200);
        hideKeyboard();
    }

    private void pick(BaseFragment fragment) {
        fragment.setTargetFragment(this, R.id.request_competitor_pick);
        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(fragment)
                .build());
    }

}
