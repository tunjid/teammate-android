package com.mainstreetcode.teammate.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.fragments.main.GamesChildFragment;
import com.mainstreetcode.teammate.model.Tournament;

public class TournamentRoundAdapter extends FragmentStatePagerAdapter {

    private int count; // num rounds in tournament is concurrent
    private final Tournament tournament;

    public TournamentRoundAdapter(Tournament tournament, FragmentManager fm) {
        super(fm);
        this.count = tournament.getNumRounds();
        this.tournament = tournament;
    }

    @Override
    public Fragment getItem(int round) {
        return GamesChildFragment.newInstance(tournament, round);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void notifyDataSetChanged() {
        count = tournament.getNumRounds();
        super.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return App.getInstance().getString(R.string.tournament_round_index, position);
    }
}
