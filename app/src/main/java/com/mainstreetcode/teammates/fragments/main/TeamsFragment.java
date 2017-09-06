package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        Fragment target = getTargetFragment();
        if (target != null) superResult += (target.getTag() + getTargetRequestCode());
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
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new TeamAdapter(teams, this));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int requestCode = getTargetRequestCode();
        FloatingActionButton fab = getFab();
        fab.setImageResource(R.drawable.ic_add_white_24dp);
        fab.setOnClickListener(this);

        toggleFab(requestCode == 0);
        setToolbarTitle(getString(requestCode == R.id.request_event_team_pick
                || requestCode == R.id.request_media_team_pick
                ? R.string.pick_team
                : R.string.my_teams));

        String userId = userViewModel.getCurrentUser().getId();
        disposables.add(teamViewModel.getMyTeams(userId).subscribe(teamConsumer, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_teams, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                showFragment(TeamSearchFragment.newInstance());
                return true;
        }
        return super.onOptionsItemSelected(item);
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
