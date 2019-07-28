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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.TeamSearchAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamSearchRequest;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import io.reactivex.Flowable;

/**
 * Searches for teams
 */

public final class TeamSearchFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        TeamAdapter.AdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.list_layout};
    private static final String ARG_SPORT = "sport-code";

    private View createTeam;
    private SearchView searchView;
    private TeamSearchRequest request;
    private InstantSearch<TeamSearchRequest, Differentiable> instantSearch;

    public static TeamSearchFragment newInstance() {
        TeamSearchFragment fragment = new TeamSearchFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    public static TeamSearchFragment newInstance(Sport sport) {
        TeamSearchFragment fragment = newInstance();
        fragment.getArguments().putString(ARG_SPORT, sport.getCode());

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        String sportCode = getArguments().getString(ARG_SPORT);

        return sportCode == null ? superResult : superResult + "-" + sportCode;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = TeamSearchRequest.from(getArguments().getString(ARG_SPORT));
        instantSearch = teamViewModel.instantSearch();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_search, container, false);
        searchView = rootView.findViewById(R.id.searchView);
        createTeam = rootView.findViewById(R.id.create_team);

        scrollManager = ScrollManager.<InteractiveViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new TeamSearchAdapter(instantSearch.getCurrentItems(), this))
                .withGridLayoutManager(2)
                .build();

        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        createTeam.setOnClickListener(this);

        if (getTargetRequestCode() != 0) {
            List<Differentiable> items = instantSearch.getCurrentItems();
            items.clear();
            disposables.add(Flowable.fromIterable(teamViewModel.getModelList(Team.class))
                    .filter(this::IsEligibleTeam)
                    .subscribe(items::add, ErrorHandler.EMPTY));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        subscribeToSearch();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchView.clearFocus();
        searchView = null;
        createTeam = null;
    }

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    public boolean showsFab() { return false; }

    @Override
    public boolean showsToolBar() { return false; }

    @Override
    public void onTeamClicked(Team team) {
        Fragment target = getTargetFragment();
        boolean canPick = target instanceof TeamAdapter.AdapterListener;

        if (canPick) ((TeamAdapter.AdapterListener) target).onTeamClicked(team);
        else showFragment(JoinRequestFragment.joinInstance(team, userViewModel.getCurrentUser()));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.create_team) showFragment(TeamEditFragment.newCreateInstance());
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        if (getView() == null || TextUtils.isEmpty(queryText)) return true;
        instantSearch.postSearch(request.query(queryText));
        return true;
    }

    private void subscribeToSearch() {
        disposables.add(instantSearch.subscribe()
                .subscribe(scrollManager::onDiff, defaultErrorHandler));
    }

    private boolean IsEligibleTeam(Differentiable team) {
        return TextUtils.isEmpty(request.getSport()) || (!(team instanceof Team)
                || ((Team) team).getSport().getCode().equals(request.getSport()));
    }
}
