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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GameAdapter;
import com.mainstreetcode.teammate.adapters.HeadToHeadRequestAdapter;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.HeadToHead;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ExpandingToolbar;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

public class HeadToHeadFragment extends MainActivityFragment
        implements
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener,
        HeadToHeadRequestAdapter.AdapterListener {

    private boolean isHome = true;
    private HeadToHead.Request request;
    private ExpandingToolbar expandingToolbar;
    private ScrollManager searchScrollManager;

    private TextView wins;
    private TextView draws;
    private TextView losses;

    private List<Differentiable> matchUps;

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
        request = HeadToHead.Request.empty();
        matchUps = gameViewModel.getHeadToHeadMatchUps();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_head_to_head, container, false);

        wins = root.findViewById(R.id.wins);
        draws = root.findViewById(R.id.draws);
        losses = root.findViewById(R.id.losses);

        searchScrollManager = ScrollManager.<BaseViewHolder>with(root.findViewById(R.id.search_options))
                .withAdapter(new HeadToHeadRequestAdapter(request, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build();

        scrollManager = ScrollManager.<InteractiveViewHolder>with(root.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(root, R.drawable.ic_head_to_head_24dp, R.string.game_head_to_head_prompt))
                .withAdapter(new GameAdapter(matchUps, game -> showFragment(GameFragment.newInstance(game))))
                .withRefreshLayout(root.findViewById(R.id.refresh_layout), this::fetchMatchUps)
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        expandingToolbar = ExpandingToolbar.create(root.findViewById(R.id.card_view_wrapper), this::fetchMatchUps);
        expandingToolbar.setTitleIcon(false);
        expandingToolbar.setTitle(R.string.game_head_to_head_params);

        scrollManager.notifyDataSetChanged();

        updateHeadToHead(0, 0, 0);
        if (!restoredFromBackStack()) expandingToolbar.changeVisibility(false);

        return root;
    }

    @Override
    public void onDestroyView() {
        expandingToolbar = null;
        searchScrollManager = null;
        wins = draws = losses = null;
        super.onDestroyView();
    }

    @Override
    public boolean showsToolBar() { return false; }

    @Override
    public boolean showsFab() { return false; }

    @Override
    protected void onKeyBoardChanged(boolean appeared) {
        super.onKeyBoardChanged(appeared);
        if (!appeared && isBottomSheetShowing()) hideBottomSheet();
    }

    @Override
    public void onUserClicked(User item) { updateCompetitor(item); }

    @Override
    public void onTeamClicked(Team item) { updateCompetitor(item); }

    @Override
    public void onHomeClicked(Competitor home) {
        isHome = true;
        findCompetitor();
    }

    @Override
    public void onAwayClicked(Competitor away) {
        isHome = false;
        findCompetitor();
    }

    private void fetchMatchUps() {
        toggleProgress(true);
        disposables.add(gameViewModel.headToHead(request).subscribe(summary -> updateHeadToHead(summary.getWins(), summary.getDraws(), summary.getLosses()), ErrorHandler.EMPTY));
        disposables.add(gameViewModel.getMatchUps(request).subscribe(diffResult -> {
            toggleProgress(false);
            scrollManager.onDiff(diffResult);
        }, defaultErrorHandler));
    }

    private void updateHeadToHead(int numWins, int numDraws, int numLosses) {
        wins.setText(getText(R.string.game_wins, numWins));
        draws.setText(getText(R.string.game_draws, numDraws));
        losses.setText(getText(R.string.game_losses, numLosses));
    }

    private void updateCompetitor(Competitive item) {
        if (isHome) request.updateHome(item);
        else request.updateAway(item);
        searchScrollManager.notifyDataSetChanged();
        hideKeyboard();
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

    private CharSequence getText(@StringRes int stringRes, int count) {
        return SpanBuilder.of(String.valueOf(count)).resize(1.4F).bold()
                .append(SpanBuilder.of(getString(stringRes))
                        .prependNewLine()
                        .build()).build();
    }
}
