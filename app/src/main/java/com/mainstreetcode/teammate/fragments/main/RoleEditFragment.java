package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.RoleEditAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;

import io.reactivex.Flowable;

/**
 * Edits a Team member
 */

public class RoleEditFragment extends HeaderedFragment<Role>
        implements
        RoleEditAdapter.RoleEditAdapterListener {

    public static final String ARG_ROLE = "role";

    private Role role;

    public static RoleEditFragment newInstance(Role role) {
        RoleEditFragment fragment = new RoleEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_ROLE, role);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Role tempRole = getArguments().getParcelable(ARG_ROLE);

        return (tempRole != null)
                ? superResult + "-" + tempRole.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        role = getArguments().getParcelable(ARG_ROLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withAdapter(new RoleEditAdapter(role, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    public void onResume() {
        super.onResume();
        User user = userViewModel.getCurrentUser();
        Team team = role.getTeam();

        disposables.add(localRoleViewModel.getRoleInTeam(user, team).subscribe(this::onRoleUpdated, defaultErrorHandler));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_kick);
        item.setVisible(canChangeRole());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_user_edit, menu);
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(R.string.edit_role));
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return canChangeRole();}

    @Override
    protected Role getHeaderedModel() {return role;}

    @Override
    protected Flowable<DiffUtil.DiffResult> fetch(Role model) { return Flowable.empty(); }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) { }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (role.getPosition().isInvalid()) {
                    showSnackbar(getString(R.string.select_role));
                    return;
                }

                toggleProgress(true);
                disposables.add(teamMemberViewModel.updateRole(role).subscribe(updatedRole -> {
                    onRoleUpdated();
                    showSnackbar(getString(R.string.updated_user, role.getUser().getFirstName()));
                }, defaultErrorHandler));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_kick:
                User roleUser = role.getUser();
                final String prompt = userViewModel.getCurrentUser().equals(roleUser)
                        ? getString(R.string.confirm_user_leave)
                        : getString(R.string.confirm_user_drop, roleUser.getFirstName());

                Activity activity;
                if ((activity = getActivity()) == null) return super.onOptionsItemSelected(item);

                new AlertDialog.Builder(activity).setTitle(prompt)
                        .setPositiveButton(R.string.yes, (dialog, which) -> disposables.add(teamMemberViewModel.deleteRole(role).subscribe(this::onRoleDropped, defaultErrorHandler)))
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
            case R.id.action_block:
                blockUser(role.getUser(), role.getTeam());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean canChangeRole() {
        return localRoleViewModel.hasPrivilegedRole() || userViewModel.getCurrentUser().equals(role.getUser());
    }

    private void onRoleUpdated() {
        toggleProgress(false);
        viewHolder.bind(getHeaderedModel());
        scrollManager.notifyDataSetChanged();

        Activity activity;
        if ((activity = getActivity()) == null) return;

        activity.invalidateOptionsMenu();
        toggleFab(showsFab());
    }

    private void onRoleDropped(Role dropped) {
        showSnackbar(getString(R.string.dropped_user, dropped.getUser().getFirstName()));
        removeEnterExitTransitions();

        Activity activity;
        if ((activity = getActivity()) == null) return;

        activity.onBackPressed();
    }
}
