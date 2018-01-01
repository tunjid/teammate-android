package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
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
import com.mainstreetcode.teammates.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Team;

import java.util.List;

/**
 * Searches for teams
 */

public final class TeamsFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        TeamAdapter.TeamAdapterListener {

    private RecyclerView recyclerView;
    private EmptyViewHolder emptyViewHolder;

    private List<Team> teams;

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
        setHasOptionsMenu(true);

        teams = teamViewModel.getModelList(Team.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teams, container, false);
        recyclerView = rootView.findViewById(R.id.team_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new TeamAdapter(teams, this));

        emptyViewHolder = new EmptyViewHolder(rootView, R.drawable.ic_group_black_24dp, R.string.no_team);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int requestCode = getTargetRequestCode();
        setFabIcon(R.drawable.ic_add_white_24dp);
        setFabClickListener(this);
        setToolbarTitle(getString(requestCode == R.id.request_event_team_pick
                || requestCode == R.id.request_media_team_pick
                || requestCode == R.id.request_chat_team_pick
                ? R.string.pick_team
                : R.string.my_teams));

        String userId = userViewModel.getCurrentUser().getId();
        disposables.add(teamViewModel.getMyTeams(userId).subscribe(this::onTeamsUpdated, defaultErrorHandler));
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
        emptyViewHolder = null;
    }

    @Override
    protected boolean showsBottomNav() {
        return getTargetRequestCode() == 0;
    }

    @Override
    protected boolean showsFab() {
        return getTargetRequestCode() == 0;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onTeamClicked(Team team) {

        int requestCode = getTargetRequestCode();
        Fragment target = getTargetFragment();

        if (target != null && target instanceof TeamAdapter.TeamAdapterListener) {
            ((TeamAdapter.TeamAdapterListener) target).onTeamClicked(team);
            if (requestCode == R.id.request_event_edit_pick) getActivity().onBackPressed();
        }
        else {
            showFragment(TeamDetailFragment.newInstance(team));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                showFragment(TeamEditFragment.newCreateInstance());
                break;
        }
    }

    private void onTeamsUpdated(DiffUtil.DiffResult result) {
        result.dispatchUpdatesTo(recyclerView.getAdapter());
        emptyViewHolder.toggle(teams.isEmpty());
    }
}
