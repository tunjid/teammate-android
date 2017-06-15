package com.mainstreetcode.teammates.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.viewmodel.TeamViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Splash screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class TeamDetailFragment extends MainActivityFragment
        implements
        View.OnClickListener {

    private static final String ARG_TEAM = "team";
    private static final String ARG_EDITABLE = "editable";

    private Team team;
    private List<Role> roles = new ArrayList<>();

    private RecyclerView recyclerView;

    public static TeamDetailFragment newInstance(Team team, boolean isEditable) {
        TeamDetailFragment fragment = new TeamDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        args.putBoolean(ARG_EDITABLE, isEditable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = getArguments().getParcelable(ARG_TEAM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_detail, container, false);
        recyclerView = rootView.findViewById(R.id.team_detail);

        boolean isEditable = getArguments().getBoolean(ARG_EDITABLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamDetailAdapter(team, roles, isEditable));
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
        setToolbarTitle(getString(isEditable ? R.string.create_team : R.string.join_team));
        toggleFab(true);

        disposables.add(roleViewModel.getRoles().subscribe(currentRoles -> {
                    roles.clear();
                    roles.addAll(currentRoles);
                })
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        toggleFab(false);
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

                TeamViewModel teamViewModel = ViewModelProviders.of(this).get(TeamViewModel.class);

                // Don't need all cached user emmisions
                Observable<User> userObservable = userViewModel.getMe().take(1);
                ErrorHandler errorHandler = ErrorHandler.builder()
                        .defaultMessage(getString(R.string.default_error))
                        .add(this::showSnackbar)
                        .build();

                if (team.getId() != null) {
                    disposables.add(userObservable
                            .flatMap(user -> teamViewModel.joinTeam(team, role))
                            .subscribe(joinRequest -> showSnackbar(getString(R.string.team_submitted_join_request)),
                                    errorHandler));
                }
                else {
                    disposables.add(userObservable.flatMap(user -> teamViewModel.createTeam(team))
                            .subscribe(createdTeam -> showSnackbar(getString(R.string.created_team, createdTeam.getName())),
                                    errorHandler)
                    );
                }
                break;
        }
    }
}
