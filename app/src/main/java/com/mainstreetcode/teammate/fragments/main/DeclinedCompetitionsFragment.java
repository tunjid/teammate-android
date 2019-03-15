package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.CompetitorAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Event;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;

import static com.mainstreetcode.teammate.model.Item.COMPETITOR;

/**
 * Lists {@link Event tournaments}
 */

public final class DeclinedCompetitionsFragment extends MainActivityFragment
        implements
        CompetitorAdapter.AdapterListener {

    private List<Differentiable> items;

    public static DeclinedCompetitionsFragment newInstance() {
        DeclinedCompetitionsFragment fragment = new DeclinedCompetitionsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = competitorViewModel.getModelList(User.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false);

        Runnable refreshAction = () -> disposables.add(competitorViewModel.refresh(User.class).subscribe(DeclinedCompetitionsFragment.this::onCompetitorsUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.<CompetitorViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(rootView, R.drawable.ic_thumb_down_24dp, R.string.no_competitors_declined))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll(() -> fetchCompetitions(false))
                .addScrollListener((dx, dy) -> updateFabForScrollState(dy))
                .addScrollListener((dx, dy) -> updateTopSpacerElevation())
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(getAdapter())
                .withLinearLayoutManager()
                .build();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCompetitions(true);
    }

    @Override
    public boolean showsFab() { return false; }

    @Override
    protected CharSequence getToolbarTitle() {
        return getString(R.string.competitors_declined);
    }

    @Override
    public void onCompetitorClicked(Competitor competitor) {
        new AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.accept_competition))
                .setPositiveButton(R.string.yes, (dialog, which) -> accept(competitor))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .setNeutralButton(R.string.event_details, ((dialog, which) -> {
                    BaseFragment fragment = !competitor.getGame().isEmpty()
                            ? GameFragment.newInstance(competitor.getGame())
                            : !competitor.getTournament().isEmpty()
                            ? TournamentDetailFragment.newInstance(competitor.getTournament()).pending(competitor)
                            : null;
                    if (fragment != null) showFragment(fragment);
                }))
                .show();
    }

    private void accept(Competitor competitor) {
        toggleProgress(true);
        disposables.add(competitorViewModel.respond(competitor, true)
                .subscribe(diffResult -> {
                    toggleProgress(false);
                    scrollManager.onDiff(diffResult);
                }, defaultErrorHandler));
    }

    private void fetchCompetitions(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(competitorViewModel.getMany(User.class, fetchLatest).subscribe(this::onCompetitorsUpdated, defaultErrorHandler));
    }

    private void onCompetitorsUpdated(DiffUtil.DiffResult result) {
        toggleProgress(false);
        scrollManager.onDiff(result);
    }

    @NonNull
    private CompetitorAdapter getAdapter() {
        return new CompetitorAdapter(items, this) {
            @NonNull
            @Override
            public CompetitorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                if (viewType != COMPETITOR) return super.onCreateViewHolder(viewGroup, viewType);
                return new CompetitorViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener);
            }
        };
    }
}
