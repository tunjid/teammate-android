package com.mainstreetcode.teammates.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamSearchAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.viewmodel.TeamViewModel;

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
        TeamSearchAdapter.TeamAdapterListener {

    private RecyclerView recyclerView;
    private final List<Team> teams = new ArrayList<>();

    TeamViewModel viewModel;

    private final Consumer<List<Team>> teamConsumer = (teams) -> {
        this.teams.clear();
        this.teams.addAll(teams);
        recyclerView.getAdapter().notifyDataSetChanged();
    };
    private final ErrorHandler searchErroHandler = ErrorHandler.builder()
            .defaultMessage(Application.getInstance().getString(R.string.default_error))
            .add(this::showSnackbar)
            .build();

    public static TeamsFragment newInstance() {
        TeamsFragment fragment = new TeamsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(this).get(TeamViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teams, container, false);
        recyclerView = rootView.findViewById(R.id.team_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamSearchAdapter(teams, this));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toggleFab(false);
        setToolbarTitle(getString(R.string.my_teams));

        disposables.add(viewModel.getMyTeams().subscribe(teamConsumer, searchErroHandler));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    @Override
    public void onTeamClicked(Team team) {
        //showFragment(TeamDetailFragment.newInstance(team, false));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_team:
                //showFragment(TeamDetailFragment.newInstance(Team.empty(), true));
                break;
        }
    }
}
