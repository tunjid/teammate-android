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
import androidx.fragment.app.Fragment
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.BottomSheetDriver
import com.mainstreetcode.teammate.fragments.main.FeedFragment
import com.mainstreetcode.teammate.fragments.registration.ResetPasswordFragment
import com.mainstreetcode.teammate.fragments.registration.SplashFragment
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator

class AppNavigator(
        val delegate: MultiStackNavigator,
        val bottomSheetDriver: BottomSheetDriver
) : Navigator by delegate {

    override val currentFragment: Fragment?
        get() = bottomSheetDriver.currentFragment ?: delegate.currentFragment

    override fun push(fragment: Fragment, tag: String): Boolean {
        bottomSheetDriver.hideBottomSheet()
        return delegate.push(fragment, tag)
    }

    override fun <T> push(fragment: T): Boolean where T : Fragment, T : Navigator.TagProvider =
            push(fragment, fragment.stableTag)

    fun show(index: Int) = delegate.show(index)

    fun completeSignIn() {
        for (i in 0 until TAB_COUNT) delegate.navigatorAt(i).clear(includeMatch = true)
        push(FeedFragment.newInstance())
    }

    fun signOut(intent: Intent? = null) {
        for (i in 0 until TAB_COUNT) delegate.navigatorAt(i).clear(includeMatch = true)

        val token: String? = intent?.resetToken()

        if (token == null) push(SplashFragment.newInstance())
        else push(ResetPasswordFragment.newInstance(token))
    }
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