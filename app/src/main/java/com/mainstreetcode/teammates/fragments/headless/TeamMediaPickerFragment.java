package com.mainstreetcode.teammates.fragments.headless;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.fragments.main.TeamMediaFragment;
import com.mainstreetcode.teammates.fragments.main.TeamsFragment;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

public class TeamMediaPickerFragment extends BaseFragment implements TeamAdapter.TeamAdapterListener {

    private static final String TAG = "TeamMediaPickerFragment";

    public static void request(AppCompatActivity host) {
        assureInstance(host);

        TeamMediaPickerFragment instance = getInstance(host);
        if (instance == null) return;

        TeamsFragment teamsFragment = TeamsFragment.newInstance();
        teamsFragment.setTargetFragment(instance, R.id.request_media_team_pick);

        instance.showFragment(teamsFragment);
    }

    private static void assureInstance(AppCompatActivity host) {
        FragmentManager fragmentManager = host.getSupportFragmentManager();
        Fragment instance = getInstance(host);

        if (instance == null) fragmentManager.beginTransaction()
                .add(new TeamMediaPickerFragment(), TAG)
                .commitNow();
    }

    private static TeamMediaPickerFragment getInstance(AppCompatActivity host) {
        return (TeamMediaPickerFragment) host.getSupportFragmentManager().findFragmentByTag(TAG);
    }

    @Override
    public String getStableTag() {
        return TAG;
    }

    @Override
    public void onTeamClicked(Team item) {
        showFragment(TeamMediaFragment.newInstance(item));
    }
}
