package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GameEditAdapter;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.GameGofer;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

/**
 * Edits a Team member
 */

public class GameEditFragment extends HeaderedFragment<Game>
        implements
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener,
        GameEditAdapter.AdapterListener {

    public static final String ARG_GAME = "stat";
    private static final int[] EXCLUDED_VIEWS = {R.id.model_list};

    private Game game;
    private GameGofer gofer;

    public static GameEditFragment newInstance(Game game) {
        GameEditFragment fragment = new GameEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_GAME, game);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_GAME));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = getArguments().getParcelable(ARG_GAME);
        gofer = gameViewModel.gofer(game);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::refresh)
                .withAdapter(new GameEditAdapter(gofer.getItems(), this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        statViewModel.clearNotifications(game);
    }

    @Override
    public void togglePersistentUi() {
        updateFabIcon();
        setFabClickListener(this);
        setToolbarTitle(getString(game.isEmpty() ? R.string.game_add : R.string.game_edit));
        super.togglePersistentUi();
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return game.isEmpty() ? R.string.game_create : R.string.game_update; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_check_white_24dp; }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return gofer.canEdit() && !isBottomSheetShowing();}

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    protected Game getHeaderedModel() {return game;}

    @Override
    protected Gofer<Game> gofer() {return gofer;}

    @Override
    protected boolean canExpandAppBar() { return false; }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
        viewHolder.bind(getHeaderedModel());

        toggleProgress(false);
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean canEditGame() { return gofer.canEdit(); }

    @Override
    public void onUserClicked(User item) { updateCompetitor(item); }

    @Override
    public void onTeamClicked(Team item) { updateCompetitor(item); }

    @Override
    public void onAwayClicked(Competitor away) {
        if (!game.isEmpty()) return;
        if (game.getHome() == away) showSnackbar(getString(R.string.game_create_prompt));
        else pickAwaySide();
    }

    @Override
    protected void onPrepComplete() {
        scrollManager.notifyDataSetChanged();
        requireActivity().invalidateOptionsMenu();
        super.onPrepComplete();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.fab) return;

        if (game.isEmpty()) createGame();
        else new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.game_manual_score_request)
                .setMessage(R.string.game_end_prompt)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    toggleProgress(true);
                    toggleProgress(true);
                    disposables.add(gameViewModel.endGame(game).subscribe(diffResult -> requireActivity().onBackPressed(), defaultErrorHandler));
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void createGame() {
        toggleProgress(true);
        disposables.add(gofer.save().subscribe(ignored -> showFragment(GameFragment.newInstance(game)), defaultErrorHandler));
    }

    private void updateCompetitor(Competitive item) {
        game.getAway().updateEntity(item);
        scrollManager.notifyDataSetChanged();
        scrollManager.getRecyclerView().postDelayed(this::hideBottomSheet, 200);
        hideKeyboard();
    }

    private void pickAwaySide() {
        String refPath = game.getRefPath();
        boolean isBetweenUsers = User.COMPETITOR_TYPE.equals(refPath);
        BaseFragment fragment = isBetweenUsers
                ? UserSearchFragment.newInstance()
                : Team.COMPETITOR_TYPE.equals(refPath)
                ? TeamSearchFragment.newInstance(game.getSport())
                : null;

        if (fragment == null) return;
        fragment.setTargetFragment(this, R.id.request_competitor_pick);

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(fragment)
                .build());
    }
}
