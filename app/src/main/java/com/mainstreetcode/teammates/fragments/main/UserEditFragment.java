package com.mainstreetcode.teammates.fragments.main;

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
import com.mainstreetcode.teammates.adapters.UserEditAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Splash screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class UserEditFragment extends MainActivityFragment
        implements
        View.OnClickListener {

    private static final String ARG_TEAM = "team";
    private static final String ARG_USER = "user";

    private Team team;
    private User user;
    private List<String> roles = new ArrayList<>();

    private RecyclerView recyclerView;

    public static UserEditFragment newInstance(Team team, User user) {
        UserEditFragment fragment = new UserEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        args.putParcelable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        Team tempTeam = getArguments().getParcelable(ARG_TEAM);
        User tempUser = getArguments().getParcelable(ARG_USER);

        return (tempTeam != null && tempUser != null)
                ? superResult + "-" + tempTeam.hashCode() + "-" + tempUser.hashCode()
                :superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team = getArguments().getParcelable(ARG_TEAM);
        user = getArguments().getParcelable(ARG_USER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_edit, container, false);
        recyclerView = rootView.findViewById(R.id.user_edit);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new UserEditAdapter(user, roles, userViewModel.getCurrentUser().equals(user)));
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
        fab.setImageResource(R.drawable.ic_check_white_24dp);
        toggleFab(true);
        setToolbarTitle(getString(R.string.edit_user));

        disposables.add(roleViewModel.getRoleValues().subscribe(currentRoles -> {
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
                String role = user.getRole();

                if (TextUtils.isEmpty(role)) {
                    showSnackbar("Please select a role");
                    return;
                }

                // Don't need all cached user emmisions
                ErrorHandler errorHandler = ErrorHandler.builder()
                        .defaultMessage(getString(R.string.default_error))
                        .add(this::showSnackbar)
                        .build();

                disposables.add(
                        teamViewModel.updateTeamUser(team, user).subscribe(updatedUser -> {
                            user.update(updatedUser);
                            showSnackbar(getString(R.string.updated_user, user.getFirstName()));
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }, errorHandler)
                );
                break;
        }
    }
}
