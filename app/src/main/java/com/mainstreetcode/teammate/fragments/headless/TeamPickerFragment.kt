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

package com.mainstreetcode.teammate.fragments.headless


import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.TeamAdapter
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.fragments.main.ChatFragment
import com.mainstreetcode.teammate.fragments.main.EventsFragment
import com.mainstreetcode.teammate.fragments.main.GamesFragment
import com.mainstreetcode.teammate.fragments.main.MediaFragment
import com.mainstreetcode.teammate.fragments.main.TeamMembersFragment
import com.mainstreetcode.teammate.fragments.main.TeamsFragment
import com.mainstreetcode.teammate.fragments.main.TournamentsFragment
import com.mainstreetcode.teammate.model.Team

class TeamPickerFragment : MainActivityFragment(), TeamAdapter.AdapterListener {

    @IdRes
    private var requestCode: Int = 0
    private var isChanging: Boolean = false

    override fun getStableTag(): String {
        return TAG
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isChanging = arguments!!.getBoolean(ARGS_CHANGING)
        requestCode = arguments!!.getInt(ARGS_REQUEST_CODE)
    }

    override fun onTeamClicked(item: Team) {
        teamViewModel.updateDefaultTeam(item)
        when (requestCode) {
            R.id.request_game_team_pick -> showFragment(GamesFragment.newInstance(item))
            R.id.request_chat_team_pick -> showFragment(ChatFragment.newInstance(item))
            R.id.request_event_team_pick -> showFragment(EventsFragment.newInstance(item))
            R.id.request_media_team_pick -> showFragment(MediaFragment.newInstance(item))
            R.id.request_tournament_team_pick -> showFragment(TournamentsFragment.newInstance(item))
            R.id.request_default_team_pick -> showFragment(TeamMembersFragment.newInstance(item))
        }
        hideBottomSheet()
    }

    private fun pick() {
        val team = teamViewModel.defaultTeam
        if (!isChanging && !team.isEmpty)
            onTeamClicked(team)
        else
            showPicker()
    }

    private fun showPicker() {
        val teamsFragment = TeamsFragment.newInstance()
        teamsFragment.setTargetFragment(this, requestCode)

        val menuRes = if (requestCode != R.id.request_event_team_pick || teamViewModel.isOnATeam)
            R.menu.empty
        else
            R.menu.fragment_events_team_pick

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(menuRes)
                .setTitle(getString(R.string.pick_team))
                .setFragment(teamsFragment)
                .build())
    }

    companion object {

        private val TAG = "TeamPickerFragment"
        private val ARGS_CHANGING = "ARGS_CHANGING"
        private val ARGS_REQUEST_CODE = "ARGS_REQUEST_CODE"

        fun pick(host: FragmentActivity, @IdRes requestCode: Int) {
            assureInstance(host, false, requestCode)

            val instance = getInstance(host, false, requestCode) ?: return

            instance.pick()
        }

        fun change(host: FragmentActivity, @IdRes requestCode: Int) {
            assureInstance(host, true, requestCode)

            val instance = getInstance(host, true, requestCode) ?: return

            instance.pick()
        }

        private fun newInstance(isChanging: Boolean, @IdRes requestCode: Int): TeamPickerFragment {
            val fragment = TeamPickerFragment()
            val args = Bundle()
            args.putBoolean(ARGS_CHANGING, isChanging)
            args.putInt(ARGS_REQUEST_CODE, requestCode)
            fragment.arguments = args
            return fragment
        }

        private fun assureInstance(host: FragmentActivity, isChanging: Boolean, @IdRes requestCode: Int) {
            val fragmentManager = host.supportFragmentManager
            val instance = getInstance(host, isChanging, requestCode)

            if (instance == null)
                fragmentManager.beginTransaction()
                        .add(newInstance(isChanging, requestCode), makeTag(isChanging, requestCode))
                        .commitNow()
        }

        private fun getInstance(host: FragmentActivity, isChanging: Boolean, @IdRes requestCode: Int): TeamPickerFragment? {
            return host.supportFragmentManager.findFragmentByTag(makeTag(isChanging, requestCode)) as TeamPickerFragment?
        }

        private fun makeTag(isChanging: Boolean, @IdRes requestCode: Int): String {
            return "$TAG-$isChanging-$requestCode"
        }
    }
}
