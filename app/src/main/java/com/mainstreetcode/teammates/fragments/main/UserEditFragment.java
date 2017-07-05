package com.mainstreetcode.teammates.fragments.main;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.UserEditAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.fragments.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Edits a Team member
 */

public class UserEditFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        ImageWorkerFragment.CropListener,
        ImageWorkerFragment.ImagePickerListener {

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
                : superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        team = getArguments().getParcelable(ARG_TEAM);
        user = getArguments().getParcelable(ARG_USER);

        getChildFragmentManager().beginTransaction()
                .add(ImageWorkerFragment.newInstance(), ImageWorkerFragment.TAG)
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_edit, container, false);
        recyclerView = rootView.findViewById(R.id.user_edit);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new UserEditAdapter(user, roles, userViewModel.getCurrentUser().equals(user), this));
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (userViewModel.isTeamAdmin(team) && !userViewModel.getCurrentUser().equals(user)) {
            inflater.inflate(R.menu.fragment_user_edit, menu);
        }
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
        }));
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                String role = user.getRoleName();

                if (TextUtils.isEmpty(role)) {
                    showSnackbar("Please select a role");
                    return;
                }

                disposables.add(
                        teamViewModel.updateTeamUser(team, user).subscribe(updatedUser -> {
                            user.update(updatedUser);
                            showSnackbar(getString(R.string.updated_user, user.getFirstName()));
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }, defaultErrorHandler)
                );
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_kick:
                final String firstName = user.getFirstName();
                final String prompt = getString(R.string.confirm_user_drop, firstName);

                Snackbar.make(recyclerView, prompt, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.yes, view ->
                                disposables.add(teamViewModel.dropUser(team, user).subscribe(dropped -> {
                                    showSnackbar(getString(R.string.dropped_user, firstName));
                                    getActivity().onBackPressed();
                                }, defaultErrorHandler)))
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageCropped(Uri uri) {
        user.get(User.IMAGE_POSITION).setValue(uri.getPath());
        recyclerView.getAdapter().notifyItemChanged(User.IMAGE_POSITION);
    }

    @Override
    public void onImageClick() {
        ImageWorkerFragment imageWorkerFragment = (ImageWorkerFragment) getChildFragmentManager()
                .findFragmentByTag(ImageWorkerFragment.TAG);

        if (imageWorkerFragment != null) imageWorkerFragment.requestCrop();
    }
}
