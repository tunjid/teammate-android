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
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.viewmodel.TeamViewModel;

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

    private Team team;

    private RecyclerView recyclerView;

    public static TeamDetailFragment newInstance(Team team) {
        TeamDetailFragment fragment = new TeamDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
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


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamDetailAdapter(team));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (Math.abs(dy) < 3) return;
                toggleFab(dy < 0);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getFab();
        fab.setOnClickListener(this);
        setToolbarTitle(team.getName());
        toggleFab(false);

        TeamViewModel viewModel = ViewModelProviders.of(getActivity()).get(TeamViewModel.class);
        disposables.add(viewModel.getTeam(team).subscribe(
                updatedTeam -> {
                    List<User> users = team.getUsers();
                    List<User> pendingUsers = team.getPendingUsers();

                    users.clear();
                    pendingUsers.clear();

                    users.addAll(updatedTeam.getUsers());
                    pendingUsers.addAll(updatedTeam.getPendingUsers());

                    recyclerView.getAdapter().notifyDataSetChanged();
                },
                ErrorHandler.builder()
                        .defaultMessage(getString(R.string.default_error))
                        .add(this::showSnackbar)
                        .build())
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
