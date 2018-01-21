package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Team;

import java.util.List;

/**
 * Searches for teams
 */

public final class TeamsFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        TeamAdapter.TeamAdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.team_list};

    private RecyclerView recyclerView;
    private EmptyViewHolder emptyViewHolder;

    private List<Identifiable> teams;

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
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(new TeamAdapter(teams, this));

        emptyViewHolder = new EmptyViewHolder(rootView, R.drawable.ic_group_black_24dp, R.string.no_team);

        View altToolbar = rootView.findViewById(R.id.alt_toolbar);
        altToolbar.setVisibility(isTeamPicker() ? View.VISIBLE : View.INVISIBLE);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setFabIcon(R.drawable.ic_add_white_24dp);
        setFabClickListener(this);
        if (!isTeamPicker()) setToolbarTitle(getString(R.string.my_teams));

        String userId = userViewModel.getCurrentUser().getId();
        disposables.add(teamViewModel.getMyTeams(userId).subscribe(this::onTeamsUpdated, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_teams, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_search);
        if (item != null && isTeamPicker()) item.setVisible(false);
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
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    public boolean showsBottomNav() {
        return true;
    }

    @Override
    public boolean showsFab() {
        return !isTeamPicker();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onTeamClicked(Team team) {
        Fragment target = getTargetFragment();
        boolean canPick = target != null && target instanceof TeamAdapter.TeamAdapterListener;

        if (canPick) ((TeamAdapter.TeamAdapterListener) target).onTeamClicked(team);
        else showFragment(TeamDetailFragment.newInstance(team));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                toggleBottomSheet(false);
                showFragment(TeamEditFragment.newCreateInstance());
                break;
        }
    }

    private void onTeamsUpdated(DiffUtil.DiffResult result) {
        boolean isEmpty = teams.isEmpty();
        emptyViewHolder.toggle(isEmpty);
        result.dispatchUpdatesTo(recyclerView.getAdapter());
        if (isTeamPicker()) toggleFab(isEmpty);
    }

    private boolean isTeamPicker() {
        return getTargetRequestCode() != 0;
    }
}
