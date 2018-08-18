package com.mainstreetcode.teammate.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.fragments.main.GamesChildFragment;
import com.mainstreetcode.teammate.model.Tournament;

public class GameRoundAdapter extends FragmentStatePagerAdapter{
    private final Tournament tournament;

    public GameRoundAdapter(Tournament tournament, FragmentManager fm) {
        super(fm);
        this.tournament = tournament;
    }

    @Override
    public Fragment getItem(int round) {
        return GamesChildFragment.newInstance(tournament, round);
    }

    @Override
    public int getCount() {
        return tournament.getNumRounds();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return App.getInstance().getString(R.string.tournament_round_index, position);
    }
}
