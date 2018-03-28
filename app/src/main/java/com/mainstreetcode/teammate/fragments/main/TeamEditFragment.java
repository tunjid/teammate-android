package com.mainstreetcode.teammate.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamEditAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.HeaderedModel;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ScrollManager;

import io.reactivex.disposables.Disposable;

import static android.app.Activity.RESULT_OK;

/**
 * Creates, edits or lets a {@link com.mainstreetcode.teammate.model.User} join a {@link Team}
 */

public class TeamEditFragment extends HeaderedFragment
        implements
        TeamEditAdapter.TeamEditAdapterListener {

    private static final int CREATING = 0;
    private static final int EDITING = 1;
    private static final int JOINING = 2;

    private static final String ARG_TEAM = "team";
    private static final String ARG_STATE = "state";
    public static final int PLACE_PICKER_REQUEST = 2;

    private int state;
    private Team team;

    public static TeamEditFragment newCreateInstance() {return newInstance(Team.empty(), CREATING);}

    public static TeamEditFragment newEditInstance(Team team) {return newInstance(team, EDITING);}

    public static TeamEditFragment newJoinInstance(Team team) {return newInstance(team, JOINING);}

    private static TeamEditFragment newInstance(Team team, int state) {
        TeamEditFragment fragment = new TeamEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        args.putInt(ARG_STATE, state);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        int state = getArguments().getInt(ARG_STATE);
        Team tempTeam = getArguments().getParcelable(ARG_TEAM);

        return tempTeam == null ? superResult : superResult + "-" + tempTeam.hashCode() + "-" + state;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        state = getArguments().getInt(ARG_STATE);
        team = getArguments().getParcelable(ARG_TEAM);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withAdapter(new TeamEditAdapter(team, roleViewModel.getRoleNames(), this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .addScrollListener(this::updateFabOnScroll)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        User user = userViewModel.getCurrentUser();

        roleViewModel.fetchRoleValues();
        disposables.add(localRoleViewModel.getRoleInTeam(user, team).subscribe(this::onRoleUpdated, defaultErrorHandler));

        if (!team.isEmpty() && state != JOINING) {
            disposables.add(teamViewModel.getTeam(team).subscribe(this::onTeamChanged, defaultErrorHandler));
        }
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        onRoleUpdated();
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {
        return state == CREATING || state == JOINING || localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onImageClick() {
        if (!localRoleViewModel.hasPrivilegedRole()) return;
        super.onImageClick();
    }

    @Override
    protected HeaderedModel getHeaderedModel() {return team;}

    @Override
    public void onAddressClicked() {
        if (getActivity() == null) return;

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);}
        catch (Exception e) {Logger.log(getStableTag(), "Unable to start places api", e);}
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
            Context context = getContext();
            if (context == null) return;

            toggleProgress(true);
            Place place = PlacePicker.getPlace(context, data);
            locationViewModel.fromPlace(place).subscribe(address -> {
                team.setAddress(address);
                scrollManager.notifyDataSetChanged();
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

                toggleProgress(true);
                Disposable disposable = null;
                switch (state) {
                    case CREATING:
                        disposable = teamViewModel.createOrUpdate(team).subscribe(result -> {
                            scrollManager.onDiff(result);
                            viewHolder.bind(getHeaderedModel());
                            toggleProgress(false);
                            showSnackbar(getString(R.string.created_team, team.getName()));
                        }, defaultErrorHandler);
                        break;
                    case JOINING:
                        JoinRequest joinRequest = JoinRequest.join(role, team, userViewModel.getCurrentUser());
                        disposable = roleViewModel.joinTeam(joinRequest).subscribe(request -> {
                            showSnackbar(getString(R.string.team_submitted_join_request));
                            toggleProgress(false);
                        }, defaultErrorHandler);
                        break;
                    case EDITING:
                        disposable = teamViewModel.createOrUpdate(team).subscribe(result -> {
                            viewHolder.bind(getHeaderedModel());
                            scrollManager.onDiff(result);
                            toggleProgress(false);
                            showSnackbar(getString(R.string.updated_team));
                        }, defaultErrorHandler);
                        break;
                }

                if (disposable != null) disposables.add(disposable);
        }
    }

    private void onTeamChanged(DiffUtil.DiffResult result) {
        toggleProgress(false);
        scrollManager.onDiff(result);
        viewHolder.bind(getHeaderedModel());
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
        scrollManager.notifyDataSetChanged();
    }
}
