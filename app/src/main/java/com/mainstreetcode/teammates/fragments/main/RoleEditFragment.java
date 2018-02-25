package com.mainstreetcode.teammates.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.RoleEditAdapter;
import com.mainstreetcode.teammates.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammates.model.HeaderedModel;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.util.ScrollManager;

/**
 * Edits a Team member
 */

public class RoleEditFragment extends HeaderedFragment
        implements
        View.OnClickListener,
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
                .withLayoutManager(new LinearLayoutManager(getContext()))
                .withAdapter(new RoleEditAdapter(role, roleViewModel.getRoleNames(), this))
                .withScrollListener((dx, dy) -> {if (Math.abs(dy) > 3) toggleFab(dy < 0);})
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(R.string.edit_user));

        User user = userViewModel.getCurrentUser();
        Team team = role.getTeam();

        disposables.add(localRoleViewModel.getRoleInTeam(user, team).subscribe(this::onRoleUpdated, defaultErrorHandler));
        roleViewModel.fetchRoleValues();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        User user = userViewModel.getCurrentUser();
        if (localRoleViewModel.hasPrivilegedRole() && !role.getUser().equals(user)) {
            inflater.inflate(R.menu.fragment_user_edit, menu);
        }
    }

    @Override
    public boolean drawsBehindStatusBar() {
        return true;
    }

    @Override
    public boolean showsFab() {
        return canChangeRole();
    }

    @Override
    protected HeaderedModel getHeaderedModel() {
        return role;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                String roleName = role.getName();

                if (TextUtils.isEmpty(roleName)) {
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
                final String firstName = role.getUser().getFirstName();
                final String prompt = getString(R.string.confirm_user_drop, firstName);

                Activity activity;
                if ((activity = getActivity()) == null) return super.onOptionsItemSelected(item);

                new AlertDialog.Builder(activity).setTitle(prompt)
                        .setPositiveButton(R.string.yes, (dialog, which) -> disposables.add(teamMemberViewModel.deleteRole(role).subscribe(this::onRoleDropped, defaultErrorHandler)))
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean canChangeRole() {
        return localRoleViewModel.hasPrivilegedRole();
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
