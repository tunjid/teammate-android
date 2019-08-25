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

package com.mainstreetcode.teammate.fragments.main

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TournamentStatAdapter
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer

import com.google.android.material.tabs.TabLayout.MODE_FIXED

class StatDetailFragment : MainActivityFragment() {

    private lateinit var tournament: Tournament

    override val toolbarTitle: CharSequence get() = getString(R.string.tournament_stats)

    override val showsFab: Boolean get() = false

    override fun getStableTag(): String =
            Gofer.tag(super.getStableTag(), arguments!!.getParcelable(ARG_TOURNAMENT)!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tournament = arguments!!.getParcelable(ARG_TOURNAMENT)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_games_parent, container, false)
        val viewPager = root.findViewById<ViewPager>(R.id.view_pager)
        val tabLayout = root.findViewById<TabLayout>(R.id.tab_layout)

        viewPager.adapter = TournamentStatAdapter(tournament, childFragmentManager)
        tabLayout.tabMode = MODE_FIXED
        tabLayout.setupWithViewPager(viewPager)

        return root
    }

    companion object {

        private const val ARG_TOURNAMENT = "tournament"

        fun newInstance(tournament: Tournament): StatDetailFragment = StatDetailFragment().apply {
            arguments = bundleOf(ARG_TOURNAMENT to tournament)
            setEnterExitTransitions()
        }
    }
}
