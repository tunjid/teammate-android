package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.JoinRequestAdapter;
import com.mainstreetcode.teammates.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammates.model.HeaderedModel;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.util.ScrollManager;

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
                ? superResult + "-" + temp.hashCode()
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
                .withAdapter(new JoinRequestAdapter(joinRequest, roleViewModel.getRoleNames(), this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .addScrollListener(this::updateFabOnScroll)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(R.string.invite_user));

        roleViewModel.fetchRoleValues();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        User user = userViewModel.getCurrentUser();
        if (localRoleViewModel.hasPrivilegedRole() && !joinRequest.getUser().equals(user)) {
            inflater.inflate(R.menu.fragment_user_edit, menu);
        }
    }

    @Override
    public void onImageClick() {}

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return true;}

    @Override
    protected HeaderedModel getHeaderedModel() {return joinRequest;}

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                String roleName = joinRequest.getRoleName();

                if (TextUtils.isEmpty(roleName)) {
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
