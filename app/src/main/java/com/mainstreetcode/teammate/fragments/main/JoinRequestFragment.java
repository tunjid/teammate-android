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
import com.mainstreetcode.teammate.adapters.JoinRequestAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;

import io.reactivex.Flowable;

/**
 * Invites a Team member
 */

public class JoinRequestFragment extends HeaderedFragment<JoinRequest>
        implements
        JoinRequestAdapter.AdapterListener {

    private static final int INVITING = 0;
    private static final int APPROVING = 1;
    private static final int ACCEPTING = 2;
    private static final int WAITING = 3;

    public static final String ARG_JOIN_REQUEST = "join-request";

    private int state;
    private JoinRequest joinRequest;

    public static JoinRequestFragment inviteInstance(Team team) {
        JoinRequestFragment fragment = newInstance(JoinRequest.invite(team));
        fragment.setEnterExitTransitions();

        return fragment;
    }

    public static JoinRequestFragment viewInstance(JoinRequest request) {
        JoinRequestFragment fragment = newInstance(request);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    private static JoinRequestFragment newInstance(JoinRequest joinRequest) {
        JoinRequestFragment fragment = new JoinRequestFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_JOIN_REQUEST, joinRequest);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }


    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        JoinRequest temp = getArguments().getParcelable(ARG_JOIN_REQUEST);

        return (temp != null)
                ? superResult + "-" + temp.getTeam().hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        joinRequest = getArguments().getParcelable(ARG_JOIN_REQUEST);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withAdapter(new JoinRequestAdapter(joinRequest, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onResume() {
        User user = userViewModel.getCurrentUser();
        Team team = joinRequest.getTeam();
        disposables.add(localRoleViewModel.getRoleInTeam(user, team).subscribe(this::onRoleUpdated, defaultErrorHandler));
        checkState();
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_user_edit, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean canBlockUser = localRoleViewModel.hasPrivilegedRole();
        boolean canDeleteRequest = canBlockUser || isRequestOwner();

        MenuItem block_item = menu.findItem(R.id.action_block);
        MenuItem deleteItem = menu.findItem(R.id.action_kick);

        block_item.setVisible(canBlockUser);
        deleteItem.setVisible(canDeleteRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_kick:
                User requestUser = joinRequest.getUser();
                final String prompt = isRequestOwner()
                        ? getString(R.string.clarify_invitation, joinRequest.getTeam().getName())
                        : getString(R.string.confirm_request_drop, requestUser.getFirstName());

                new AlertDialog.Builder(requireActivity()).setTitle(prompt)
                        .setPositiveButton(R.string.yes, (dialog, which) -> processJoinRequest(false))
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
            case R.id.action_block:
                blockUser(joinRequest.getUser(), joinRequest.getTeam());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean showsFab() {
        if (state == INVITING) return true;
        else if (state == APPROVING) return localRoleViewModel.hasPrivilegedRole();
        else if (state == ACCEPTING) return isRequestOwner();
        else return false;
    }

    @Override
    public void onImageClick() {}

    @Override
    protected JoinRequest getHeaderedModel() {return joinRequest;}

    @Override
    protected Flowable<DiffUtil.DiffResult> fetch(JoinRequest model) { return Flowable.empty(); }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) { }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (state == WAITING) return;
                if (state == APPROVING || state == ACCEPTING) {
                    toggleProgress(true);
                    processJoinRequest(true);
                    return;
                }
                if (joinRequest.getPosition().isInvalid()) {
                    showSnackbar(getString(R.string.select_role));
                    return;
                }

                toggleProgress(true);
                disposables.add(roleViewModel.joinTeam(joinRequest)
                        .subscribe(request -> onJoinRequestSent(), defaultErrorHandler));
                break;
        }
    }

    private void onJoinRequestSent() {
        toggleProgress(false);
        showSnackbar(getString(R.string.user_invite_sent));
        toggleBottomSheet(false);
    }

    private void checkState() {
        state = joinRequest.isEmpty() ? INVITING : isRequestOwner() ? WAITING : joinRequest.isUserApproved() ? APPROVING : ACCEPTING;

        switch (state) {
            case INVITING:
                setToolbarTitle(getString(R.string.invite_user));
                break;
            case WAITING:
                setToolbarTitle(getString(R.string.pending_request));
                break;
            case APPROVING:
                setToolbarTitle(getString(R.string.approve_request));
                break;
            case ACCEPTING:
                setToolbarTitle(getString(R.string.accept_request));
                break;
        }
        toggleFab(showsFab());
        scrollManager.notifyDataSetChanged();
    }

    private void processJoinRequest(boolean approved) {
        disposables.add(teamMemberViewModel.processJoinRequest(joinRequest, approved)
                .subscribe(deleted -> onRequestProcessed(approved), defaultErrorHandler));
    }

    private void onRequestProcessed(boolean approved) {
        int stringResource = approved ? R.string.added_user : R.string.removed_user;
        String name = joinRequest.getUser().getFirstName();
        showSnackbar(getString(stringResource, name));
        requireActivity().onBackPressed();
    }

    private void onRoleUpdated() {
        toggleFab(showsFab());
        requireActivity().invalidateOptionsMenu();
        scrollManager.notifyDataSetChanged();
    }

    private boolean isRequestOwner() {
        return userViewModel.getCurrentUser().equals(joinRequest.getUser());
    }
}
