package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StandingsAdapter;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.util.SyncedScrollManager;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.StandingRowViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Standings;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.util.SyncedScrollView;

/**
 * Lists {@link Event tournaments}
 */

public final class StandingsFragment extends MainActivityFragment
        implements StandingsAdapter.AdapterListener {

    private static final String ARG_TOURNAMENT = "team";

    private Tournament tournament;
    private Standings standings;
    private StandingRowViewHolder viewHolder;
    private SyncedScrollManager syncedScrollManager = new SyncedScrollManager();

    public static StandingsFragment newInstance(Tournament team) {
        StandingsFragment fragment = new StandingsFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TOURNAMENT, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Tournament tempTournament = getArguments().getParcelable(ARG_TOURNAMENT);

        return (tempTournament != null)
                ? superResult + "-" + tempTournament.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        tournament = getArguments().getParcelable(ARG_TOURNAMENT);
        standings = tournamentViewModel.getStandings(tournament);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_standings, container, false);
        View spacerToolbar = rootView.findViewById(R.id.spacer_toolbar);

        Runnable refreshAction = () -> fetchStandings(true);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.list_layout))
                .withEmptyViewholder(new EmptyViewHolder(rootView.findViewById(R.id.empty_container), R.drawable.ic_table_24dp, R.string.tournament_no_standings))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScrollCallback(() -> fetchStandings(false))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new StandingsAdapter(standings.getTable(), this))
                .withLinearLayoutManager()
                .build();

        scrollManager.setViewHolderColor(R.attr.alt_empty_view_holder_tint);

        viewHolder = new StandingRowViewHolder(spacerToolbar.findViewById(R.id.item_container), this);
        viewHolder.thumbnail.setVisibility(View.GONE);
        viewHolder.position.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchStandings(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        syncedScrollManager.clearClients();
    }

    @Override
    public void togglePersistentUi() {/* Do nothing */}

    @Override
    public boolean showsFab() { return false; }

    @Override
    public void addScrollNotifier(SyncedScrollView notifier) {
        syncedScrollManager.addScrollClient(notifier);
    }

    @Override
    public void onCompetitorClicked(Competitor competitor) {
        showCompetitor(competitor);
    }

    private void fetchStandings(boolean isRefreshing) {
        if (isRefreshing) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(tournamentViewModel.fetchStandings(tournament).subscribe(this::onTournamentsUpdated, defaultErrorHandler));
    }

    private void onTournamentsUpdated() {
        scrollManager.notifyDataSetChanged();
        viewHolder.bindColumns(standings.getColumnNames());
        toggleProgress(false);
        if (!restoredFromBackStack()) syncedScrollManager.jog();
    }
}
