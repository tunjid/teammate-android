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
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.transition.Fade
import android.transition.Transition
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnNextLayout
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
import com.mainstreetcode.teammate.baseclasses.TeammatesBaseActivity
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment
import com.mainstreetcode.teammate.fragments.main.BlankBottomSheetFragment
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
import com.mainstreetcode.teammate.fragments.main.UserEditFragment
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.ViewHolderUtil
import com.mainstreetcode.teammate.util.ViewHolderUtil.updateToolBar
import com.mainstreetcode.teammate.util.nav.BottomNav
import com.mainstreetcode.teammate.util.nav.NavDialogFragment
import com.mainstreetcode.teammate.util.nav.NavItem
import com.mainstreetcode.teammate.viewmodel.TeamViewModel
import com.mainstreetcode.teammate.viewmodel.UserViewModel
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.view.animator.ViewHider
import com.tunjid.androidbootstrap.view.animator.ViewHider.BOTTOM
import com.tunjid.androidbootstrap.view.util.ViewUtil.getLayoutParams
import io.reactivex.disposables.CompositeDisposable

class MainActivity : TeammatesBaseActivity(), BottomSheetController {

    private var bottomNavHeight: Int = 0

    private var bottomToolbarState: BottomSheetController.ToolbarState? = null
    private lateinit var bottomBarHider: ViewHider
    private lateinit var bottomNav: BottomNav

    lateinit var inputRecycledPool: RecyclerView.RecycledViewPool
        private set

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var bottomSheetContainer: ViewGroup
    private lateinit var bottomSheetToolbar: Toolbar
    private lateinit var altToolbar: Toolbar

    private lateinit var userViewModel: UserViewModel

    private lateinit var disposables: CompositeDisposable

    private val lifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
            if (isNotInMainFragmentContainer(v)) return
            val t = f.tag ?: return

            var id = 0

            when {
                t.contains(FeedFragment::class.java.simpleName) -> id = R.id.action_home
                t.contains(EventsFragment::class.java.simpleName) -> id = R.id.action_events
                t.contains(ChatFragment::class.java.simpleName) -> id = R.id.action_messages
                t.contains(MediaFragment::class.java.simpleName) -> id = R.id.action_media
                t.contains(TeamsFragment::class.java.simpleName) -> id = R.id.action_team
            }

