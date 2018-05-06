package com.mainstreetcode.teammate.fragments.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.ModelCardViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

/**
 * Displays a {@link Team team's} {@link User members}.
 */

public class TeamDetailFragment extends MainActivityFragment
        implements
        TeamDetailAdapter.UserAdapterListener {

    private static final String ARG_TEAM = "team";

    private Team team;
    private List<Identifiable> teamModels;

    public static TeamDetailFragment newInstance(Team team) {
        TeamDetailFragment fragment = new TeamDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Team tempTeam = getArguments().getParcelable(ARG_TEAM);

        return tempTeam == null ? superResult : superResult + "-" + tempTeam.hashCode();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        team = getArguments().getParcelable(ARG_TEAM);
        teamModels = teamMemberViewModel.getModelList(team);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_detail, container, false);

        Runnable refreshAction = () -> disposables.add(teamMemberViewModel.refresh(team).subscribe(TeamDetailFragment.this::onTeamUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_detail))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withAdapter(new TeamDetailAdapter(teamModels, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .addScrollListener(this::updateFabOnScroll)
                .withStaggeredGridLayoutManager(2)
                .build();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCurrentRole();

        fetchTeamMembers(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_team_detail, menu);
        boolean visible = showsFab();

        MenuItem editItem = menu.findItem(R.id.action_edit);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        MenuItem blockedItem = menu.findItem(R.id.action_blocked);

        editItem.setVisible(visible);
        deleteItem.setVisible(visible);
        blockedItem.setVisible(visible);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                showFragment(TeamEditFragment.newEditInstance(team));
                return true;
            case R.id.action_blocked:
                showFragment(BlockedUsersFragment.newInstance(team));
                return true;
            case R.id.action_delete:
                Context context = getContext();
                if (context == null) return true;
                new AlertDialog.Builder(context).setTitle(getString(R.string.delete_team_prompt, team.getName()))
                        .setPositiveButton(R.string.yes, (dialog, which) -> deleteTeam())
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_group_add_white_24dp);
        setToolbarTitle(getString(R.string.team_name_prefix, team.getName()));
    }

    @Override
    public boolean showsFab() {
        return localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onRoleClicked(Role role) {
        View rootView = getView();
        if (rootView == null) return;

        showFragment(RoleEditFragment.newInstance(role));
    }

    @Override
    public void onJoinRequestClicked(JoinRequest request) {
        showFragment(JoinRequestFragment.viewInstance(request));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (localRoleViewModel.hasPrivilegedRole())
                    showFragment(JoinRequestFragment.inviteInstance(team));
                break;
        }
    }

    @Override
    @Nullable
    @SuppressLint("CommitTransaction")
    @SuppressWarnings("ConstantConditions")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        if (fragmentTo.getStableTag().contains(RoleEditFragment.class.getSimpleName())) {
            Role role = fragmentTo.getArguments().getParcelable(RoleEditFragment.ARG_ROLE);
            if (role == null) return null;

            ModelCardViewHolder holder = (ModelCardViewHolder) scrollManager.findViewHolderForItemId(role.hashCode());
            if (holder == null) return null;

            return beginTransaction()
                    .addSharedElement(holder.itemView, getTransitionName(role, R.id.fragment_header_background))
                    .addSharedElement(holder.getThumbnail(), getTransitionName(role, R.id.fragment_header_thumbnail));
        }
        if (fragmentTo.getStableTag().contains(JoinRequestFragment.class.getSimpleName())) {
            JoinRequest request = fragmentTo.getArguments().getParcelable(JoinRequestFragment.ARG_JOIN_REQUEST);
            if (request == null) return null;

            ModelCardViewHolder holder = (ModelCardViewHolder) scrollManager.findViewHolderForItemId(request.hashCode());
            if (holder == null) return null;

            return beginTransaction()
                    .addSharedElement(holder.itemView, getTransitionName(request, R.id.fragment_header_background))
                    .addSharedElement(holder.getThumbnail(), getTransitionName(request, R.id.fragment_header_thumbnail));
        }
        return super.provideFragmentTransaction(fragmentTo);
    }

    void fetchTeamMembers(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(teamMemberViewModel.getMany(team, fetchLatest).subscribe(this::onTeamUpdated, defaultErrorHandler));
    }

    private void deleteTeam() {
        disposables.add(teamViewModel.deleteTeam(team).subscribe(deleted -> onTeamDeleted(), defaultErrorHandler));
    }

    private void onTeamUpdated(DiffUtil.DiffResult diffResult) {
        updateCurrentRole();
        scrollManager.onDiff(diffResult);
        Activity activity = getActivity();
        if (activity != null) activity.invalidateOptionsMenu();
    }

    private void onTeamDeleted() {
        showSnackbar(getString(R.string.deleted_team, team.getName()));
        removeEnterExitTransitions();

        Activity activity = getActivity();
        if (activity != null) activity.onBackPressed();
    }

    private void updateCurrentRole() {
        if (team.isEmpty()) return;

        disposables.add(localRoleViewModel.getRoleInTeam(userViewModel.getCurrentUser(), team)
                .subscribe(this::onRoleUpdated, ErrorHandler.EMPTY));
    }

    private void onRoleUpdated() {
        toggleFab(showsFab());
        Activity activity = getActivity();
        if (activity != null) activity.invalidateOptionsMenu();
    }
}
