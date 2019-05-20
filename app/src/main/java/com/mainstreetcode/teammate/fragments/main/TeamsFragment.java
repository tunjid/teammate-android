/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

/**
 * Searches for teams
 */

public final class TeamsFragment extends MainActivityFragment
        implements
        TeamAdapter.AdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.list_layout};

    private List<Differentiable> roles;

    public static TeamsFragment newInstance() {
        TeamsFragment fragment = new TeamsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        Fragment target = getTargetFragment();
        if (target != null) superResult += ("-" + target.getTag() + "-" + getTargetRequestCode());
        return superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roles = roleViewModel.getModelList(Role.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false);

        Runnable refreshAction = () -> disposables.add(roleViewModel.refresh(Role.class).subscribe(TeamsFragment.this::onTeamsUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.<InteractiveViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(rootView, getEmptyDrawable(), getEmptyText()))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .addScrollListener((dx, dy) -> updateFabForScrollState(dy))
                .addScrollListener((dx, dy) -> updateTopSpacerElevation())
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new TeamAdapter(roles, this))
                .withStaggeredGridLayoutManager(2)
                .build();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTeams();
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.team_search_create; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_search_white_24dp; }

    @Override
    protected CharSequence getToolbarTitle() { return getString(R.string.my_teams); }

    @Override
    public int[] staticViews() { return EXCLUDED_VIEWS; }

    @Override
    public boolean showsBottomNav() { return true; }

    @Override
    public boolean showsFab() { return !isTeamPicker() || roles.isEmpty(); }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onTeamClicked(Team team) {
        Fragment target = getTargetFragment();
        boolean canPick = target instanceof TeamAdapter.AdapterListener;

        if (canPick) ((TeamAdapter.AdapterListener) target).onTeamClicked(team);
        else {
            teamViewModel.updateDefaultTeam(team);
            showFragment(TeamMembersFragment.newInstance(team));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                hideBottomSheet();
                showFragment(TeamSearchFragment.newInstance());
                break;
        }
    }

    private void fetchTeams() {
        scrollManager.setRefreshing();
        disposables.add(roleViewModel.getMore(Role.class).subscribe(this::onTeamsUpdated, defaultErrorHandler));
    }

    private void onTeamsUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
        togglePersistentUi();
    }

    private boolean isTeamPicker() {
        return getTargetRequestCode() != 0;
    }

    @DrawableRes
    private int getEmptyDrawable() {
        switch (getTargetRequestCode()) {
            case R.id.request_chat_team_pick:
                return R.drawable.ic_message_black_24dp;
            case R.id.request_game_team_pick:
                return R.drawable.ic_score_white_24dp;
            case R.id.request_event_team_pick:
                return R.drawable.ic_event_white_24dp;
            case R.id.request_media_team_pick:
                return R.drawable.ic_video_library_black_24dp;
            case R.id.request_tournament_team_pick:
                return R.drawable.ic_trophy_white_24dp;
            default:
                return R.drawable.ic_group_black_24dp;
        }
    }

    @StringRes
    private int getEmptyText() {
        switch (getTargetRequestCode()) {
            case R.id.request_event_team_pick:
                return R.string.no_team_event;
            case R.id.request_chat_team_pick:
                return R.string.no_team_chat;
            case R.id.request_media_team_pick:
                return R.string.no_team_media;
            case R.id.request_tournament_team_pick:
                return R.string.no_team_tournament;
            default:
                return R.string.no_team;
        }
    }
}