            bottomNav.highlight(id)
        }
    }

    override val isBottomSheetShowing: Boolean
        get() = bottomSheetBehavior.state != STATE_HIDDEN

    private val bottomSheetTransition: Transition
        get() = Fade().setDuration(250)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.registerFragmentLifecycleCallbacks(lifecycleCallbacks, false)

        userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        inputRecycledPool = RecyclerView.RecycledViewPool()
        inputRecycledPool.setMaxRecycledViews(Item.INPUT, 10)

        if (!userViewModel.isSignedIn) return startRegistrationActivity(this)

        disposables = CompositeDisposable()

        altToolbar = findViewById(R.id.alt_toolbar)
        bottomSheetToolbar = findViewById(R.id.bottom_toolbar)
        bottomSheetContainer = findViewById(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer)

        toolbar.setNavigationContentDescription(R.string.expand_nav)
        toolbar.setNavigationIcon(R.drawable.ic_supervisor_white_24dp)
        toolbar.setNavigationOnClickListener { showNavOverflow() }
        altToolbar.setOnMenuItemClickListener(this::onAltMenuItemSelected)
        bottomSheetToolbar.setOnMenuItemClickListener(this::onOptionsItemSelected)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == STATE_HIDDEN) restoreHiddenViewState()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        bottomNav = BottomNav.builder().setContainer(findViewById(R.id.bottom_navigation))
                .setSwipeRunnable(this::showNavOverflow)
                .setListener (View.OnClickListener { view -> onNavItemSelected(view.id) })
                .setNavItems(NavItem.create(R.id.action_home, R.string.home, R.drawable.ic_home_black_24dp),
                        NavItem.create(R.id.action_events, R.string.events, R.drawable.ic_event_white_24dp),
                        NavItem.create(R.id.action_messages, R.string.chats, R.drawable.ic_message_black_24dp),
                        NavItem.create(R.id.action_media, R.string.media, R.drawable.ic_video_library_black_24dp),
                        NavItem.create(R.id.action_tournaments, R.string.tourneys, R.drawable.ic_trophy_white_24dp))
                .createBottomNav()

        if (savedInstanceState != null) bottomToolbarState = savedInstanceState.getParcelable(BOTTOM_TOOLBAR_STATE)
        refreshBottomToolbar()

        route(savedInstanceState, intent)
        App.prime()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        val bottomNav = findViewById<View>(R.id.bottom_navigation)
        val bottomBarSnapshot = findViewById<ImageView>(R.id.bottom_nav_snapshot)
        bottomNav.doOnNextLayout {
            bottomNavHeight = bottomNav.height
            getLayoutParams(bottomBarSnapshot).height = bottomNavHeight
        }

        bottomBarHider = ViewHider.of(bottomBarSnapshot).setDuration(HIDER_DURATION.toLong())
                .setDirection(BOTTOM)
                .addStartRunnable {
                    val view = currentFragment
                    if (view == null || view.showsBottomNav()) return@addStartRunnable

                    bottomBarSnapshot.setImageBitmap(bottomNav.drawToBitmap(Bitmap.Config.ARGB_8888))
                    bottomNav.visibility = GONE
                }
                .addEndRunnable {
                    val view = currentFragment
                    if (view == null || !view.showsBottomNav()) return@addEndRunnable

                    bottomNav.visibility = View.VISIBLE
                    initTransition()
                }
                .build()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        route(null, intent)
    }

    override fun onResume() {
        super.onResume()
        val teamViewModel = ViewModelProviders.of(this).get(TeamViewModel::class.java)

        disposables.add(teamViewModel.teamChangeFlowable
                .flatMapSingle { team ->
                    ViewHolderUtil.fetchRoundedDrawable(this,
                            team.imageUrl,
                            resources.getDimensionPixelSize(R.dimen.double_margin), R.drawable.ic_supervisor_white_24dp)
                }
                .subscribe(this::updateToolbarIcon, ErrorHandler.EMPTY::accept))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = onNavItemSelected(item.itemId)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(BOTTOM_TOOLBAR_STATE, bottomToolbarState)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state != STATE_HIDDEN) hideBottomSheet()
        else super.onBackPressed()
    }

    override fun updateAltToolbar(menu: Int, title: CharSequence) {
        updateToolBar(altToolbar, menu, title)
    }

    override fun adjustKeyboardPadding(suggestion: Int): Int {
        var padding = super.adjustKeyboardPadding(suggestion)
        if (padding != bottomInset) padding -= bottomNavHeight
        return padding
    }

    override fun toggleAltToolbar(show: Boolean) {
        val current = currentFragment
        if (show) toggleToolbar(false)
        else if (current != null) toggleToolbar(current.showsToolBar())

        altToolbar.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun toggleToolbar(show: Boolean) {
        super.toggleToolbar(show)
        altToolbar.visibility = View.INVISIBLE
    }

    override fun toggleBottombar(show: Boolean) {
        if (show) bottomBarHider.show()
        else bottomBarHider.hide()
    }

    override fun hideBottomSheet() {
        bottomSheetBehavior.state = STATE_HIDDEN
        restoreHiddenViewState()
    }

    override fun showBottomSheet(args: BottomSheetController.Args) {
        val fragmentManager = supportFragmentManager ?: return

        clearTransientBars()

        val topPadding = topInset + resources.getDimensionPixelSize(R.dimen.single_margin)
        bottomSheetContainer.setPadding(0, topPadding, 0, 0)

        val toShow = args.fragment
        toShow.enterTransition = bottomSheetTransition
        toShow.exitTransition = bottomSheetTransition

        fragmentManager.beginTransaction()
                .replace(R.id.bottom_sheet_view, toShow, toShow.stableTag)
                .runOnCommit {
                    bottomToolbarState = args.toolbarState
                    bottomSheetBehavior.state = STATE_EXPANDED
                    refreshBottomToolbar()
                }.commit()
    }

    override fun showFragment(fragment: BaseFragment): Boolean {
        hideBottomSheet()
        return super.showFragment(fragment)
    }

    private fun onAltMenuItemSelected(item: MenuItem): Boolean {
        val current = currentFragment
        return current != null && current.onOptionsItemSelected(item)
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

    private fun onNavItemSelected(@IdRes id: Int): Boolean {
        when (id) {
            R.id.action_home -> {
                showFragment(FeedFragment.newInstance())
                return true
            }
            R.id.action_events -> {
                TeamPickerFragment.pick(this, R.id.request_event_team_pick)
                return true
            }
            R.id.action_messages -> {
                TeamPickerFragment.pick(this, R.id.request_chat_team_pick)
                return true
            }
            R.id.action_media -> {
                TeamPickerFragment.pick(this, R.id.request_media_team_pick)
                return true
            }
            R.id.action_tournaments -> {
                TeamPickerFragment.pick(this, R.id.request_tournament_team_pick)
                return true
            }
            R.id.action_games -> {
                TeamPickerFragment.pick(this, R.id.request_game_team_pick)
                return true
            }
            R.id.action_find_teams -> {
                showFragment(TeamSearchFragment.newInstance())
                return true
            }
            R.id.action_team -> {
                showFragment(TeamsFragment.newInstance())
                return true
            }
            R.id.action_expand_home_nav -> {
                showNavOverflow()
                return true
            }
            R.id.action_settings -> {
                showFragment(SettingsFragment.newInstance())
                return true
            }
            R.id.action_rsvp_list -> {
                showFragment(MyEventsFragment.newInstance())
                return true
            }
            R.id.action_public_events -> {
                showFragment(EventSearchFragment.newInstance())
                return true
            }
            R.id.action_head_to_head -> {
                showFragment(HeadToHeadFragment.newInstance())
                return true
            }
            R.id.action_stats_aggregate -> {
                showFragment(StatAggregateFragment.newInstance())
                return true
            }
            R.id.action_declined_competitions -> {
                showFragment(DeclinedCompetitionsFragment.newInstance())
                return true
            }
            R.id.action_my_profile -> {
                showFragment(UserEditFragment.newInstance(userViewModel.currentUser))
                return true
            }
            else -> return false
        }
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
        }?.let(this@MainActivity::showFragment)
                ?: if (savedInstanceState == null) showFragment(FeedFragment.newInstance())
    }

    private fun refreshBottomToolbar() {
        if (bottomToolbarState == null) return
        bottomSheetToolbar.menu.clear()
        bottomSheetToolbar.inflateMenu(bottomToolbarState!!.menuRes)
        bottomSheetToolbar.title = bottomToolbarState!!.title
    }

    private fun restoreHiddenViewState() {
        currentFragment ?: return

        val onCommit = {
            val post = currentFragment
            if (post != null && post.view != null) post.togglePersistentUi()
        }

        val fragmentManager = supportFragmentManager ?: return

        val fragment = BlankBottomSheetFragment.newInstance()
        fragmentManager.beginTransaction()
                .replace(R.id.bottom_sheet_view, fragment, fragment.stableTag)
                .runOnCommit(onCommit)
                .commit()
    }

    companion object {

        const val FEED_DEEP_LINK = "feed-deep-link"
        const val BOTTOM_TOOLBAR_STATE = "BOTTOM_TOOLBAR_STATE"

        fun startRegistrationActivity(activity: Activity) {
            val main = Intent(activity, RegistrationActivity::class.java)
            activity.startActivity(main)
            activity.finish()
        }
    }

}
