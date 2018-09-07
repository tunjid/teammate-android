package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GameAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.List;

/**
 * Lists {@link Event tournaments}
 */

public final class GamesChildFragment extends MainActivityFragment
        implements
        GameAdapter.AdapterListener {

    private static final String ARG_TOURNAMENT = "tournament";
    private static final String ARG_ROUND = "round";

    private int round;
    private Tournament tournament;
    private List<Identifiable> items;

    public static GamesChildFragment newInstance(Tournament tournament, int round) {
        GamesChildFragment fragment = new GamesChildFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TOURNAMENT, tournament);
        args.putInt(ARG_ROUND, round);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Tournament tempTournament = getArguments().getParcelable(ARG_TOURNAMENT);
        int round = getArguments().getInt(ARG_ROUND);

        return (tempTournament != null)
                ? superResult + "-" + tempTournament.hashCode() + "-" + round
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        round = getArguments().getInt(ARG_ROUND);
        tournament = getArguments().getParcelable(ARG_TOURNAMENT);
        items = gameViewModel.getGamesForRound(tournament, round);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_games_child, container, false);

        Runnable refreshAction = () -> disposables.add(gameViewModel.fetchGamesInRound(tournament, round).subscribe(GamesChildFragment.this::onGamesUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_trophy_white_24dp, R.string.no_tournaments))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScrollCallback(() -> fetchTournaments(false))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new GameAdapter(items, this))
                .withLinearLayoutManager()
                .build();

        scrollManager.setViewHolderColor(R.color.dark_grey);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTournaments(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_team:
                TeamPickerFragment.change(getActivity(), R.id.request_tournament_team_pick);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void togglePersistentUi() {/* Do nothing */}

    @Override
    public boolean showsFab() { return false; }

    @Override
    public void onGameClicked(Game game) {
        showFragment(GameFragment.newInstance(game));
    }

    void fetchTournaments(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(gameViewModel.fetchGamesInRound(tournament, round).subscribe(this::onGamesUpdated, defaultErrorHandler));
    }

    private void onGamesUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
        toggleProgress(false);
    }
}
