package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GameAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TournamentViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.ListState;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DiffUtil;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

/**
 * Lists {@link Event tournaments}
 */

public final class GamesFragment extends MainActivityFragment
        implements
        GameAdapter.AdapterListener {

    private static final String ARG_TEAM = "team";

    private Team team;
    private List<Differentiable> items;

    public static GamesFragment newInstance(Team team) {
        GamesFragment fragment = new GamesFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Team tempTeam = getArguments().getParcelable(ARG_TEAM);

        return (tempTeam != null)
                ? superResult + "-" + tempTeam.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team = getArguments().getParcelable(ARG_TEAM);
        items = gameViewModel.getModelList(team);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false);

        Runnable refreshAction = () -> disposables.add(gameViewModel.refresh(team).subscribe(GamesFragment.this::onGamesUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.<InteractiveViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(rootView, R.drawable.ic_score_white_24dp, R.string.no_games))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll(() -> fetchGames(false))
                .addScrollListener((dx, dy) -> updateFabForScrollState(dy))
                .addScrollListener((dx, dy) -> updateTopSpacerElevation())
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new GameAdapter(items, this))
                .withLinearLayoutManager()
                .build();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchGames(true);
        watchForRoleChanges(team, this::togglePersistentUi);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_team:
                TeamPickerFragment.change(getActivity(), R.id.request_game_team_pick);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @StringRes
    public int getFabStringResource() { return R.string.game_add; }

    @Override
    @DrawableRes
    public int getFabIconResource() { return R.drawable.ic_add_white_24dp; }

    @Override
    protected int getToolbarMenu() { return R.menu.fragment_tournaments; }

    @Override public CharSequence getToolbarTitle() { return getString(R.string.games); }

    @Override
    public boolean showsFab() {
        Sport sport = team.getSport();
        boolean supportsTournaments = sport.supportsCompetitions();
        if (sport.betweenUsers()) return supportsTournaments;
        return supportsTournaments && localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onGameClicked(Game game) {
        showFragment(GameFragment.newInstance(game));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                Game game = Game.empty(team);
                Competitive entity = User.COMPETITOR_TYPE.equals(game.getRefPath())
                        ? userViewModel.getCurrentUser()
                        : teamViewModel.getDefaultTeam();

                game.getHome().updateEntity(entity);
                showFragment(GameEditFragment.newInstance(game));
                break;
        }
    }

    @Nullable
    @Override
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        FragmentTransaction superResult = super.provideFragmentTransaction(fragmentTo);

        if (fragmentTo.getStableTag().contains(TournamentEditFragment.class.getSimpleName())) {
            Bundle args = fragmentTo.getArguments();
            if (args == null) return superResult;

            Tournament tournament = args.getParcelable(TournamentEditFragment.ARG_TOURNAMENT);
            if (tournament == null) return superResult;

            TournamentViewHolder viewHolder = (TournamentViewHolder) scrollManager.findViewHolderForItemId(tournament.hashCode());
            if (viewHolder == null) return superResult;

            return beginTransaction()
                    .addSharedElement(viewHolder.itemView, getTransitionName(tournament, R.id.fragment_header_background))
                    .addSharedElement(viewHolder.getImage(), getTransitionName(tournament, R.id.fragment_header_thumbnail));

        }
        return superResult;
    }

    private void fetchGames(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(gameViewModel.getMany(team, fetchLatest).subscribe(this::onGamesUpdated, defaultErrorHandler));
    }

    private void onGamesUpdated(DiffUtil.DiffResult result) {
        toggleProgress(false);
        boolean supportsTournaments = team.getSport().supportsCompetitions();
        scrollManager.onDiff(result);
        scrollManager.updateForEmptyList(ListState.of(R.drawable.ic_score_white_24dp, supportsTournaments
                ? R.string.no_games : R.string.no_game_support));
    }
}
