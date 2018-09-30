package com.mainstreetcode.teammate.fragments.main;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;

public class HeadToHeadFragment extends MainActivityFragment
        implements
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener {

    private HeadToHeadRequest request;

    private TextView searchButton;
    private TextView searchTitle;
    private ViewGroup cardView;

    private List<Identifiable> matchUps;
    private ScrollManager searchScrollManager;

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

        View.OnClickListener searchClickListener = clicked -> toggleVisibility();

        cardView = root.findViewById(R.id.card_view_wrapper);
        searchTitle = root.findViewById(R.id.search_title);
        searchButton = root.findViewById(R.id.search);

        searchTitle.setOnClickListener(searchClickListener);
        searchButton.setOnClickListener(searchClickListener);

        setTitleIcon(false);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setToolbarTitle(getString(R.string.game_head_to_head));
    }

    @Override
    public void onDestroyView() {
        searchButton = null;
        searchTitle = null;
        cardView = null;
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

    @SuppressLint("ResourceAsColor")
    private void setTitleIcon(boolean isDown) {
        int resVal = isDown ? R.drawable.anim_vect_down_to_right_arrow : R.drawable.anim_vect_right_to_down_arrow;

        Drawable icon = AnimatedVectorDrawableCompat.create(searchTitle.getContext(), resVal);
        if (icon == null) return;

        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(searchTitle, null, null, icon, null);
    }

    private void changeVisibility(boolean inVisible) {
        TransitionManager.beginDelayedTransition(cardView, new AutoTransition());

        setTitleIcon(inVisible);

        AnimatedVectorDrawableCompat animatedDrawable = (AnimatedVectorDrawableCompat)
                TextViewCompat.getCompoundDrawablesRelative(searchTitle)[2];

        animatedDrawable.start();

        int visibility = inVisible ? View.GONE : View.VISIBLE;
        searchButton.setVisibility(visibility);
        searchScrollManager.getRecyclerView().setVisibility(visibility);
    }

    private void toggleVisibility() {
        View view = searchScrollManager.getRecyclerView();
        boolean invisible = view.getVisibility() == View.VISIBLE;
        changeVisibility(invisible);

        // Search
        if (invisible) fetchMatchUps();
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
                ? TeamSearchFragment.newInstance()
                : null;

        if (fragment == null) return;
        fragment.setTargetFragment(this, R.id.request_competitor_pick);

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(fragment)
                .build());
    }
}
