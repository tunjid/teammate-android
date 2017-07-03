package com.mainstreetcode.teammates.fragments.main;

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

/**
 * Creates or lets a user join a team
 */

public class TeamDetailFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        TeamDetailAdapter.UserAdapterListener {

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
                defaultErrorHandler)
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

        if (user.isUserApproved() && !user.isTeamApproved() && userViewModel.isTeamAdmin(team)) {
            new AlertDialog.Builder(getContext()).setTitle(getString(R.string.add_user_to_team, user.getFirstName()))
                    .setPositiveButton(R.string.yes, (dialog, which) -> approveUser(user, true))
                    .setNegativeButton(R.string.no, (dialog, which) -> approveUser(user, false))
                    .show();
        }
        else if (team.getUsers().contains(user)) {
            showFragment(UserEditFragment.newInstance(team, user));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                String role = team.getRole();

                if (TextUtils.isEmpty(role)) {
                    showSnackbar(getString(R.string.select_role));
                    return;
                }

                if (team.getId() != null) {
                    disposables.add(teamViewModel.joinTeam(team, role)
                            .subscribe(joinRequest -> showSnackbar(getString(R.string.team_submitted_join_request)),
                                    defaultErrorHandler));
                }
                else {
                    disposables.add(teamViewModel.createTeam(team)
                            .subscribe(createdTeam -> showSnackbar(getString(R.string.created_team, createdTeam.getName())),
                                    defaultErrorHandler)
                    );
                }
                break;
        }
    }

    private void approveUser(final User user, final boolean approve) {
        disposables.add(
                teamViewModel.approveUser(team, user, approve).subscribe((joinRequest) -> {
                    if (approve) {
                        user.setTeamApproved(true);
                        team.getUsers().add(user);
                    }
                    team.getPendingUsers().remove(user);
                    recyclerView.getAdapter().notifyDataSetChanged();

                    String name = user.getFirstName();
                    showSnackbar(approve
                            ? getString(R.string.added_user, name)
                            : getString(R.string.removed_user, name));
                }, defaultErrorHandler)
        );
    }
}
