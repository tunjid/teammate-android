package com.mainstreetcode.teammates.fragments.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.ModelCardViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleSelectViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Flowable;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;

/**
 * Displays a {@link Team team's} {@link User members}.
 */

public class TeamDetailFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        TeamDetailAdapter.UserAdapterListener {

    private static final String ARG_TEAM = "team";

    private Team team;

    private Role currentRole = Role.empty();
    private final List<String> availableRoles = new ArrayList<>();
    private final List<Model> teamModels = new ArrayList<>();

    private RecyclerView recyclerView;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_detail, container, false);
        EditText editText = rootView.findViewById(R.id.team_name);
        editText.setText(team.getName());

        recyclerView = rootView.findViewById(R.id.team_detail);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new TeamDetailAdapter(teamModels, this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!showsFab()) return;
                if (Math.abs(dy) < 3) return;
                toggleFab(dy < 0);
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setOnClickListener(this);
        setFabIcon(R.drawable.ic_group_add_white_24dp);
        setToolbarTitle(getString(R.string.team_name_prefix, team.getName()));
        updateCurrentRole();

        disposables.add(roleViewModel.getRoleValues().subscribe(this::onRolesUpdated, ErrorHandler.EMPTY));
        disposables.add(teamViewModel.getTeam(team, teamModels).subscribe(this::onTeamUpdated, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_team_detail, menu);
        boolean visible = showsFab();

        MenuItem editItem = menu.findItem(R.id.action_edit);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);

        editItem.setVisible(visible);
        deleteItem.setVisible(visible);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                showFragment(TeamEditFragment.newEditInstance(team));
                return true;
            case R.id.action_delete:
                new AlertDialog.Builder(recyclerView.getContext()).setTitle(getString(R.string.delete_team_prompt, team.getName()))
                        .setPositiveButton(R.string.yes, (dialog, which) -> deleteTeam())
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
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
    protected boolean showsFab() {
        return currentRole.isPrivilegedRole();
    }

    @Override
    public void onRoleClicked(Role role) {
        View rootView = getView();
        if (rootView == null) return;

        if (team.getRoles().contains(role)) showFragment(RoleEditFragment.newInstance(role));
    }

    @Override
    public void onJoinRequestClicked(JoinRequest request) {
        View rootView = getView();
        if (rootView == null) return;
        if (!currentRole.isPrivilegedRole()) return;

        if (request.isUserApproved() && !request.isTeamApproved()) {
            new AlertDialog.Builder(recyclerView.getContext()).setTitle(getString(R.string.add_user_to_team, request.getUser().getFirstName()))
                    .setPositiveButton(R.string.yes, (dialog, which) -> approveUser(request, true))
                    .setNegativeButton(R.string.no, (dialog, which) -> approveUser(request, false))
                    .show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (showsFab()) inviteUser();
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

            ModelCardViewHolder holder = (ModelCardViewHolder) recyclerView.findViewHolderForItemId(role.hashCode());
            if (holder == null) return null;

            return beginTransaction()
                    .addSharedElement(holder.itemView, getTransitionName(role, R.id.fragment_header_background))
                    .addSharedElement(holder.getThumbnail(), getTransitionName(role, R.id.fragment_header_thumbnail));
        }
        return super.provideFragmentTransaction(fragmentTo);
    }

    private void approveUser(final JoinRequest request, final boolean approve) {
        Flowable<DiffUtil.DiffResult> resultFlowable = approve
                ? roleViewModel.approveUser(request, team, teamModels)
                : roleViewModel.declineUser(request, team, teamModels);

        disposables.add(resultFlowable.subscribe(diffResult -> onJoinAction(diffResult, request, approve), defaultErrorHandler));
    }

    private void deleteTeam() {
        disposables.add(teamViewModel.deleteTeam(team).subscribe(deleted -> onTeamDeleted(), defaultErrorHandler));
    }

    private void onTeamUpdated(DiffUtil.DiffResult diffResult) {
        updateCurrentRole();
        diffResult.dispatchUpdatesTo(recyclerView.getAdapter());

        Activity activity = getActivity();
        if (activity != null) activity.invalidateOptionsMenu();
    }

    private void onRolesUpdated(List<String> values) {
        availableRoles.clear();
        availableRoles.addAll(values);
    }

    private void onTeamDeleted() {
        showSnackbar(getString(R.string.deleted_team, team.getName()));
        removeEnterExitTransitions();

        Activity activity = getActivity();
        if (activity != null) activity.invalidateOptionsMenu();
    }

    private void updateCurrentRole() {
        if (team.isEmpty()) return;

        disposables.add(localRoleViewModel.getRoleInTeam(userViewModel.getCurrentUser(), team)
                .subscribe(this::onRoleUpdated, ErrorHandler.EMPTY));
    }

    private void onJoinAction(DiffUtil.DiffResult result, JoinRequest request, boolean approve) {
        result.dispatchUpdatesTo(recyclerView.getAdapter());

        int stringResource = approve ? R.string.added_user : R.string.removed_user;
        String name = request.getUser().getFirstName();
        showSnackbar(getString(stringResource, name));
    }

    private void onRoleUpdated(Role role) {
        currentRole.update(role);
        toggleFab(showsFab());
        Activity activity = getActivity();
        if (activity != null) activity.invalidateOptionsMenu();
    }

    @SuppressLint("InflateParams")
    private void inviteUser() {
        Context context = recyclerView.getContext();
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_invite_user, null);
        View inviteButton = dialogView.findViewById(R.id.invite);

        final EditText firstNameText = dialogView.findViewById(R.id.first_name);
        final EditText lastNameText = dialogView.findViewById(R.id.last_name);
        final EditText emailText = dialogView.findViewById(R.id.email);
        final TextInputLayout roleText = dialogView.findViewById(R.id.input_layout);

        final AtomicReference<String> roleReference = new AtomicReference<>();

        RoleSelectViewHolder holder = new RoleSelectViewHolder(dialogView, availableRoles, () -> true);
        Item<String> item = new Item<>(Item.ROLE, R.string.team_role, R.string.team_role, "", roleReference::set, "");

        holder.bind(item);

        final Dialog dialog = new AlertDialog.Builder(context).setTitle("").setView(dialogView).show();

        inviteButton.setOnClickListener(view -> {

            if (!validator.isNotEmpty(firstNameText)
                    || !validator.isNotEmpty(lastNameText)
                    || !validator.isValidEmail(emailText)
                    || !validator.isNotEmpty(roleText.getEditText())) return;

            String firstName = firstNameText.getText().toString();
            String lastName = lastNameText.getText().toString();
            String email = emailText.getText().toString();

            JoinRequest joinRequest = JoinRequest.invite(roleReference.get(), team, firstName, lastName, email);

            disposables.add(roleViewModel.joinTeam(joinRequest)
                    .subscribe(request -> showSnackbar(getString(R.string.user_invite_sent)), defaultErrorHandler));

            dialog.dismiss();
        });
    }
}
