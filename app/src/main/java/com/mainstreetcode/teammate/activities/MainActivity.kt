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

package com.mainstreetcode.teammate.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
import com.mainstreetcode.teammate.baseclasses.BottomSheetDriver
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseActivity
import com.mainstreetcode.teammate.baseclasses.WindowInsetsDriver
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment
import com.mainstreetcode.teammate.fragments.main.ChatFragment
import com.mainstreetcode.teammate.fragments.main.DeclinedCompetitionsFragment
import com.mainstreetcode.teammate.fragments.main.EventEditFragment
import com.mainstreetcode.teammate.fragments.main.EventSearchFragment
import com.mainstreetcode.teammate.fragments.main.EventsFragment
import com.mainstreetcode.teammate.fragments.main.FeedFragment
import com.mainstreetcode.teammate.fragments.main.GameFragment
import com.mainstreetcode.teammate.fragments.main.HeadToHeadFragment
import com.mainstreetcode.teammate.fragments.main.MediaFragment
import com.mainstreetcode.teammate.fragments.main.MyEventsFragment
import com.mainstreetcode.teammate.fragments.main.SettingsFragment
import com.mainstreetcode.teammate.fragments.main.StatAggregateFragment
import com.mainstreetcode.teammate.fragments.main.TeamMembersFragment
import com.mainstreetcode.teammate.fragments.main.TeamSearchFragment
import com.mainstreetcode.teammate.fragments.main.TeamsFragment
import com.mainstreetcode.teammate.fragments.main.TournamentDetailFragment
import com.mainstreetcode.teammate.fragments.main.TournamentsFragment
import com.mainstreetcode.teammate.fragments.main.UserEditFragment
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.fetchRoundedDrawable
import com.mainstreetcode.teammate.util.nav.BottomNav
import com.mainstreetcode.teammate.util.nav.NavDialogFragment
import com.mainstreetcode.teammate.util.nav.NavItem
import com.mainstreetcode.teammate.viewmodel.TeamViewModel
import com.mainstreetcode.teammate.viewmodel.UserViewModel
import com.tunjid.androidbootstrap.core.components.StackNavigator
import com.tunjid.androidbootstrap.core.components.savedStateFor
import com.tunjid.androidbootstrap.core.components.stackNavigationController
import io.reactivex.disposables.CompositeDisposable

