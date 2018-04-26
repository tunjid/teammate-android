package com.mainstreetcode.teammate.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamEditAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ScrollManager;

import io.reactivex.Flowable;

import static android.app.Activity.RESULT_OK;

/**
 * Creates, edits or lets a {@link com.mainstreetcode.teammate.model.User} join a {@link Team}
 */

public class TeamEditFragment extends HeaderedFragment<Team>
        implements
        TeamEditAdapter.TeamEditAdapterListener {

    private static final int CREATING = 0;
    private static final int EDITING = 1;

    private static final String ARG_TEAM = "team";
    private static final String ARG_STATE = "state";

    private int state;
    private Team team;

    public static TeamEditFragment newCreateInstance() {return newInstance(Team.empty(), CREATING);}

    public static TeamEditFragment newEditInstance(Team team) {return newInstance(team, EDITING);}

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
                .withAdapter(new TeamEditAdapter(team, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        User user = userViewModel.getCurrentUser();
        disposables.add(localRoleViewModel.getRoleInTeam(user, team).subscribe(this::onRoleUpdated, defaultErrorHandler));
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
        return state == CREATING || localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.fab) return;

        toggleProgress(true);
        disposables.add(teamViewModel.createOrUpdate(team)
                .subscribe(result -> {
                    showSnackbar(getModelUpdateMessage());
                    onModelUpdated(result);
                }, defaultErrorHandler));
    }

    @Override
    public void onImageClick() {
        if (state == CREATING)
            showSnackbar(getString(R.string.create_team_first));
        else if (!localRoleViewModel.hasPrivilegedRole())
            showSnackbar(getString(R.string.no_permission));

        else super.onImageClick();
    }

    @Override
    protected Team getHeaderedModel() {return team;}

    @Override
    protected Flowable<DiffUtil.DiffResult> fetch(Team model) {
        return teamViewModel.getTeam(model);
    }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        viewHolder.bind(getHeaderedModel());
        scrollManager.onDiff(result);
        toggleProgress(false);
        if (!team.isEmpty()) state = EDITING;
    }

    @Override
    public void onAddressClicked() {
        if (getActivity() == null) return;

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);}
        catch (Exception e) {Logger.log(getStableTag(), "Unable to start places api", e);}
    }

    @Override
    public boolean isPrivileged() {
        return state == CREATING || localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PLACE_PICKER_REQUEST) return;
        if (resultCode != RESULT_OK) return;
        Context context = getContext();
        if (context == null) return;

        toggleProgress(true);
        Place place = PlacePicker.getPlace(context, data);
        disposables.add(locationViewModel.fromPlace(place)
                .subscribe(this::onAddressFound, defaultErrorHandler));
    }

    private void onRoleUpdated() {
        state = team.isEmpty() ? CREATING : EDITING;
        scrollManager.notifyDataSetChanged();

        toggleFab(showsFab());
        setToolbarTitle(getString(state == CREATING ? R.string.create_team : R.string.edit_team));
    }

    private void onAddressFound(Address address) {
        team.setAddress(address);
        scrollManager.notifyDataSetChanged();
        toggleProgress(false);
    }

    @NonNull
    private String getModelUpdateMessage() {
        return state == CREATING ? getString(R.string.created_team, team.getName()) : getString(R.string.updated_team);
    }
}
