package com.mainstreetcode.teammates.fragments.headless;


import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.fragments.main.EventsFragment;
import com.mainstreetcode.teammates.fragments.main.ChatFragment;
import com.mainstreetcode.teammates.fragments.main.MediaFragment;
import com.mainstreetcode.teammates.fragments.main.TeamsFragment;
import com.mainstreetcode.teammates.model.Team;

public class TeamPickerFragment extends MainActivityFragment implements TeamAdapter.TeamAdapterListener {

    private static final String TAG = "TeamMediaPickerFragment";
    private static final String ARGS_REQUEST_CODE = "ARGS_REQUEST_CODE";

    @IdRes
    private int requestCode;

    private static TeamPickerFragment newInstance(@IdRes int requestCode) {
        TeamPickerFragment fragment = new TeamPickerFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_REQUEST_CODE, requestCode);
        fragment.setArguments(args);
        return fragment;
    }

    public static void pick(FragmentActivity host, @IdRes int requestCode) {
        assureInstance(host, requestCode);

        TeamPickerFragment instance = getInstance(host, requestCode);
        if (instance == null) return;

        instance.pick();
    }

    private static void assureInstance(FragmentActivity host, @IdRes int requestCode) {
        FragmentManager fragmentManager = host.getSupportFragmentManager();
        Fragment instance = getInstance(host, requestCode);

        if (instance == null) fragmentManager.beginTransaction()
                .add(newInstance(requestCode), makeTag(requestCode))
                .commitNow();
    }

    private static TeamPickerFragment getInstance(FragmentActivity host, @IdRes int requestCode) {
        return (TeamPickerFragment) host.getSupportFragmentManager().findFragmentByTag(makeTag(requestCode));
    }

    private static String makeTag(@IdRes int requestCode) {
        return TAG + "-" + requestCode;
    }

    @Override
    public String getStableTag() {
        return TAG;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCode = getArguments().getInt(ARGS_REQUEST_CODE);
    }

    @Override
    public void onTeamClicked(Team item) {
        teamViewModel.updateDefaultTeam(item);
        switch (requestCode) {
            case R.id.request_event_team_pick:
                showFragment(EventsFragment.newInstance(item));
                break;
            case R.id.request_chat_team_pick:
                showFragment(ChatFragment.newInstance(item));
                break;
            case R.id.request_media_team_pick:
                showFragment(MediaFragment.newInstance(item));
                break;
        }
    }

    private void pick() {
        Team team = teamViewModel.getDefaultTeam();
        if (!team.isEmpty()) onTeamClicked(team);
        else showPicker();
    }

    private void showPicker() {
        TeamsFragment teamsFragment = TeamsFragment.newInstance();
        teamsFragment.setTargetFragment(this, requestCode);

        showFragment(teamsFragment);
    }
}
