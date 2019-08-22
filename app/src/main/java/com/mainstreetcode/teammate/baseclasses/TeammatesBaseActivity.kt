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

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.view.View.GONE
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar
import com.mainstreetcode.teammate.adapters.viewholders.LoadingBar
import com.mainstreetcode.teammate.model.UiState
import com.mainstreetcode.teammate.util.FabInteractor
import com.mainstreetcode.teammate.util.TOOLBAR_ANIM_DELAY
import com.mainstreetcode.teammate.util.isDisplayingSystemUI
import com.mainstreetcode.teammate.util.resolveThemeColor
import com.mainstreetcode.teammate.util.updateToolBar
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity
import com.tunjid.androidbootstrap.view.animator.ViewHider
import com.tunjid.androidbootstrap.view.animator.ViewHider.BOTTOM
import com.tunjid.androidbootstrap.view.animator.ViewHider.TOP
import com.tunjid.androidbootstrap.view.util.ViewUtil
import java.util.*
import kotlin.math.max

/**
 * Base Activity for the app
 */

abstract class TeammatesBaseActivity : BaseActivity(), PersistentUiController {
    private var leftInset: Int = 0
    private var rightInset: Int = 0
    var bottomInset: Int = 0

    private var insetsApplied: Boolean = false

    private lateinit var bottomInsetView: View
    private lateinit var topInsetView: View

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var fab: MaterialButton
    private lateinit var padding: View

    protected lateinit var toolbar: Toolbar

    private lateinit var fabHider: ViewHider
    private lateinit var toolbarHider: ViewHider
    private lateinit var fabInteractor: FabInteractor

    private var loadingBar: LoadingBar? = null

    private lateinit var uiState: UiState

    private val transientBottomBars = ArrayList<BaseTransientBottomBar<*>>()

    private val callback = object : BaseTransientBottomBar.BaseCallback<BaseTransientBottomBar<*>>() {
        override fun onDismissed(bar: BaseTransientBottomBar<*>?, event: Int) {
            transientBottomBars.remove(bar)
        }
    }

    private val fragmentViewCreatedCallback: FragmentManager.FragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
            if (isNotInMainFragmentContainer(v)) return

