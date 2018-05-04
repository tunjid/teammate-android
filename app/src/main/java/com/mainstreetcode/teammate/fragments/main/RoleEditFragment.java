package com.mainstreetcode.teammate.fragments.main;

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
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.mainstreetcode.teammate.viewmodel.gofers.RoleGofer;
import com.mainstreetcode.teammate.viewmodel.gofers.TeamHostingGofer;

/**
 * Edits a Team member
 */

public class RoleEditFragment extends HeaderedFragment<Role>
        implements
        RoleEditAdapter.RoleEditAdapterListener {

    public static final String ARG_ROLE = "role";

    private Role role;
    private RoleGofer gofer;

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
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_ROLE));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        role = getArguments().getParcelable(ARG_ROLE);
        gofer = teamMemberViewModel.gofer(role);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withAdapter(new RoleEditAdapter(gofer.getItems(), this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
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
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return canChangeRole();}

    @Override
    protected Role getHeaderedModel() {return role;}

    @Override
    protected TeamHostingGofer<Role> gofer() { return gofer; }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) { }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (role.getPosition().isInvalid()) showSnackbar(getString(R.string.select_role));
                else updateRole();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_kick:
                showDropRolePrompt();
                return true;
            case R.id.action_block:
                blockUser(role.getUser(), role.getTeam());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean canChangeRole() {
        return gofer.hasPrivilegedRole() || userViewModel.getCurrentUser().equals(role.getUser());
    }

    private void showDropRolePrompt() {
        new AlertDialog.Builder(requireActivity()).setTitle(gofer.getDropRolePrompt(this))
                .setPositiveButton(R.string.yes, (dialog, which) -> disposables.add(gofer.remove().subscribe(this::onRoleDropped, defaultErrorHandler)))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateRole() {
        toggleProgress(true);
        disposables.add(gofer.save().subscribe(this::onRoleUpdated, defaultErrorHandler));
    }

    private void onRoleUpdated(DiffUtil.DiffResult result) {
        viewHolder.bind(getHeaderedModel());
        scrollManager.onDiff(result);
        toggleFab(showsFab());
        toggleProgress(false);
        requireActivity().invalidateOptionsMenu();
        showSnackbar(getString(R.string.updated_user, role.getUser().getFirstName()));
    }

    private void onRoleDropped() {
        showSnackbar(getString(R.string.dropped_user, role.getUser().getFirstName()));
        removeEnterExitTransitions();
        requireActivity().onBackPressed();
    }
}
