/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
