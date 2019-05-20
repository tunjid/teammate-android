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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StatAggregateAdapter;
import com.mainstreetcode.teammate.adapters.StatAggregateRequestAdapter;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.StatAggregate;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ExpandingToolbar;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

public class StatAggregateFragment extends MainActivityFragment
        implements
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener,
        StatAggregateRequestAdapter.AdapterListener {

    private StatAggregate.Request request;
    private ExpandingToolbar expandingToolbar;
    private ScrollManager searchScrollManager;

    private List<Differentiable> items;

    public static StatAggregateFragment newInstance() {
        StatAggregateFragment fragment = new StatAggregateFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        fragment.setEnterExitTransitions();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = StatAggregate.Request.empty();
        items = statViewModel.getStatAggregates();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stat_aggregate, container, false);

        searchScrollManager = ScrollManager.<BaseViewHolder>with(root.findViewById(R.id.search_options))
                .withAdapter(new StatAggregateRequestAdapter(request, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build();

        scrollManager = ScrollManager.<InteractiveViewHolder>with(root.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(root, R.drawable.ic_stat_white_24dp, R.string.stat_aggregate_empty))
                .withRefreshLayout(root.findViewById(R.id.refresh_layout), this::fetchAggregates)
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new StatAggregateAdapter(items))
                .withLinearLayoutManager()
                .build();

        expandingToolbar = ExpandingToolbar.create(root.findViewById(R.id.card_view_wrapper), this::fetchAggregates);
        expandingToolbar.setTitleIcon(false);
        expandingToolbar.setTitle(R.string.stat_aggregate_get);

        scrollManager.notifyDataSetChanged();

        if (!restoredFromBackStack()) expandingToolbar.changeVisibility(false);

        return root;
    }

    @Override
    public void onDestroyView() {
        expandingToolbar = null;
        searchScrollManager = null;
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
    public void onUserPicked(User item) {
        pick(UserSearchFragment.newInstance());
    }

    @Override
    public void onTeamPicked(Team item) {
        pick(TeamSearchFragment.newInstance(request.getSport()));
    }

    @Override
    public void onTeamClicked(Team item) {
        updateEntity(item);
    }

    @Override
    public void onUserClicked(User item) {
        updateEntity(item);
    }

    private void fetchAggregates() {
        toggleProgress(true);
        disposables.add(statViewModel.aggregate(request).subscribe(result -> {
            toggleProgress(false);
            scrollManager.onDiff(result);
        }, defaultErrorHandler));
    }

    private void updateEntity(Competitive item) {
        if (item instanceof User) request.updateUser((User) item);
        else if (item instanceof Team) request.updateTeam((Team) item);
        else return;

        searchScrollManager.notifyDataSetChanged();
        hideKeyboard();
    }

    private void pick(BaseFragment fragment) {
        fragment.setTargetFragment(this, R.id.request_competitor_pick);
        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(fragment)
                .build());
    }

}
