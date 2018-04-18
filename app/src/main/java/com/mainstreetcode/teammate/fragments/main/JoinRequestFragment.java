package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.JoinRequestAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.HeaderedModel;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;

/**
 * Invites a Team member
 */

public class JoinRequestFragment extends HeaderedFragment
        implements
        JoinRequestAdapter.AdapterListener {

    public static final String ARG_JOIN_REQUEST = "join-request";

    private JoinRequest joinRequest;

    public static JoinRequestFragment newInstance(Team team) {
        JoinRequestFragment fragment = new JoinRequestFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_JOIN_REQUEST, JoinRequest.invite(team));
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
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        User user = userViewModel.getCurrentUser();
        if (localRoleViewModel.hasPrivilegedRole() && !joinRequest.getUser().equals(user)) {
            inflater.inflate(R.menu.fragment_user_edit, menu);
        }
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(R.string.invite_user));
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return true;}

    @Override
    public void onImageClick() {}

    @Override
    protected HeaderedModel getHeaderedModel() {return joinRequest;}

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
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
}