            clearTransientBars()
            adjustSystemInsets(f)
            setOnApplyWindowInsetsListener(v) { _, insets -> consumeFragmentInsets(insets) }
        }
    }

    private val decorView: View
        get() = window.decorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentViewCreatedCallback, false)
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)

        uiState = if (savedInstanceState == null) UiState.freshState() else savedInstanceState.getParcelable(UI_STATE)!!
    }

    override fun onPause() {
        clearTransientBars()
        super.onPause()
    }

    @SuppressLint("WrongViewCast")
    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)

        fab = findViewById(R.id.fab)
        fragmentContainer = findViewById(R.id.main_fragment_container)
        coordinatorLayout = findViewById(R.id.coordinator)
        constraintLayout = findViewById(R.id.content_view)
        bottomInsetView = findViewById(R.id.bottom_inset)
        topInsetView = findViewById(R.id.top_inset)
        toolbar = findViewById(R.id.toolbar)
        padding = findViewById(R.id.padding)
        toolbarHider = ViewHider.of(toolbar).setDuration(HIDER_DURATION.toLong()).setDirection(TOP).build()
        fabHider = ViewHider.of(fab).setDuration(HIDER_DURATION.toLong()).setDirection(BOTTOM).build()
        fabInteractor = FabInteractor(fab)

        fab.backgroundTintList = ColorStateList.valueOf(resolveThemeColor(R.attr.colorSecondary))

        padding.setOnTouchListener { _, event ->
            if (event.action == ACTION_UP) setKeyboardPadding(bottomInset)
            true
        }

        toolbar.setOnMenuItemClickListener { item ->
            val fragment = currentFragment
            val selected = fragment != null && fragment.onOptionsItemSelected(item)
            selected || onOptionsItemSelected(item)
        }

        val decorView = decorView
        decorView.systemUiVisibility = DEFAULT_SYSTEM_UI_FLAGS
        decorView.setOnSystemUiVisibilityChangeListener { toggleToolbar(!decorView.isDisplayingSystemUI()) }
        setOnApplyWindowInsetsListener(constraintLayout) { _, insets -> consumeSystemInsets(insets) }
        showSystemUI()
    }

    override fun onStart() {
        super.onStart()
        updateUI(true, uiState)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(UI_STATE, uiState)
        super.onSaveInstanceState(outState)
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
        toolbar.postDelayed({
            val fragment = currentFragment
            fragment?.onPrepareOptionsMenu(toolbar.menu)
        }, TOOLBAR_ANIM_DELAY.toLong())
    }

    override fun getCurrentFragment(): TeammatesBaseFragment? =
            super.getCurrentFragment() as? TeammatesBaseFragment

    override fun update(state: UiState) = updateUI(false, state)

    override fun updateMainToolBar(menu: Int, title: CharSequence) =
            toolbar.updateToolBar(menu, title)

    override fun updateAltToolbar(menu: Int, title: CharSequence) {}

    override fun toggleToolbar(show: Boolean) =
            if (show) toolbarHider.show()
            else toolbarHider.hide()

    override fun toggleAltToolbar(show: Boolean) {}

    override fun toggleBottombar(show: Boolean) {}

    override fun toggleFab(show: Boolean) {
        if (show) fabHider.show()
        else fabHider.hide()
    }

    @SuppressLint("Range", "WrongConstant")
    override fun toggleProgress(show: Boolean) {
        val bar = loadingBar
        if (show && bar != null && bar.isShown) return
        if (show) LoadingBar.make(coordinatorLayout, LENGTH_INDEFINITE).apply { loadingBar = this }.show()
        else if (bar != null && bar.isShownOrQueued) bar.dismiss()
    }

    override fun toggleSystemUI(show: Boolean) {
        if (show) showSystemUI()
        else hideSystemUI()
    }

    override fun toggleLightNavBar(isLight: Boolean) {
        if (SDK_INT < O) return

        val decorView = decorView
        var visibility = decorView.systemUiVisibility

        visibility = if (isLight) visibility or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        else visibility and SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()

        decorView.systemUiVisibility = visibility
    }

    override fun setFabIcon(@DrawableRes icon: Int, @StringRes textRes: Int) {
        runOnUiThread { if (icon != 0 && textRes != 0) fabInteractor.update(icon, textRes) }
    }

    override fun setNavBarColor(color: Int) {
        window.navigationBarColor = color
    }

    override fun setFabExtended(expanded: Boolean) {
        fabInteractor.isExtended = expanded
    }

    override fun showSnackBar(message: CharSequence) {
        val snackbar = Snackbar.make(coordinatorLayout, message, LENGTH_LONG)

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        setOnApplyWindowInsetsListener(snackbar.view) { _, insets -> insets }
        snackbar.fabDependentShow()
    }

    override fun showSnackBar(consumer: (Snackbar) -> Unit) {
        val snackbar = Snackbar.make(coordinatorLayout, "", LENGTH_INDEFINITE).withCallback(callback)

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        setOnApplyWindowInsetsListener(snackbar.view) { _, insets -> insets }
        consumer.invoke(snackbar)
        snackbar.fabDependentShow()
    }

    override fun showChoices(consumer: (ChoiceBar) -> Unit) {
        val bar = ChoiceBar.make(coordinatorLayout, LENGTH_INDEFINITE).withCallback(callback)
        consumer.invoke(bar)
        bar.fabDependentShow()
    }

    override fun setFabClickListener(clickListener: View.OnClickListener?) =
            fabInteractor.setOnClickListener(clickListener)

    private fun BaseTransientBottomBar<*>.fabDependentShow() = fab.postDelayed(HIDER_DURATION.toLong()) {
        transientBottomBars.add(this)
        @Suppress("UsePropertyAccessSyntax")
        if (fab.isVisible) setAnchorView(fab)
        show()
    }

    fun onDialogDismissed() {
        val fragment = currentFragment
        val showFab = fragment != null && fragment.showsFab

        if (showFab) toggleFab(true)
    }

    protected fun isNotInMainFragmentContainer(view: View): Boolean {
        val parent = view.parent as? View
        return parent == null || parent.id != R.id.main_fragment_container
    }

    protected open fun adjustKeyboardPadding(suggestion: Int): Int = suggestion - bottomInset

    protected fun clearTransientBars() {
        for (bar in transientBottomBars)
            if (bar is ChoiceBar) bar.dismissAsTimeout()
            else bar.dismiss()
        transientBottomBars.clear()
    }

    protected fun initTransition() {
        val transition = AutoTransition()
        transition.duration = 200

        val view = currentFragment
        if (view != null) for (id in view.staticViews) transition.excludeTarget(id, true)
        transition.excludeTarget(RecyclerView::class.java, true)
        transition.excludeTarget(toolbar, true)

        TransitionManager.beginDelayedTransition(toolbar.parent as ViewGroup, transition)
    }

    private fun updateUI(force: Boolean, state: UiState) {
        uiState = uiState.diff(force,
                state,
                this::toggleFab,
                this::toggleToolbar,
                this::toggleAltToolbar,
                this::toggleBottombar,
                this::toggleSystemUI,
                this::toggleLightNavBar,
                this::setNavBarColor,
                { },
                this::setFabIcon,
                this::updateMainToolBar,
                this::updateAltToolbar,
                this::setFabClickListener
        )
    }

    private fun consumeSystemInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        if (insetsApplied) return insets

        topInset = insets.systemWindowInsetTop
        leftInset = insets.systemWindowInsetLeft
        rightInset = insets.systemWindowInsetRight
        bottomInset = insets.systemWindowInsetBottom

        ViewUtil.getLayoutParams(topInsetView).height = topInset
        ViewUtil.getLayoutParams(bottomInsetView).height = bottomInset
        adjustSystemInsets(currentFragment)

        insetsApplied = true
        return insets
    }

    private fun consumeFragmentInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        val keyboardPadding = insets.systemWindowInsetBottom
        setKeyboardPadding(keyboardPadding)

        val view = currentFragment
        view?.onKeyBoardChanged(keyboardPadding != bottomInset)
        return insets
    }

    private fun setKeyboardPadding(padding: Int) {
        var ref = padding
        initTransition()
        ref = adjustKeyboardPadding(ref)
        ref = max(ref, 0)

        fragmentContainer.setPadding(0, 0, 0, ref)
        ViewUtil.getLayoutParams(this.padding).height = if (ref == 0) 1 else ref // 0 breaks animations
    }

    private fun adjustSystemInsets(fragment: Fragment?) {
        if (fragment !is TeammatesBaseFragment) return
        val insetFlags = fragment.insetFlags

        ViewUtil.getLayoutParams(toolbar).topMargin = if (insetFlags.hasTopInset()) 0 else topInset
        bottomInsetView.visibility = if (insetFlags.hasBottomInset()) VISIBLE else GONE
        topInsetView.visibility = if (insetFlags.hasTopInset()) VISIBLE else GONE
        constraintLayout.setPadding(if (insetFlags.hasLeftInset()) leftInset else 0, 0, if (insetFlags.hasRightInset()) rightInset else 0, 0)
    }

    private fun hideSystemUI() {
        val decorView = decorView
        val visibility = (decorView.systemUiVisibility
                or SYSTEM_UI_FLAG_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = visibility
    }

    private fun showSystemUI() {
        val decorView = decorView
        var visibility = decorView.systemUiVisibility
        visibility = visibility and SYSTEM_UI_FLAG_FULLSCREEN.inv()
        visibility = visibility and SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
        visibility = visibility and SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()

        decorView.systemUiVisibility = visibility
    }

    companion object {

        private const val DEFAULT_SYSTEM_UI_FLAGS = (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        const val HIDER_DURATION = 300
        private const val UI_STATE = "APP_UI_STATE"

        var topInset: Int = 0
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : BaseTransientBottomBar<T>> BaseTransientBottomBar<T>.withCallback(callback: BaseTransientBottomBar.BaseCallback<BaseTransientBottomBar<*>>): T =
        addCallback(callback as BaseTransientBottomBar.BaseCallback<T>)
