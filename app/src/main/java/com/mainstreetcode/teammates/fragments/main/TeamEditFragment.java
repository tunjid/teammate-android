package com.mainstreetcode.teammates.fragments.main;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamEditAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.fragments.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Team;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Creates, edits or lets a {@link com.mainstreetcode.teammates.model.User} join a {@link Team}
 */

public class TeamEditFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        ImageWorkerFragment.CropListener,
        ImageWorkerFragment.ImagePickerListener {

    private static final String ARG_TEAM = "team";
    private static final String ARG_EDITABLE = "editable";

    private Team team;
    private List<String> roles = new ArrayList<>();

    private RecyclerView recyclerView;

    public static TeamEditFragment newInstance(Team team, boolean isEditable) {
        TeamEditFragment fragment = new TeamEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        args.putBoolean(ARG_EDITABLE, isEditable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        Team tempTeam = getArguments().getParcelable(ARG_TEAM);

        return tempTeam == null ? superResult : superResult + "-" + tempTeam.hashCode();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = getArguments().getParcelable(ARG_TEAM);
        getChildFragmentManager().beginTransaction()
                .add(ImageWorkerFragment.newInstance(), ImageWorkerFragment.TAG)
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_edit, container, false);
        recyclerView = rootView.findViewById(R.id.team_edit);

        boolean isEditable = getArguments().getBoolean(ARG_EDITABLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamEditAdapter(team, roles, isEditable, this));
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
        boolean isEditable = getArguments().getBoolean(ARG_EDITABLE, false);
        FloatingActionButton fab = getFab();
        fab.setOnClickListener(this);
        fab.setImageResource(isEditable ? R.drawable.ic_check_white_24dp : R.drawable.ic_group_add_white_24dp);
        toggleFab(true);
        setToolbarTitle(getString(!isEditable
                ? R.string.join_team
                : team.isNewTeam()
                ? R.string.create_team
                : R.string.edit_team));

        disposables.add(roleViewModel.getRoleValues().subscribe(currentRoles -> {
                    roles.clear();
                    roles.addAll(currentRoles);
                })
        );
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        ImageWorkerFragment fragment = (ImageWorkerFragment) childFragment;
        if (childFragment != null) fragment.setCropListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        toggleFab(false);
    }

    @Override
    public void onImageClick() {
        ImageWorkerFragment imageWorkerFragment = (ImageWorkerFragment) getChildFragmentManager()
                .findFragmentByTag(ImageWorkerFragment.TAG);

        if (imageWorkerFragment != null) imageWorkerFragment.requestCrop();
    }

    @Override
    public void onImageCropped(Uri uri) {
        team.get(Team.LOGO_POSITION).setValue(uri.getPath());
        recyclerView.getAdapter().notifyItemChanged(Team.LOGO_POSITION);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                String role = team.get(Team.ROLE_POSITION).getValue();

                if (TextUtils.isEmpty(role)) {
                    showSnackbar("Please select a role");
                    return;
                }

                boolean isEditable = getArguments().getBoolean(ARG_EDITABLE, false);
                Disposable disposable;

                // Join a team
                if (!isEditable) {
                    disposable = teamViewModel.joinTeam(team, role)
                            .subscribe(joinRequest -> showSnackbar(getString(R.string.team_submitted_join_request)), defaultErrorHandler);
                }
                // Create a team
                else if (team.isNewTeam()) {
                    disposable = teamViewModel.createTeam(team)
                            .subscribe(createdTeam -> showSnackbar(getString(R.string.created_team, createdTeam.getName())), defaultErrorHandler);
                }
                // Update a team
                else {
                    disposable = teamViewModel.updateTeam(team).subscribe(updatedTeam -> {
                        team.update(updatedTeam);
                        showSnackbar(getString(R.string.updated_team));
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }, defaultErrorHandler);
                }
                disposables.add(disposable);
                break;
        }
    }
}
