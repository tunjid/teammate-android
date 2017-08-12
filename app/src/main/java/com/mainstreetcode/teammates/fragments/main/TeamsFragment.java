package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Team;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Searches for teams
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public final class TeamsFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        TeamAdapter.TeamAdapterListener {

    private RecyclerView recyclerView;
    private final List<Team> teams = new ArrayList<>();

    private final Consumer<List<Team>> teamConsumer = (teams) -> {
        this.teams.clear();
        this.teams.addAll(teams);
        recyclerView.getAdapter().notifyDataSetChanged();
    };

    public static TeamsFragment newInstance() {
        TeamsFragment fragment = new TeamsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        if (getTargetFragment() != null) superResult += getTargetRequestCode();
        return superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teams, container, false);
        recyclerView = rootView.findViewById(R.id.team_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamAdapter(teams, this));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setImageResource(R.drawable.ic_add_white_24dp);
        fab.setOnClickListener(this);
        toggleFab(true);
        setToolbarTitle(getString(getTargetRequestCode() == R.id.request_code_team_pick
                ? R.string.pick_team
                : R.string.my_teams));

        String userId = userViewModel.getCurrentUser().getId();
        disposables.add(teamViewModel  .getMyTeams(userId).subscribe(teamConsumer, defaultErrorHandler));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    @Override
    public void onTeamClicked(Team team) {
        Fragment target = getTargetFragment();
        if (target != null && target instanceof TeamAdapter.TeamAdapterListener) {
            ((TeamAdapter.TeamAdapterListener) target).onTeamClicked(team);
            getActivity().onBackPressed();
        }
        else {
            showFragment(TeamDetailFragment.newInstance(team));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                showFragment(TeamEditFragment.newInstance(Team.empty(), true));
                break;
        }
    }
}
