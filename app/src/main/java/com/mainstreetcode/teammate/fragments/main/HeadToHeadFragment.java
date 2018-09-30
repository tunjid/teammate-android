package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GameAdapter;
import com.mainstreetcode.teammate.adapters.HeadToHeadRequestAdapter;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.HeadToHeadRequest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ExpandingToolbar;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;

public class HeadToHeadFragment extends MainActivityFragment
        implements
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener {

    private HeadToHeadRequest request;
    private ExpandingToolbar expandingToolbar;
    private ScrollManager searchScrollManager;

    private List<Identifiable> matchUps;

    public static HeadToHeadFragment newInstance() {
        HeadToHeadFragment fragment = new HeadToHeadFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = HeadToHeadRequest.empty();
        matchUps = gameViewModel.getHeadToHeadMatchUps();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_head_to_head, container, false);

        searchScrollManager = ScrollManager.withRecyclerView(root.findViewById(R.id.search_options))
                .withAdapter(new HeadToHeadRequestAdapter(request, competitor -> findCompetitor()))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager = ScrollManager.withRecyclerView(root.findViewById(R.id.team_list))
                .withAdapter(new GameAdapter(matchUps, game -> showFragment(GameFragment.newInstance(game))))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        expandingToolbar = ExpandingToolbar.create(root.findViewById(R.id.card_view_wrapper), this::fetchMatchUps);
        expandingToolbar.setTitleIcon(false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setToolbarTitle(getString(R.string.game_head_to_head));
    }

    @Override
    public void onDestroyView() {
        expandingToolbar = null;
        searchScrollManager = null;
        super.onDestroyView();
    }

    @Override
    public boolean showsFab() { return false; }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        updateFabIcon();
        setFabClickListener(this);
    }

    @Override
    public void onUserClicked(User item) {

    }

    @Override
    public void onTeamClicked(Team item) {

    }

    private void fetchMatchUps() {
        disposables.add(gameViewModel.getMatchUps(request).subscribe(searchScrollManager::onDiff, defaultErrorHandler));
    }

    private void findCompetitor() {
        if (request.hasInvalidType()) {
            showSnackbar(getString(R.string.game_select_tournament_type));
            return;
        }

        String refPath = request.getRefPath();
        boolean isBetweenUsers = User.COMPETITOR_TYPE.equals(refPath);
        BaseFragment fragment = isBetweenUsers
                ? UserSearchFragment.newInstance()
                : Team.COMPETITOR_TYPE.equals(refPath)
                ? TeamSearchFragment.newInstance(request.getSport())
                : null;

        if (fragment == null) return;
        fragment.setTargetFragment(this, R.id.request_competitor_pick);

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(fragment)
                .build());
    }
}
