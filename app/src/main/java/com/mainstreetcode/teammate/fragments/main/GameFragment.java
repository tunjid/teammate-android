package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StatAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.GameViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lists {@link Event games}
 */

public final class GameFragment extends MainActivityFragment {

    private static final String ARG_GAME = "game";

    private Game game;
    private AtomicBoolean fabStatus;
    private GameViewHolder gameViewHolder;

    private List<Identifiable> items;

    public static GameFragment newInstance(Game game) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_GAME, game);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Game tempGame = getArguments().getParcelable(ARG_GAME);

        return (tempGame != null)
                ? superResult + "-" + tempGame.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        game = getArguments().getParcelable(ARG_GAME);
        items = statViewModel.getModelList(game);
        fabStatus = new AtomicBoolean();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);
        gameViewHolder = new GameViewHolder(rootView.findViewById(R.id.app_bar), ignored -> {});
        gameViewHolder.bind(game);

        Runnable refreshAction = () -> disposables.add(statViewModel.refresh(game).subscribe(GameFragment.this::onGamesUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_stat_white_24dp, R.string.no_stats))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScrollCallback(() -> fetchStats(false))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new StatAdapter(items, stat -> showFragment(StatEditFragment.newInstance(stat))))
                .withLinearLayoutManager()
                .build();

        scrollManager.setViewHolderColor(R.color.dark_grey);

        rootView.findViewById(R.id.date).setOnClickListener(view -> showFragment(EventEditFragment.newInstance(game)));

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_end_game).setVisible(showsFab());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_game, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_end_game:
                endGame();
                return true;
            case R.id.action_event:
                showFragment(EventEditFragment.newInstance(game));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchGame();
        fetchStats(true);
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_add_white_24dp);
        setToolbarTitle(getString(R.string.game_stats));
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean showsFab() {
        return fabStatus.get() && !game.isEnded();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) showFragment(StatEditFragment.newInstance(Stat.empty(game)));
    }

    private void endGame() {
        toggleProgress(true);
        disposables.add(gameViewModel.endGame(game).subscribe(this::onGameUpdated, defaultErrorHandler));
    }

    private void fetchGame() {
        disposables.add(gameViewModel.getGame(game).subscribe(this::onGameUpdated, defaultErrorHandler));
    }

    private void fetchStats(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(statViewModel.getMany(game, fetchLatest).subscribe(this::onGamesUpdated, defaultErrorHandler));
        disposables.add(statViewModel.canEditGameStats(game).doOnSuccess(fabStatus::set).subscribe(ignored -> togglePersistentUi(), defaultErrorHandler));
    }

    private void onGamesUpdated(DiffUtil.DiffResult result) {
        toggleProgress(false);
        scrollManager.onDiff(result);
    }

    private void onGameUpdated() {
        gameViewHolder.bind(game);
        toggleProgress(false);
        togglePersistentUi();
    }
}
