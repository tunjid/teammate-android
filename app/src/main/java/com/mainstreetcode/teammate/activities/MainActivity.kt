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

import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
import com.mainstreetcode.teammate.baseclasses.BottomSheetDriver
import com.mainstreetcode.teammate.baseclasses.GlobalUiController
import com.mainstreetcode.teammate.baseclasses.HIDER_DURATION
import com.mainstreetcode.teammate.baseclasses.TransientBarController
import com.mainstreetcode.teammate.baseclasses.TransientBarDriver
import com.mainstreetcode.teammate.baseclasses.WindowInsetsDriver
import com.mainstreetcode.teammate.baseclasses.globalUiDriver
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
import com.mainstreetcode.teammate.model.UiState
import com.mainstreetcode.teammate.navigation.AppNavigator
import com.mainstreetcode.teammate.navigation.TAB_COUNT
import com.mainstreetcode.teammate.navigation.toNavId
import com.mainstreetcode.teammate.navigation.toRequestId
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.fetchRoundedDrawable
import com.mainstreetcode.teammate.util.isInDarkMode
import com.mainstreetcode.teammate.util.nav.BottomNav
import com.mainstreetcode.teammate.util.nav.NavDialogFragment
import com.mainstreetcode.teammate.util.nav.NavItem
import com.mainstreetcode.teammate.viewmodel.PrefsViewModel
import com.mainstreetcode.teammate.viewmodel.TeamViewModel
import com.mainstreetcode.teammate.viewmodel.UserViewModel
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.multiStackNavigationController
import com.tunjid.androidx.savedstate.savedStateFor
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity(R.layout.activity_main),
        GlobalUiController,
        BottomSheetController,
        TransientBarController,
        Navigator.Controller {

    private var bottomNavHeight: Int = 0

    private lateinit var bottomNav: BottomNav

    lateinit var inputRecycledPool: RecyclerView.RecycledViewPool
        private set

    private lateinit var toolbar: Toolbar

    private val disposables: CompositeDisposable = CompositeDisposable()

    private val userViewModel by viewModels<UserViewModel>()

    private val teamViewModel by viewModels<TeamViewModel>()

    private val multiStackNavigator: MultiStackNavigator by multiStackNavigationController(
            TAB_COUNT,
            R.id.main_fragment_container
    ) {
        when (it.toNavId) {
            R.id.action_home -> FeedFragment.newInstance().run { this to stableTag }
            R.id.action_events -> EventsFragment.newInstance(teamViewModel.defaultTeam).run { this to stableTag }
            R.id.action_messages -> ChatFragment.newInstance(teamViewModel.defaultTeam).run { this to stableTag }
            R.id.action_media -> MediaFragment.newInstance(teamViewModel.defaultTeam).run { this to stableTag }
            R.id.action_tournaments -> TournamentsFragment.newInstance(teamViewModel.defaultTeam).run { this to stableTag }
            else -> FeedFragment.newInstance().run { this to stableTag }
        }
    }

    override var uiState: UiState by globalUiDriver { navigator.current }

    override val transientBarDriver: TransientBarDriver by lazy {
        TransientBarDriver(findViewById(R.id.coordinator), findViewById(R.id.fab))
    }

    override val bottomSheetDriver: BottomSheetDriver by lazy {
        BottomSheetDriver(
                this,
                savedStateFor(this, "BottomSheet"),
                findViewById(R.id.bottom_sheet),
                findViewById(R.id.bottom_toolbar),
                transientBarDriver
        )
    }

    override val navigator: AppNavigator by lazy { AppNavigator(multiStackNavigator, bottomSheetDriver) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(ViewModelProviders.of(this).get(PrefsViewModel::class.java).nightUiMode)
        setTheme(if (isInDarkMode) R.style.AppDarkTheme else R.style.AppTheme)

        super.onCreate(savedInstanceState)

        supportFragmentManager.registerFragmentLifecycleCallbacks(windowInsetsDriver(), true)
        supportFragmentManager.registerFragmentLifecycleCallbacks(transientBarCallback(), true)

        inputRecycledPool = RecyclerView.RecycledViewPool()
        inputRecycledPool.setMaxRecycledViews(Item.INPUT, 10)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationContentDescription(R.string.expand_nav)
        toolbar.setNavigationIcon(R.drawable.ic_supervisor_white_24dp)
        toolbar.setNavigationOnClickListener { showNavOverflow() }

        bottomNav = BottomNav.builder().setContainer(findViewById<LinearLayout>(R.id.bottom_navigation)
                .apply { doOnLayout { bottomNavHeight = height } })
                .setSwipeRunnable(this::showNavOverflow)
                .setListener(View.OnClickListener { view -> onNavItemSelected(view.id) })
                .setNavItems(NavItem.create(R.id.action_home, R.string.home, R.drawable.ic_home_black_24dp),
                        NavItem.create(R.id.action_events, R.string.events, R.drawable.ic_event_white_24dp),
                        NavItem.create(R.id.action_messages, R.string.chats, R.drawable.ic_message_black_24dp),
                        NavItem.create(R.id.action_media, R.string.media, R.drawable.ic_video_library_black_24dp),
                        NavItem.create(R.id.action_tournaments, R.string.tourneys, R.drawable.ic_trophy_white_24dp))
                .createBottomNav()

        multiStackNavigator.stackSelectedListener = { bottomNav.highlight(it.toNavId) }
        multiStackNavigator.stackTransactionModifier = { crossFade() }
        multiStackNavigator.transactionModifier = { incomingFragment ->
            val current = navigator.current
            if (current is Navigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
            else crossFade()
        }

        onBackPressedDispatcher.addCallback(this) { navigator.pop() }
        onBackPressedDispatcher.addCallback(this, bottomSheetDriver)

        App.prime()

        if (!userViewModel.isSignedIn) navigator.signOut(intent)
        else route(savedInstanceState, intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        route(null, intent)
    }

    override fun onResume() {
        super.onResume()
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

    private fun adjustKeyboardPadding(suggestion: Int): Int {
        var padding = suggestion
        if (padding != WindowInsetsDriver.bottomInset && uiState.bottomNavShows) padding -= bottomNavHeight
        return padding
    }

    private fun showNavOverflow() = NavDialogFragment.newInstance().show(supportFragmentManager, "")

    private fun updateToolbarIcon(drawable: Drawable) {
        val current = toolbar.navigationIcon
        val updated = TransitionDrawable(if (current == null) arrayOf(drawable)
        else arrayOf(current.current, drawable))

        toolbar.navigationIcon = updated
        if (current != null) updated.startTransition(HIDER_DURATION)
    }

    private fun onNavItemSelected(@IdRes id: Int): Boolean = when (id) {
        R.id.action_expand_home_nav -> showNavOverflow().let { true }
        R.id.action_home -> multiStackNavigator.show(0).let { true }
        R.id.action_events,
        R.id.action_messages,
        R.id.action_media,
        R.id.action_tournaments -> TeamPickerFragment.pick(this, id.toRequestId).let { true }
        else -> navigator.push(when (id) {
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

    private fun route(savedInstanceState: Bundle?, intent: Intent) = when (val model: Model<*>? = intent.getParcelableExtra(FEED_DEEP_LINK)) {
        is Game -> GameFragment.newInstance(model)
        is Chat -> ChatFragment.newInstance(model.team)
        is Event -> EventEditFragment.newInstance(model)
        is Tournament -> TournamentDetailFragment.newInstance(model)
        is JoinRequest -> TeamMembersFragment.newInstance(model.team)
        else -> if (savedInstanceState == null) FeedFragment.newInstance() else null
    }?.let(navigator::push)

    private fun windowInsetsDriver(): WindowInsetsDriver = WindowInsetsDriver(
            stackNavigatorSource = this.multiStackNavigator::activeNavigator,
            parentContainer = findViewById(R.id.content_view),
            contentContainer = findViewById(R.id.main_fragment_container),
            coordinatorLayout = findViewById(R.id.coordinator),
            toolbar = findViewById(R.id.toolbar),
            topInsetView = findViewById(R.id.top_inset),
            bottomInsetView = findViewById(R.id.bottom_inset),
            keyboardPadding = findViewById(R.id.padding),
            insetAdjuster = this::adjustKeyboardPadding
    )

    private fun transientBarCallback() = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
            if (f.id == navigator.containerId) transientBarDriver.clearTransientBars()
        }
    }
}

const val FEED_DEEP_LINK = "feed-deep-link"

private fun FragmentTransaction.crossFade() = setCustomAnimations(
        android.R.anim.fade_in,
        android.R.anim.fade_out,
        android.R.anim.fade_in,
        android.R.anim.fade_out
)