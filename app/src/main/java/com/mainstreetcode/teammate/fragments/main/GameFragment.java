package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.chip.Chip;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StatAdapter;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.GameViewHolder;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lists {@link Event games}
 */

public final class GameFragment extends MainActivityFragment
        implements UserAdapter.AdapterListener {

    private static final String ARG_GAME = "game";

    private Game game;
    private AtomicBoolean privilegeStatus;
    private GameViewHolder gameViewHolder;

    private Chip refereeChip;

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
        privilegeStatus = new AtomicBoolean();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);
        View appBar = rootView.findViewById(R.id.app_bar);
        refereeChip = rootView.findViewById(R.id.referee_chip);
        gameViewHolder = new GameViewHolder(appBar, ignored -> {});
        gameViewHolder.bind(game);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_stat_white_24dp, R.string.no_stats))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::refresh)
                .withEndlessScrollCallback(() -> fetchStats(false))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new StatAdapter(items, stat -> showFragment(StatEditFragment.newInstance(stat))))
                .withLinearLayoutManager()
                .build();

        scrollManager.setViewHolderColor(R.color.dark_grey);

        refereeChip.setCloseIconResource(R.drawable.ic_close_24dp);
        refereeChip.setOnCloseIconClickListener(v -> {
            game.getReferee().update(User.empty());
            toggleProgress(true);
            updateGame();
        });
        refereeChip.setOnClickListener(v -> {
            BaseFragment fragment = UserSearchFragment.newInstance();
            fragment.setTargetFragment(this, R.id.request_user_pick);
            showBottomSheet(BottomSheetController.Args.builder()
                    .setMenuRes(R.menu.empty)
                    .setFragment(fragment)
                    .build());
        });
        rootView.findViewById(R.id.home_thumbnail).setOnClickListener(view -> showCompetitor(game.getHome()));
        rootView.findViewById(R.id.away_thumbnail).setOnClickListener(view -> showCompetitor(game.getAway()));
        rootView.findViewById(R.id.score).setOnClickListener(view -> showFragment(GameEditFragment.newInstance(game)));
        rootView.findViewById(R.id.date).setOnClickListener(view -> showFragment(EventEditFragment.newInstance(game)));

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_end_game).setVisible(showsFab());
        menu.findItem(R.id.action_event).setVisible(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_game, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_end_game:
                endGameRequest();
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
    public void onDestroyView() {
        super.onDestroyView();
        refereeChip=null;
    }

    @Override
    public void togglePersistentUi() {
        updateFabIcon();
        setFabClickListener(this);
        setToolbarTitle(getString(R.string.game_stats));
        requireActivity().invalidateOptionsMenu();
        super.togglePersistentUi();
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.stat_add; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_add_white_24dp; }

    @Override
    public boolean showsFab() {
        return privilegeStatus.get() && !game.isEnded();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) showFragment(StatEditFragment.newInstance(Stat.empty(game)));
    }

    @Override
    public void onUserClicked(User item) {
        hideKeyboard();
        refereeChip.postDelayed(() -> {
            hideBottomSheet();
            addRefereeRequest(item);
        }, 200);
    }

    private void updateGame() {
        disposables.add(gameViewModel.updateGame(game).subscribe(this::onGameUpdated, defaultErrorHandler));
    }

    private void fetchGame() {
        disposables.add(gameViewModel.getGame(game).subscribe(this::onGameUpdated, defaultErrorHandler));
    }

    private void refresh() {
        fetchGame();
        disposables.add(statViewModel.refresh(game).subscribe(GameFragment.this::onStatsFetched, defaultErrorHandler));
    }

    private void updateFabStatus() {
        disposables.add(statViewModel.canEditGameStats(game).doOnSuccess(privilegeStatus::set)
                .subscribe(ignored -> togglePersistentUi(), defaultErrorHandler));
    }

    private void endGameRequest() {
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.game_end_request)
                .setMessage(R.string.game_end_prompt)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    toggleProgress(true);
                    disposables.add(gameViewModel.endGame(game).subscribe(this::onGameUpdated, defaultErrorHandler));
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addRefereeRequest(User user) {
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.game_add_referee_title)
                .setMessage(R.string.game_add_referee_prompt)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    game.getReferee().update(user);
                    toggleProgress(true);
                    updateGame();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void fetchStats(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(statViewModel.getMany(game, fetchLatest).subscribe(this::onStatsFetched, defaultErrorHandler));
        updateFabStatus();
    }

    private void onStatsFetched(DiffUtil.DiffResult result) {
        toggleProgress(false);
        scrollManager.onDiff(result);
    }

    private void onGameUpdated(Game game) {
        gameViewHolder.bind(game);
        toggleProgress(false);
        updateFabStatus();
        bindReferee();
    }

    private void bindReferee() {
        int size = refereeChip.getResources().getDimensionPixelSize(R.dimen.double_margin);
        User referee = game.getReferee();
        refereeChip.setText(referee.getName());
        refereeChip.setCloseIconVisible(!referee.isEmpty() && privilegeStatus.get());
        disposables.add(ViewHolderUtil.fetchRoundedDrawable(refereeChip.getContext(), referee.getImageUrl(), size)
                .subscribe(refereeChip::setChipIcon, ErrorHandler.EMPTY));
    }
}
