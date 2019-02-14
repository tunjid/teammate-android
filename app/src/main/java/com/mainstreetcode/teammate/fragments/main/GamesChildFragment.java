package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
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
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

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
    private List<Differentiable> items;

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
        Fragment fragment = getParentFragment();
        RecyclerView.RecycledViewPool recycledViewPool = fragment instanceof TournamentDetailFragment
                ? ((TournamentDetailFragment) fragment).getGamesRecycledViewPool()
                : new RecyclerView.RecycledViewPool();

        scrollManager = ScrollManager.<InteractiveViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(rootView, R.drawable.ic_trophy_white_24dp, R.string.no_tournaments))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::onRefresh)
                .withEndlessScroll(() -> fetchTournaments(false))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new GameAdapter(items, this))
                .withRecycledViewPool(recycledViewPool)
                .withLinearLayoutManager()
                .build();

        scrollManager.setViewHolderColor(R.attr.alt_empty_view_holder_tint);

        return rootView;
    }

    private void onRefresh() {
        disposables.add(gameViewModel.fetchGamesInRound(tournament, round).subscribe(GamesChildFragment.this::onGamesUpdated, defaultErrorHandler));
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

    private void fetchTournaments(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        onRefresh();
    }

    private void onGamesUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
        toggleProgress(false);
    }
}
