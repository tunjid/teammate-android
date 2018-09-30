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
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

/**
 * Searches for teams
 */

public final class TeamSearchFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        TeamAdapter.AdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.team_list};
    public static final String ARG_SPORT = "sport-code";

    private View createTeam;
    private SearchView searchView;
    private TeamSearchRequest request;
    private InstantSearch<TeamSearchRequest, Team> instantSearch;

    private final List<Identifiable> teams = new ArrayList<>();

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
        setHasOptionsMenu(true);
        request = TeamSearchRequest.from(getArguments().getString(ARG_SPORT));
        instantSearch = teamViewModel.instantSearch();
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

        if (getTargetRequestCode() != 0) {
            teams.clear();
            disposables.add(Flowable.fromIterable(teamViewModel.getModelList(Team.class))
                    .filter(this::IsEligibleTeam)
                    .subscribe(teams::add, ErrorHandler.EMPTY));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        subscribeToSearch(request.query(searchView.getQuery().toString()));
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

    private void subscribeToSearch(TeamSearchRequest searchRequest) {
        if (instantSearch.postSearch(searchRequest)) return;
        disposables.add(instantSearch.subscribe()
                .doOnSubscribe(subscription -> subscribeToSearch(searchRequest))
                .subscribe(this::onTeamsUpdated, defaultErrorHandler));
    }

    private void onTeamsUpdated(List<Team> teams) {
        this.teams.clear();
        this.teams.addAll(teams);
        scrollManager.notifyDataSetChanged();
    }

    private boolean IsEligibleTeam(Identifiable team) {
        return TextUtils.isEmpty(request.getSport()) || (!(team instanceof Team)
                || ((Team) team).getSport().getCode().equals(request.getSport()));
    }
}
