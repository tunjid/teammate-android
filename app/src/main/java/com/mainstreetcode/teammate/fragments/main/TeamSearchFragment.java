package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.TeamSearchAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamSearchRequest;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches for teams
 */

public final class TeamSearchFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        TeamAdapter.TeamAdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.team_list};
    public static final String ARG_TOURNAMENT = "tournament";

    private View createTeam;
    private SearchView searchView;
    private TeamSearchRequest request;
    private final List<Identifiable> teams = new ArrayList<>();

    public static TeamSearchFragment newInstance() {
        TeamSearchFragment fragment = new TeamSearchFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    public static TeamSearchFragment newInstance(Tournament tournament) {
        TeamSearchFragment fragment = newInstance();
        fragment.getArguments().putParcelable(ARG_TOURNAMENT, tournament);

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        request = TeamSearchRequest.from(getArguments().getParcelable(ARG_TOURNAMENT));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_search, container, false);
        searchView = rootView.findViewById(R.id.searchView);
        createTeam = rootView.findViewById(R.id.create_team);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new TeamSearchAdapter(teams, this))
                .withGridLayoutManager(2)
                .build();

        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        createTeam.setOnClickListener(this);

        if (getTargetFragment() != null) {
            teams.clear();
            teams.addAll(teamViewModel.getModelList(Team.class));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        postSearch(searchView.getQuery().toString());
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
        boolean canPick = target != null && target instanceof TeamAdapter.TeamAdapterListener;

        if (canPick) ((TeamAdapter.TeamAdapterListener) target).onTeamClicked(team);
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
        teamViewModel.postSearch(queryText);
        return true;
    }

    private void postSearch(String queryText) {
        if (teamViewModel.postSearch(queryText)) return;
        disposables.add(teamViewModel.findTeams(request)
                .doOnSubscribe(subscription -> postSearch(queryText))
                .subscribe(this::onTeamsUpdated, defaultErrorHandler));
    }

    private void onTeamsUpdated(List<Team> teams) {
        this.teams.clear();
        this.teams.addAll(teams);
        scrollManager.notifyDataSetChanged();
    }
}
