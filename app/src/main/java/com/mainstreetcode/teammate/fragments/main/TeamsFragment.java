package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.List;

/**
 * Searches for teams
 */

public final class TeamsFragment extends MainActivityFragment
        implements
        TeamAdapter.TeamAdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.team_list};

    private List<Identifiable> roles;

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
        roles = roleViewModel.getModelList(Role.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teams, container, false);

        Runnable refreshAction = () -> disposables.add(roleViewModel.refresh(Role.class).subscribe(TeamsFragment.this::onTeamsUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_group_black_24dp, R.string.no_team))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new TeamAdapter(roles, this))
                .withStaggeredGridLayoutManager(2)
                .build();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTeams();
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
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_search_white_24dp);
        setToolbarTitle(getString(isTeamPicker() ? R.string.pick_team : R.string.my_teams));
    }

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    public boolean showsBottomNav() {return true;}

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
                showFragment(TeamSearchFragment.newInstance());
                break;
        }
    }

    private void fetchTeams() {
        scrollManager.setRefreshing();
        disposables.add(roleViewModel.getMore(Role.class).subscribe(this::onTeamsUpdated, defaultErrorHandler));
    }

    private void onTeamsUpdated(DiffUtil.DiffResult result) {
        boolean isEmpty = roles.isEmpty();
        if (isTeamPicker()) toggleFab(isEmpty);
        scrollManager.onDiff(result);
    }

    private boolean isTeamPicker() {
        return getTargetRequestCode() != 0;
    }
}
