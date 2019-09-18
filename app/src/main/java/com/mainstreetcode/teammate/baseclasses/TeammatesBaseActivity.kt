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

package com.mainstreetcode.teammate.baseclasses

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.UiState
import com.mainstreetcode.teammate.util.isInDarkMode
import com.mainstreetcode.teammate.viewmodel.PrefsViewModel
import com.tunjid.androidbootstrap.core.components.StackNavigator

/**
 * Base Activity for the app
 */

abstract class TeammatesBaseActivity(layoutRes: Int = 0) : AppCompatActivity(layoutRes),
        GlobalUiController,
        TransientBarController,
        StackNavigator.NavigationController {

    override var uiState: UiState by globalUiDriver { navigator.currentFragment }

    override val transientBarDriver: TransientBarDriver by lazy {
        TransientBarDriver(findViewById(R.id.coordinator), findViewById(R.id.fab))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(ViewModelProviders.of(this).get(PrefsViewModel::class.java).nightUiMode)

        setTheme(if (isInDarkMode) R.style.AppDarkTheme else R.style.AppTheme)

        super.onCreate(savedInstanceState)
        supportFragmentManager.registerFragmentLifecycleCallbacks(
                WindowInsetsDriver(
                        stackNavigatorSource = this::navigator,
                        parentContainer = findViewById(R.id.content_view),
                        contentContainer = findViewById(R.id.main_fragment_container),
                        coordinatorLayout = findViewById(R.id.coordinator),
                        toolbar = findViewById(R.id.toolbar),
                        topInsetView = findViewById(R.id.top_inset),
                        bottomInsetView = findViewById(R.id.bottom_inset),
                        keyboardPadding = findViewById(R.id.padding),
                        insetAdjuster = this::adjustKeyboardPadding
                ), true)

        supportFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                        if (f.id != navigator.containerId) return

                        transientBarDriver.clearTransientBars()
                    }
                }, true)

        navigator.transactionModifier = { incomingFragment ->
            val current = navigator.currentFragment
            if (current is StackNavigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
            else setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            )
        }

        onBackPressedDispatcher.addCallback(this) { navigator.pop() }

        val color = ContextCompat.getColor(this, R.color.transparent)
        window.statusBarColor = color
        window.navigationBarColor = color
    }

    fun onDialogDismissed() {
//        val fragment = currentFragment
//        val showFab = fragment != null && fragment.showsFab
//
//        if (showFab) toggleFab(true)
    }

    protected open fun adjustKeyboardPadding(suggestion: Int): Int = suggestion


    companion object {

        const val HIDER_DURATION = 300

    }
}
