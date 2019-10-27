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

package com.mainstreetcode.teammate.navigation

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.activities.FEED_DEEP_LINK
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
import com.mainstreetcode.teammate.baseclasses.BottomSheetDriver
import com.mainstreetcode.teammate.baseclasses.TransientBarController
import com.mainstreetcode.teammate.baseclasses.TransientBarDriver
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
import com.mainstreetcode.teammate.fragments.registration.ResetPasswordFragment
import com.mainstreetcode.teammate.fragments.registration.SplashFragment
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.util.nav.BottomNav
import com.mainstreetcode.teammate.util.nav.NavDialogFragment
import com.mainstreetcode.teammate.util.nav.NavItem
import com.mainstreetcode.teammate.viewmodel.TeamViewModel
import com.mainstreetcode.teammate.viewmodel.UserViewModel
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.multiStackNavigationController
import com.tunjid.androidx.savedstate.savedStateFor

class AppNavigator(private val host: FragmentActivity) :
        Navigator,
        BottomSheetController,
        TransientBarController {

    private val userViewModel by host.viewModels<UserViewModel>()

    private val teamViewModel by host.viewModels<TeamViewModel>()

    private val bottomNav: BottomNav

    private val delegate: MultiStackNavigator by host.multiStackNavigationController(TAB_COUNT, R.id.main_fragment_container, this::route)

    var bottomNavHeight: Int = 0

    val activeNavigator get() = delegate.activeNavigator

    init {
        bottomNav = BottomNav.builder().setContainer(host.findViewById<LinearLayout>(R.id.bottom_navigation)
                .apply { doOnLayout { bottomNavHeight = height } })
                .setSwipeRunnable(this::showNavOverflow)
                .setListener(View.OnClickListener { view -> onNavItemSelected(view.id) })
                .setNavItems(NavItem.create(R.id.action_home, R.string.home, R.drawable.ic_home_black_24dp),
                        NavItem.create(R.id.action_events, R.string.events, R.drawable.ic_event_white_24dp),
                        NavItem.create(R.id.action_messages, R.string.chats, R.drawable.ic_message_black_24dp),
                        NavItem.create(R.id.action_media, R.string.media, R.drawable.ic_video_library_black_24dp),
                        NavItem.create(R.id.action_tournaments, R.string.tourneys, R.drawable.ic_trophy_white_24dp))
                .createBottomNav()

        delegate.stackSelectedListener = { bottomNav.highlight(it.toNavId) }
        delegate.stackTransactionModifier = { crossFade() }
        delegate.transactionModifier = { incomingFragment ->
            val current = delegate.current
            if (current is Navigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
            else crossFade()
        }
    }

    override val transientBarDriver: TransientBarDriver by lazy {
        TransientBarDriver(host.findViewById(R.id.coordinator), host.findViewById(R.id.fab))
    }

    override val bottomSheetDriver: BottomSheetDriver by lazy {
        BottomSheetDriver(
                host,
                savedStateFor(host, "BottomSheet"),
                host.findViewById(R.id.bottom_sheet),
                host.findViewById(R.id.bottom_toolbar),
                transientBarDriver
        )
    }
    override val containerId: Int get() = delegate.containerId

    override val previous: Fragment? get() = delegate.previous

    override val current: Fragment? get() = bottomSheetDriver.current ?: delegate.current

    override fun clear(upToTag: String?, includeMatch: Boolean) = delegate.clear(upToTag, includeMatch)

    override fun pop(): Boolean = delegate.pop()

    override fun push(fragment: Fragment, tag: String): Boolean {
        bottomSheetDriver.hideBottomSheet()
        return delegate.push(fragment, tag)
    }

    override fun <T> push(fragment: T): Boolean where T : Fragment, T : Navigator.TagProvider =
            push(fragment, fragment.stableTag)

    fun show(index: Int) = delegate.show(index)

    fun completeSignIn() = delegate.clearAll()

    fun signOut(intent: Intent? = null) {
        delegate.clearAll()

//        val token: String? = intent?.resetToken()
    }

    fun showNavOverflow() = NavDialogFragment.newInstance().show(host.supportFragmentManager, "")

    fun onNavItemSelected(@IdRes id: Int): Boolean = when (id) {
        R.id.action_expand_home_nav -> showNavOverflow().let { true }
        R.id.action_home -> delegate.show(0).let { true }
        R.id.action_events,
        R.id.action_messages,
        R.id.action_media,
        R.id.action_tournaments -> TeamPickerFragment.pick(host, id.toRequestId).let { true }
        else -> push(when (id) {
            R.id.action_find_teams -> TeamSearchFragment.newInstance()
            R.id.action_team -> TeamsFragment.newInstance()
            R.id.action_settings -> SettingsFragment.newInstance()
            R.id.action_rsvp_list -> MyEventsFragment.newInstance()
            R.id.action_public_events -> EventSearchFragment.newInstance()
            R.id.action_head_to_head -> HeadToHeadFragment.newInstance()
            R.id.action_stats_aggregate -> StatAggregateFragment.newInstance()
            R.id.action_declined_competitions -> DeclinedCompetitionsFragment.newInstance()
            R.id.action_my_profile -> UserEditFragment.newInstance(userViewModel.currentUser)
            else -> TeamSearchFragment.newInstance()
        }).let { true }
    }

    private fun route(it: Int): Pair<Fragment, String> = when (it.toNavId) {
        R.id.action_home -> when (userViewModel.isSignedIn) {
            true -> FeedFragment.newInstance().run { this to stableTag }
            else -> when (val token = host.intent?.resetToken()) {
                null -> SplashFragment.newInstance().run { this to stableTag }
                else -> ResetPasswordFragment.newInstance(token).run { this to stableTag }
            }
        }
        R.id.action_events -> EventsFragment.newInstance(teamViewModel.defaultTeam).run { this to stableTag }
        R.id.action_messages -> ChatFragment.newInstance(teamViewModel.defaultTeam).run { this to stableTag }
        R.id.action_media -> MediaFragment.newInstance(teamViewModel.defaultTeam).run { this to stableTag }
        R.id.action_tournaments -> TournamentsFragment.newInstance(teamViewModel.defaultTeam).run { this to stableTag }
        else -> FeedFragment.newInstance().run { this to stableTag }
    }

    fun route(intent: Intent) = when (val model: Model<*>? = intent.getParcelableExtra(FEED_DEEP_LINK)) {
        is Game -> GameFragment.newInstance(model)
        is Chat -> ChatFragment.newInstance(model.team)
        is Event -> EventEditFragment.newInstance(model)
        is Tournament -> TournamentDetailFragment.newInstance(model)
        is JoinRequest -> TeamMembersFragment.newInstance(model.team)
        else -> null
    }?.let(::push)
}

private val bottomNavItems = intArrayOf(
        R.id.action_home,
        R.id.action_events,
        R.id.action_messages,
        R.id.action_media,
        R.id.action_tournaments
)

val TAB_COUNT = bottomNavItems.size
private const val TOKEN = "token"

val Int.toRequestId
    get() = when (this) {
        R.id.action_events -> R.id.request_event_team_pick
        R.id.action_messages -> R.id.request_chat_team_pick
        R.id.action_media -> R.id.request_media_team_pick
        R.id.action_tournaments -> R.id.request_tournament_team_pick
        R.id.action_games -> R.id.request_game_team_pick
        else -> R.id.request_default_team_pick
    }

val Int.requestIdToNavIndex
    get() = when (this) {
        R.id.request_game_team_pick -> R.id.action_games.toNavIndex
        R.id.request_chat_team_pick -> R.id.action_messages.toNavIndex
        R.id.request_event_team_pick -> R.id.action_events.toNavIndex
        R.id.request_media_team_pick -> R.id.action_media.toNavIndex
        R.id.request_tournament_team_pick -> R.id.action_tournaments.toNavIndex
        R.id.request_default_team_pick -> R.id.action_home.toNavIndex
        else -> R.id.action_home.toNavIndex
    }

val Int.toNavIndex
    get() = bottomNavItems.indexOf(this)

val Int.toNavId
    get() = bottomNavItems[this]

private fun Intent.resetToken(): String? {
    val uri = data ?: return null

    val domain1 = App.instance.getString(R.string.domain_1)
    val domain2 = App.instance.getString(R.string.domain_2)

    val path = uri.path ?: return null
    val domainMatches = domain1 == uri.host || domain2 == uri.host

    return if (domainMatches && path.contains("forgotPassword")) uri.getQueryParameter(TOKEN)
    else null
}

private fun FragmentTransaction.crossFade() = setCustomAnimations(
        android.R.anim.fade_in,
        android.R.anim.fade_out,
        android.R.anim.fade_in,
        android.R.anim.fade_out
)