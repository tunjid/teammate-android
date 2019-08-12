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

package com.mainstreetcode.teammate.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.fragments.main.GamesChildFragment
import com.mainstreetcode.teammate.model.Tournament

class TournamentRoundAdapter(private val tournament: Tournament, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private var count: Int = 0 // num rounds in tournament is concurrent

    init {
        this.count = tournament.numRounds
    }

    override fun getItem(round: Int): Fragment = GamesChildFragment.newInstance(tournament, round)

    override fun getCount(): Int = count

    override fun notifyDataSetChanged() {
        count = tournament.numRounds
        super.notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence? =
            App.instance.getString(R.string.tournament_round_index, position)
}
