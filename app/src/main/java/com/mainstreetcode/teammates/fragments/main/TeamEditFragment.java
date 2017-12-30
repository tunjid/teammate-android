package com.mainstreetcode.teammates.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamEditAdapter;
import com.mainstreetcode.teammates.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.HeaderedModel;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

import static android.app.Activity.RESULT_OK;

/**
 * Creates, edits or lets a {@link com.mainstreetcode.teammates.model.User} join a {@link Team}
 */

public class TeamEditFragment extends HeaderedFragment
        implements
        View.OnClickListener,
        TeamEditAdapter.TeamEditAdapterListener {

    private static final int CREATING = 0;
    private static final int EDITING = 1;
    private static final int JOINING = 2;

    private static final String ARG_TEAM = "team";
    private static final String ARG_STATE = "editable";
    public static final int PLACE_PICKER_REQUEST = 2;

    private int state;
    private Team team;
    private List<String> roles = new ArrayList<>();

    private RecyclerView recyclerView;

    private static TeamEditFragment newInstance(Team team, int state) {
        TeamEditFragment fragment = new TeamEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        args.putInt(ARG_STATE, state);
        fragment.setArguments(args);
        return fragment;
    }

    public static TeamEditFragment newCreateInstance() {
        return newInstance(Team.empty(), CREATING);
    }

    public static TeamEditFragment newEditInstance(Team team) {
        return newInstance(team, EDITING);
    }

    public static TeamEditFragment newJoinInstance(Team team) {
        return newInstance(team, JOINING);
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

        state = getArguments().getInt(ARG_STATE);
        team = getArguments().getParcelable(ARG_TEAM);

        ImageWorkerFragment fragment = ImageWorkerFragment.newInstance();
        fragment.setTargetFragment(this, ImageWorkerFragment.CROP_CHOOSER);

        ImageWorkerFragment.attach(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);
        recyclerView = rootView.findViewById(R.id.model_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamEditAdapter(team, roles, this));
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setOnClickListener(this);
        setFabIcon(state == JOINING ? R.drawable.ic_check_white_24dp : R.drawable.ic_group_add_white_24dp);

        User user = userViewModel.getCurrentUser();

        onRoleUpdated();

        disposables.add(localRoleViewModel.getRoleInTeam(user, team)
                .subscribe(this::onRoleUpdated, defaultErrorHandler));

        disposables.add(roleViewModel.getRoleValues()
                .subscribe(this::onRolesFetched, emptyErrorHandler));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    @Override
    public boolean drawsBehindStatusBar() {
        return true;
    }

    @Override
    protected boolean showsFab() {
        return state == CREATING || state == JOINING || localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onImageClick() {
        if (!localRoleViewModel.hasPrivilegedRole()) return;
        super.onImageClick();
    }

    @Override
    protected HeaderedModel getHeaderedModel() {
        return team;
    }

    @Override
    public void onAddressClicked() {
        if (getActivity() == null) return;

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);}
        catch (Exception e) {e.printStackTrace();}
    }

    @Override
    public boolean isJoiningTeam() {
        return state == JOINING;
    }

    @Override
    public boolean isPrivileged() {
        return state == CREATING || localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PLACE_PICKER_REQUEST) return;
        if (resultCode == RESULT_OK) {
            toggleProgress(true);
            Context context = recyclerView.getContext();
            Place place = PlacePicker.getPlace(context, data);
            locationViewModel.fromPlace(place).subscribe(address -> {
                team.setAddress(address);
                recyclerView.getAdapter().notifyDataSetChanged();
                toggleProgress(false);
            }, defaultErrorHandler);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                String role = team.get(Team.ROLE_POSITION).getValue();

                if (isJoiningTeam() && TextUtils.isEmpty(role)) {
                    showSnackbar(getString(R.string.choose_role_error));
                    return;
                }

                Disposable disposable = null;

                switch (state) {
                    case CREATING:
                        disposable = teamViewModel.createOrUpdate(team)
                                .subscribe(createdTeam -> showSnackbar(getString(R.string.created_team, createdTeam.getName())), defaultErrorHandler);
                        break;
                    case JOINING:
                        JoinRequest joinRequest = JoinRequest.join(role, team, userViewModel.getCurrentUser());
                        disposable = roleViewModel.joinTeam(joinRequest)
                                .subscribe(request -> showSnackbar(getString(R.string.team_submitted_join_request)), defaultErrorHandler);
                        break;
                    case EDITING:
                        disposable = teamViewModel.createOrUpdate(team).subscribe(updatedTeam -> {
                            showSnackbar(getString(R.string.updated_team));
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }, defaultErrorHandler);
                        break;
                }

                if (disposable != null) disposables.add(disposable);
        }
    }

    private void onRoleUpdated() {
        state = team.isEmpty() ? CREATING : localRoleViewModel.getCurrentRole().isEmpty() ? JOINING : EDITING;

        switch (state) {
            case CREATING:
                setToolbarTitle(getString(R.string.create_team));
                break;
            case JOINING:
                setToolbarTitle(getString(R.string.join_team));
                break;
            case EDITING:
                setToolbarTitle(getString(R.string.edit_team));
                break;
        }
        toggleFab(showsFab());
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void onRolesFetched(List<String> fetchedRoles) {
        roles.clear();
        roles.addAll(fetchedRoles);
    }
}
