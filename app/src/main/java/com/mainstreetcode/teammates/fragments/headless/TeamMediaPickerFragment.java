package com.mainstreetcode.teammates.fragments.headless;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.fragments.main.TeamMediaFragment;
import com.mainstreetcode.teammates.fragments.main.TeamsFragment;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

public class TeamMediaPickerFragment extends BaseFragment implements TeamAdapter.TeamAdapterListener {

    private static final String TAG = "TeamMediaPickerFragment";

    private Team selectedTeam = Team.empty();

    public static void request(FragmentActivity host) {
        assureInstance(host);

        TeamMediaPickerFragment instance = getInstance(host);
        if (instance == null) return;

        instance.pick();
    }

    public static void pick(FragmentActivity host) {
        assureInstance(host);

        TeamMediaPickerFragment instance = getInstance(host);
        if (instance == null) return;

        instance.selectedTeam.update(Team.empty());
        instance.pick();
    }

    private static void assureInstance(FragmentActivity host) {
        FragmentManager fragmentManager = host.getSupportFragmentManager();
        Fragment instance = getInstance(host);

        if (instance == null) fragmentManager.beginTransaction()
                .add(new TeamMediaPickerFragment(), TAG)
                .commitNow();
    }

    private static TeamMediaPickerFragment getInstance(FragmentActivity host) {
        return (TeamMediaPickerFragment) host.getSupportFragmentManager().findFragmentByTag(TAG);
    }

    @Override
    public String getStableTag() {
        return TAG;
    }

    @Override
    public void onTeamClicked(Team item) {
        selectedTeam.update(item);
        showFragment(TeamMediaFragment.newInstance(item));
    }

    private void pick() {
        if (!selectedTeam.isEmpty()) onTeamClicked(selectedTeam);
        else showPicker();
    }

    private void showPicker() {
        TeamsFragment teamsFragment = TeamsFragment.newInstance();
        teamsFragment.setTargetFragment(this, R.id.request_media_team_pick);

        showFragment(teamsFragment);
    }
}
