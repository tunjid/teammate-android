package com.mainstreetcode.teammates.fragments.headless;


import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.fragments.main.TeamChatFragment;
import com.mainstreetcode.teammates.fragments.main.TeamMediaFragment;
import com.mainstreetcode.teammates.fragments.main.TeamsFragment;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

public class TeamPickerFragment extends BaseFragment implements TeamAdapter.TeamAdapterListener {

    private static final String TAG = "TeamMediaPickerFragment";
    private static final String ARGS_REQUEST_CODE = "ARGS_REQUEST_CODE";

    @IdRes
    private int requestCode;
    private Team selectedTeam = Team.empty();

    private static TeamPickerFragment newInstance(@IdRes int requestCode) {
        TeamPickerFragment fragment = new TeamPickerFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_REQUEST_CODE, requestCode);
        fragment.setArguments(args);
        return fragment;
    }

    public static void request(FragmentActivity host, @IdRes int requestCode) {
        assureInstance(host, requestCode);

        TeamPickerFragment instance = getInstance(host, requestCode);
        if (instance == null) return;

        instance.pick();
    }

    public static void pick(FragmentActivity host, @IdRes int requestCode) {
        assureInstance(host, requestCode);

        TeamPickerFragment instance = getInstance(host, requestCode);
        if (instance == null) return;

        instance.selectedTeam.update(Team.empty());
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestCode = getArguments().getInt(ARGS_REQUEST_CODE);
    }

    @Override
    public void onTeamClicked(Team item) {
        selectedTeam.update(item);
        switch (requestCode) {
            case R.id.request_chat_team_pick:
                showFragment(TeamChatFragment.newInstance(item));
                break;
            case R.id.request_media_team_pick:
                showFragment(TeamMediaFragment.newInstance(item));
                break;
        }
    }

    private void pick() {
        if (!selectedTeam.isEmpty()) onTeamClicked(selectedTeam);
        else showPicker();
    }

    private void showPicker() {
        TeamsFragment teamsFragment = TeamsFragment.newInstance();
        teamsFragment.setTargetFragment(this, requestCode);

        showFragment(teamsFragment);
    }
}
