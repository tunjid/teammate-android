package com.mainstreetcode.teammate.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.fragments.main.StandingsFragment;
import com.mainstreetcode.teammate.fragments.main.StatRankFragment;
import com.mainstreetcode.teammate.model.Tournament;

public class TournamentStatAdapter extends FragmentStatePagerAdapter {
    private final Tournament tournament;

    public TournamentStatAdapter(Tournament tournament, FragmentManager fm) {
        super(fm);
        this.tournament = tournament;
    }

    @Override
    public Fragment getItem(int position) {
        return shouldDisplayStandings(position)
                ? StandingsFragment.newInstance(tournament)
                : StatRankFragment.newInstance(tournament);
    }

    @Override
    public int getCount() {
        return tournament.isKnockOut() ? 1 : 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return App.getInstance().getString(shouldDisplayStandings(position)
                ? R.string.tournament_standings : R.string.stat_ranks);
    }

    private boolean shouldDisplayStandings(int position) {
        return position == 0 && !tournament.isKnockOut();
    }
}