class MainActivity : TeammatesBaseActivity(R.layout.activity_main),
        BottomSheetController,
        StackNavigator.NavigationController {

    override val navigator: StackNavigator by stackNavigationController(R.id.main_fragment_container)

    private var bottomNavHeight: Int = 0

    private lateinit var bottomNav: BottomNav

    lateinit var inputRecycledPool: RecyclerView.RecycledViewPool
        private set

    private lateinit var toolbar: Toolbar

    private lateinit var userViewModel: UserViewModel

    private lateinit var disposables: CompositeDisposable

    override val bottomSheetDriver: BottomSheetDriver by lazy {
        BottomSheetDriver(
                this,
                savedStateFor(this, "BottomSheet"),
                findViewById(R.id.bottom_sheet),
                findViewById(R.id.bottom_toolbar),
                transientBarDriver
        )
    }

    private val lifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
            if (f.id == navigator.containerId) bottomSheetDriver.hideBottomSheet()
        }

        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
            if (f.id != navigator.containerId) return
            val t = f.tag ?: return

            var id = 0

            when {
                t.contains(FeedFragment::class.java.simpleName) -> id = R.id.action_home
                t.contains(EventsFragment::class.java.simpleName) -> id = R.id.action_events
                t.contains(ChatFragment::class.java.simpleName) -> id = R.id.action_messages
                t.contains(MediaFragment::class.java.simpleName) -> id = R.id.action_media
                t.contains(TeamsFragment::class.java.simpleName) -> id = R.id.action_team
                t.contains(TournamentsFragment::class.java.simpleName) -> id = R.id.action_tournaments
            }

            bottomNav.highlight(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.registerFragmentLifecycleCallbacks(lifecycleCallbacks, false)

        userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        inputRecycledPool = RecyclerView.RecycledViewPool()
        inputRecycledPool.setMaxRecycledViews(Item.INPUT, 10)

        if (!userViewModel.isSignedIn) return startRegistrationActivity(this)

        disposables = CompositeDisposable()

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationContentDescription(R.string.expand_nav)
        toolbar.setNavigationIcon(R.drawable.ic_supervisor_white_24dp)
        toolbar.setNavigationOnClickListener { showNavOverflow() }

        bottomNav = BottomNav.builder().setContainer(findViewById(R.id.bottom_navigation))
                .setSwipeRunnable(this::showNavOverflow)
                .setListener(View.OnClickListener { view -> onNavItemSelected(view.id) })
                .setNavItems(NavItem.create(R.id.action_home, R.string.home, R.drawable.ic_home_black_24dp),
                        NavItem.create(R.id.action_events, R.string.events, R.drawable.ic_event_white_24dp),
                        NavItem.create(R.id.action_messages, R.string.chats, R.drawable.ic_message_black_24dp),
                        NavItem.create(R.id.action_media, R.string.media, R.drawable.ic_video_library_black_24dp),
                        NavItem.create(R.id.action_tournaments, R.string.tourneys, R.drawable.ic_trophy_white_24dp))
                .createBottomNav()

        onBackPressedDispatcher.addCallback(this, bottomSheetDriver)

        route(savedInstanceState, intent)
        App.prime()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        route(null, intent)
    }

    override fun onResume() {
        super.onResume()
        val teamViewModel = ViewModelProviders.of(this).get(TeamViewModel::class.java)

        disposables.add(teamViewModel.teamChangeFlowable.flatMapMaybe { team ->
            fetchRoundedDrawable(this,
                    team.imageUrl,
                    resources.getDimensionPixelSize(R.dimen.double_margin), R.drawable.ic_supervisor_white_24dp)
        }.subscribe(this::updateToolbarIcon, ErrorHandler.EMPTY::invoke))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = onNavItemSelected(item.itemId)

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    override fun adjustKeyboardPadding(suggestion: Int): Int {
        var padding = super.adjustKeyboardPadding(suggestion)
        if (padding != WindowInsetsDriver.bottomInset) padding -= bottomNavHeight
        return padding
    }

    private fun showNavOverflow() {
        NavDialogFragment.newInstance().show(supportFragmentManager, "")
    }

    private fun updateToolbarIcon(drawable: Drawable) {
        val current = toolbar.navigationIcon
        val updated = TransitionDrawable(if (current == null) arrayOf(drawable)
        else arrayOf(current.current, drawable))

        toolbar.navigationIcon = updated
        if (current != null) updated.startTransition(HIDER_DURATION)
    }

    private fun onNavItemSelected(@IdRes id: Int): Boolean = when (id) {
        R.id.action_home -> {
            navigator.show(FeedFragment.newInstance())
        }
        R.id.action_events -> {
            TeamPickerFragment.pick(this, R.id.request_event_team_pick).let { true }
        }
        R.id.action_messages -> {
            TeamPickerFragment.pick(this, R.id.request_chat_team_pick).let { true }
        }
        R.id.action_media -> {
            TeamPickerFragment.pick(this, R.id.request_media_team_pick).let { true }
        }
        R.id.action_tournaments -> {
            TeamPickerFragment.pick(this, R.id.request_tournament_team_pick).let { true }
        }
        R.id.action_games -> {
            TeamPickerFragment.pick(this, R.id.request_game_team_pick).let { true }
        }
        R.id.action_find_teams -> {
            navigator.show(TeamSearchFragment.newInstance())
        }
        R.id.action_team -> {
            navigator.show(TeamsFragment.newInstance())
        }
        R.id.action_expand_home_nav -> {
            showNavOverflow().let { true }
        }
        R.id.action_settings -> {
            navigator.show(SettingsFragment.newInstance())
        }
        R.id.action_rsvp_list -> {
            navigator.show(MyEventsFragment.newInstance())
        }
        R.id.action_public_events -> {
            navigator.show(EventSearchFragment.newInstance())
        }
        R.id.action_head_to_head -> {
            navigator.show(HeadToHeadFragment.newInstance())
        }
        R.id.action_stats_aggregate -> {
            navigator.show(StatAggregateFragment.newInstance())
        }
        R.id.action_declined_competitions -> {
            navigator.show(DeclinedCompetitionsFragment.newInstance())
        }
        R.id.action_my_profile -> {
            navigator.show(UserEditFragment.newInstance(userViewModel.currentUser))
        }
        else -> false
    }

    private fun route(savedInstanceState: Bundle?, intent: Intent) {
        val model = intent.getParcelableExtra<Model<*>>(FEED_DEEP_LINK)

        when (model) {
            is Game -> GameFragment.newInstance(model)
            is Chat -> ChatFragment.newInstance(model.team)
            is Event -> EventEditFragment.newInstance(model)
            is JoinRequest -> TeamMembersFragment.newInstance(model.team)
            is Tournament -> TournamentDetailFragment.newInstance(model)
            else -> null
        }?.let { this@MainActivity.navigator.show(it) }
                ?: if (savedInstanceState == null) navigator.show(FeedFragment.newInstance())
    }

    companion object {

        const val FEED_DEEP_LINK = "feed-deep-link"

        fun startRegistrationActivity(activity: Activity) {
            val main = Intent(activity, RegistrationActivity::class.java)
            activity.startActivity(main)
            activity.finish()
        }
    }

}
