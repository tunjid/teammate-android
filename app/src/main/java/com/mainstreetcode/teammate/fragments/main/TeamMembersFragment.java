package com.mainstreetcode.teammate.fragments.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamMemberAdapter;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.ModelCardViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DiffUtil;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

/**
 * Displays a {@link Team team's} {@link User members}.
 */

public class TeamMembersFragment extends MainActivityFragment
        implements
        TeamMemberAdapter.UserAdapterListener {

    private static final String ARG_TEAM = "team";

    private Team team;
    private List<Differentiable> teamModels;

    public static TeamMembersFragment newInstance(Team team) {
        TeamMembersFragment fragment = new TeamMembersFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false);

        Runnable refreshAction = () -> disposables.add(teamMemberViewModel.refresh(team).subscribe(TeamMembersFragment.this::onTeamUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.<InteractiveViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withAdapter(new TeamMemberAdapter(teamModels, this))
                .addScrollListener((dx, dy) -> updateFabForScrollState(dy))
                .addScrollListener((dx, dy) -> updateTopSpacerElevation())
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withStaggeredGridLayoutManager(2)
                .build();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTeamMembers(true);
        watchForRoleChanges(team, this::togglePersistentUi);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean visible = showsFab();

        MenuItem editItem = menu.findItem(R.id.action_edit);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        MenuItem blockedItem = menu.findItem(R.id.action_blocked);
        MenuItem tournamentItem = menu.findItem(R.id.action_team_tournaments);

        editItem.setVisible(visible);
        deleteItem.setVisible(visible);
        blockedItem.setVisible(visible);
        tournamentItem.setVisible(team.getSport().supportsCompetitions());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_team_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                showFragment(TeamEditFragment.newEditInstance(team));
                return true;
            case R.id.action_team_tournaments:
                showFragment(TournamentsFragment.newInstance(team));
                return true;
            case R.id.action_blocked:
                showFragment(BlockedUsersFragment.newInstance(team));
                return true;
            case R.id.action_delete:
                Context context = getContext();
                if (context == null) return true;
                new AlertDialog.Builder(context).setTitle(getString(R.string.delete_team_prompt, team.getName()))
                        .setMessage(R.string.delete_team_prompt_body)
                        .setPositiveButton(R.string.yes, (dialog, which) -> deleteTeam())
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.invite_user; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_group_add_white_24dp; }

    @Override
    protected CharSequence getToolbarTitle() {
        return getTargetFragment() != null ? "" : getString(R.string.team_name_prefix, team.getName());
    }

    @Override
    public boolean showsFab() {
        return getTargetRequestCode() == 0 && localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onRoleClicked(Role role) {
        Fragment target = getTargetFragment();
        boolean canPick = target instanceof UserAdapter.AdapterListener;

        if (canPick) ((UserAdapter.AdapterListener) target).onUserClicked(role.getUser());
        else showFragment(RoleEditFragment.newInstance(role));
    }

    @Override
    public void onJoinRequestClicked(JoinRequest request) {
        Fragment target = getTargetFragment();
        boolean canPick = target instanceof UserAdapter.AdapterListener;

        if (canPick) showSnackbar(getString(R.string.stat_user_not_on_team));
        else showFragment(JoinRequestFragment.viewInstance(request));
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

    private void fetchTeamMembers(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(teamMemberViewModel.getMany(team, fetchLatest).subscribe(this::onTeamUpdated, defaultErrorHandler));
    }

    private void deleteTeam() {
        disposables.add(teamViewModel.deleteTeam(team).subscribe(deleted -> onTeamDeleted(), defaultErrorHandler));
    }

    private void onTeamUpdated(DiffUtil.DiffResult diffResult) {
        scrollManager.onDiff(diffResult);
        requireActivity().invalidateOptionsMenu();
    }

    private void onTeamDeleted() {
        showSnackbar(getString(R.string.deleted_team, team.getName()));
        removeEnterExitTransitions();

        Activity activity = getActivity();
        if (activity != null) activity.onBackPressed();
    }
}
