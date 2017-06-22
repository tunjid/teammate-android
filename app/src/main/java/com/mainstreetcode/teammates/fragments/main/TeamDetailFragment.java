package com.mainstreetcode.teammates.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.viewmodel.TeamViewModel;

import io.reactivex.Observable;

/**
 * Splash screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class TeamDetailFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        TeamDetailAdapter.UserAdapterListener {

    private static final String ARG_TEAM = "team";

    private Team team;

    private RecyclerView recyclerView;

    ErrorHandler errorHandler = ErrorHandler.builder()
            .defaultMessage(getString(R.string.default_error))
            .add(this::showSnackbar)
            .build();

    public static TeamDetailFragment newInstance(Team team) {
        TeamDetailFragment fragment = new TeamDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
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
        setHasOptionsMenu(true);

        team = getArguments().getParcelable(ARG_TEAM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_detail, container, false);
        EditText editText = rootView.findViewById(R.id.team_name);
        recyclerView = rootView.findViewById(R.id.team_detail);

        editText.setText(team.getName());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamDetailAdapter(team, this));
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
        setToolbarTitle(getString(R.string.team_name_prefix, team.getName()));
        toggleFab(false);

        disposables.add(teamViewModel.getTeam(team).subscribe(
                updatedTeam -> {
                    team.update(updatedTeam);
                    recyclerView.getAdapter().notifyDataSetChanged();
                },
                errorHandler)
        );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_team_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                showFragment(TeamEditFragment.newInstance(team, true));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        toggleFab(false);
    }

    @Override
    public void onUserClicked(User user) {
        View rootView = getView();
        if (rootView == null) return;

        User currentUser = userViewModel.getCurrentUser();
        int i = team.getUsers().indexOf(currentUser);

        if (i != -1) currentUser = team.getUsers().get(i);

        if (user.isUserApproved() && !user.isTeamApproved() && "Admin".equals(currentUser.getRole())) {

            new AlertDialog.Builder(getContext()).setTitle(getString(R.string.add_user_to_team, user.getFirstName()))
                    .setPositiveButton(R.string.yes, (dialog, which) -> approveUser(user, true))
                    .setNegativeButton(R.string.no, (dialog, which) -> approveUser(user, false))
                    .show();
        }
//        else if (user.isUserApproved() && !user.isTeamApproved()) {
//
//        }
        else if (team.getUsers().contains(user)) {
            showFragment(UserEditFragment.newInstance(user));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                String role = team.getRole();

                if (TextUtils.isEmpty(role)) {
                    showSnackbar("Please select a role");
                    return;
                }

                TeamViewModel teamViewModel = ViewModelProviders.of(this).get(TeamViewModel.class);

                // Don't need all cached user emmisions
                Observable<User> userObservable = userViewModel.getMe().take(1);

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

    private void approveUser(User user, boolean approve) {
        disposables.add(
                teamViewModel.approveUser(team, user, approve).subscribe((joinRequest) -> {}, errorHandler)
        );
    }
}
