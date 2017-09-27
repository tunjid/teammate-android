package com.mainstreetcode.teammates.fragments.main;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.RoleEditAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Edits a Team member
 */

public class RoleEditFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        ImageWorkerFragment.CropListener,
        ImageWorkerFragment.ImagePickerListener {

    private static final String ARG_ROLE = "role";

    private Role role;
    private Role currentRole = Role.empty();
    private List<String> roles = new ArrayList<>();

    private RecyclerView recyclerView;

    public static RoleEditFragment newInstance(Role role) {
        RoleEditFragment fragment = new RoleEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        Role tempRole = getArguments().getParcelable(ARG_ROLE);

        return (tempRole != null)
                ? superResult + "-" + tempRole.hashCode()
                : superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        role = getArguments().getParcelable(ARG_ROLE);

        ImageWorkerFragment fragment = ImageWorkerFragment.newInstance();
        fragment.setTargetFragment(this, ImageWorkerFragment.CROP_CHOOSER);

        ImageWorkerFragment.attach(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_edit, container, false);
        recyclerView = rootView.findViewById(R.id.user_edit);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RoleEditAdapter(role, roles, this::canChangeRole, canEditRoleUserInfo(), this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (Math.abs(dy) < 3) return;
                toggleFab(dy < 0);
            }
        });

        recyclerView.requestFocus();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        User user = userViewModel.getCurrentUser();
        if (currentRole.isPrivilegedRole() && !role.getUser().equals(user)) {
            inflater.inflate(R.menu.fragment_user_edit, menu);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setOnClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(R.string.edit_user));

        User user = userViewModel.getCurrentUser();

        disposables.add(roleViewModel.getRoleInTeam(user.getId(), role.getTeamId())
                .subscribe(this::onRoleUpdated, defaultErrorHandler));

        disposables.add(roleViewModel.getRoleValues().subscribe(currentRoles -> {
            roles.clear();
            roles.addAll(currentRoles);
        }, emptyErrorHandler));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ImageWorkerFragment.detach(this);
    }

    @Override
    public boolean drawsBehindStatusBar() {
        return true;
    }

    @Override
    protected boolean showsFab() {
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:

                String roleName = role.getName();

                if (TextUtils.isEmpty(roleName)) {
                    showSnackbar("Please select a role");
                    return;
                }

                disposables.add(roleViewModel.updateRole(role).subscribe(updatedRole -> {
                            showSnackbar(getString(R.string.updated_user, role.getUser().getFirstName()));
                            recyclerView.getAdapter().notifyDataSetChanged();
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

                new AlertDialog.Builder(getContext()).setTitle(prompt)
                        .setPositiveButton(R.string.yes, (dialog, which) -> disposables.add(roleViewModel.dropRole(role).subscribe(dropped -> {
                            showSnackbar(getString(R.string.dropped_user, firstName));
                            getActivity().onBackPressed();
                        }, defaultErrorHandler)))
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageCropped(Uri uri) {
        role.get(Role.IMAGE_POSITION).setValue(uri.getPath());
        recyclerView.getAdapter().notifyItemChanged(User.IMAGE_POSITION);
    }

    @Override
    public void onImageClick() {
        ImageWorkerFragment.requestCrop(this);
    }

    private void onRoleUpdated(Role role) {
        currentRole.update(role);
        recyclerView.getRecycledViewPool().clear();
        recyclerView.getAdapter().notifyItemChanged(Role.ROLE_NAME_POSITION);
        getActivity().invalidateOptionsMenu();

    }

    private boolean canEditRoleUserInfo() {
        return role.getUser().equals(userViewModel.getCurrentUser());
    }

    private boolean canChangeRole() {
        return (!currentRole.isEmpty() && currentRole.isPrivilegedRole()) || canEditRoleUserInfo();
    }
}
