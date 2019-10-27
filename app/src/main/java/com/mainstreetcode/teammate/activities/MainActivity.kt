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
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.baseclasses.BottomSheetController
import com.mainstreetcode.teammate.baseclasses.BottomSheetDriver
import com.mainstreetcode.teammate.baseclasses.GlobalUiController
import com.mainstreetcode.teammate.baseclasses.TransientBarController
import com.mainstreetcode.teammate.baseclasses.TransientBarDriver
import com.mainstreetcode.teammate.baseclasses.WindowInsetsDriver
import com.mainstreetcode.teammate.baseclasses.globalUiDriver
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.UiState
import com.mainstreetcode.teammate.navigation.AppNavigator
import com.mainstreetcode.teammate.util.isInDarkMode
import com.mainstreetcode.teammate.viewmodel.PrefsViewModel
import com.tunjid.androidx.navigation.Navigator

class MainActivity : AppCompatActivity(R.layout.activity_main),
        GlobalUiController,
        BottomSheetController,
        TransientBarController,
        Navigator.Controller {

    lateinit var inputRecycledPool: RecyclerView.RecycledViewPool
        private set

    override var uiState: UiState by globalUiDriver { navigator.current }

    override val navigator: AppNavigator by lazy { AppNavigator(this) }

    override val transientBarDriver: TransientBarDriver
        get() = navigator.transientBarDriver

    override val bottomSheetDriver: BottomSheetDriver
        get() = navigator.bottomSheetDriver

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(ViewModelProviders.of(this).get(PrefsViewModel::class.java).nightUiMode)
        setTheme(if (isInDarkMode) R.style.AppDarkTheme else R.style.AppTheme)

        super.onCreate(savedInstanceState)

        supportFragmentManager.registerFragmentLifecycleCallbacks(windowInsetsDriver(), true)
        supportFragmentManager.registerFragmentLifecycleCallbacks(transientBarCallback(), true)

        inputRecycledPool = RecyclerView.RecycledViewPool()
        inputRecycledPool.setMaxRecycledViews(Item.INPUT, 10)

        onBackPressedDispatcher.addCallback(this) { navigator.pop() }
        onBackPressedDispatcher.addCallback(this, bottomSheetDriver)

        App.prime()

        navigator.checkDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navigator.checkDeepLink(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = navigator.onNavItemSelected(item.itemId)

    private fun adjustKeyboardPadding(suggestion: Int): Int {
        var padding = suggestion
        if (padding != WindowInsetsDriver.bottomInset && uiState.bottomNavShows) padding -= navigator.bottomNavHeight
        return padding
    }

    private fun windowInsetsDriver(): WindowInsetsDriver = WindowInsetsDriver(
            stackNavigatorSource = this.navigator::activeNavigator,
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