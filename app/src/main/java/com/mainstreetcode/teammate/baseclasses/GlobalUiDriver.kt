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
import android.graphics.Bitmap
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.UiState
import com.mainstreetcode.teammate.util.FabInteractor
import com.mainstreetcode.teammate.util.isDisplayingSystemUI
import com.mainstreetcode.teammate.util.resolveThemeColor
import com.mainstreetcode.teammate.util.springCrossFade
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.material.animator.FabExtensionAnimator
import com.tunjid.androidx.view.animator.ViewHider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * An interface for classes that host a [UiState], usually a [FragmentActivity].
 * Implementations should delegate to an instance of [GlobalUiDriver]
 */
interface GlobalUiController {
    var uiState: UiState
}

/**
 * Convenience method for [FragmentActivity] delegation to a [GlobalUiDriver] when implementing
 * [GlobalUiController]
 */
fun FragmentActivity.globalUiDriver(
        toolbarId: Int = R.id.toolbar,
        altToolbarId: Int = R.id.alt_toolbar,
        fabId: Int = R.id.fab,
        bottomNavId: Int = R.id.bottom_navigation,
        grassId: Int = R.id.grass_background,
        currentSource: () -> Fragment?
) = object : ReadWriteProperty<FragmentActivity, UiState> {

    private val driver by lazy {
        GlobalUiDriver(
                this@globalUiDriver,
                toolbarId,
                altToolbarId,
                fabId,
                bottomNavId,
                grassId,
                currentSource
        )
    }

    override operator fun getValue(thisRef: FragmentActivity, property: KProperty<*>): UiState =
            driver.uiState

    override fun setValue(thisRef: FragmentActivity, property: KProperty<*>, value: UiState) {
        driver.uiState = value
    }
}

/**
 * Convenience method for [Fragment] delegation to a [FragmentActivity] when implementing
 * [GlobalUiController]
 */
fun Fragment.activityGlobalUiController() = object : ReadWriteProperty<Fragment, UiState> {

    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): UiState =
            (activity as? GlobalUiController)?.uiState
                    ?: throw IllegalStateException("This fragment is not hosted by a GlobalUiController")

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: UiState) {
        val host = activity
        check(host is GlobalUiController) { "This fragment is not hosted by a GlobalUiController" }
        host.uiState = value
    }
}

/**
 * Drives global UI that is common from screen to screen described by a [UiState].
 * This makes it so that these persistent UI elements aren't duplicated, and only animate themselves when they change.
 * This is the default implementation of [GlobalUiController] that other implementations of
 * the same interface should delegate to.
 */
class GlobalUiDriver(
        private val host: FragmentActivity,
        toolbarId: Int,
        altToolbarId: Int,
        fabId: Int,
        bottomNavId: Int,
        grassId: Int = R.id.grass_background,
        private val getCurrentFragment: () -> Fragment?
) : GlobalUiController {

    private var hasHiddenSystemUi = false

    init {
        val color = ContextCompat.getColor(host, R.color.transparent)
        host.window.statusBarColor = color
        host.window.navigationBarColor = color
        host.window.decorView.systemUiVisibility = DEFAULT_SYSTEM_UI_FLAGS
        host.window.decorView.setOnSystemUiVisibilityChangeListener { if (hasHiddenSystemUi) uiState = uiState.copy(toolbarShows = !host.window.decorView.isDisplayingSystemUI()) }
    }

    private val altToolbar: Toolbar = host.findViewById<Toolbar>(altToolbarId).apply {
        setOnMenuItemClickListener(this@GlobalUiDriver::onMenuItemClicked)
    }

    private val toolbarHider: ViewHider<Toolbar> = host.findViewById<Toolbar>(toolbarId).run {
        setOnMenuItemClickListener(this@GlobalUiDriver::onMenuItemClicked)
        ViewHider.of(this)
                .setDirection(ViewHider.TOP)
                .addStartAction { altToolbar.visibility = View.INVISIBLE }
                .build()
    }

    private val fabHider: ViewHider<MaterialButton> = host.findViewById<MaterialButton>(fabId).run {
        backgroundTintList = ColorStateList.valueOf(host.resolveThemeColor(R.attr.colorSecondary))
        ViewHider.of(this).setDirection(ViewHider.BOTTOM).build()
    }

    private val bottomNavHider: ViewHider<ImageView> = host.findViewById<View>(bottomNavId).run {
        val bottomNavSnapshot = host.findViewById<ImageView>(R.id.bottom_nav_snapshot)
        doOnLayout { bottomNavSnapshot.layoutParams.height = height }

        ViewHider.of(bottomNavSnapshot)
                .setDirection(ViewHider.BOTTOM)
                .addStartAction {
                    if (getCurrentFragment() == null) return@addStartAction
                    if (isVisible && isLaidOut && width != 0 && height != 0)
                        bottomNavSnapshot.setImageBitmap(drawToBitmap(Bitmap.Config.ARGB_8888))

                    // Invisible so the snapshot can  be seen to animate in
                    visibility = if (uiState.bottomNavShows) View.INVISIBLE else View.GONE
                }
                .addEndAction {
                    // Finally show or hide the actual bottom bar
                    isVisible = uiState.bottomNavShows
                }
                .build()
    }

    private val grassHider: ViewHider<ImageView> = host.findViewById<ImageView>(grassId).run {
        ViewHider.of(this).setDirection(ViewHider.BOTTOM).build()
    }

    private val fabExtensionAnimator: FabInteractor = FabInteractor(fabHider.view).apply { isExtended = true }

    private var state: UiState = UiState.freshState()

    override var uiState: UiState
        get() = state
        @SuppressLint("InlinedApi")
        set(value) {
            val previous = state.copy()
            state = value.copy(toolbarInvalidated = false) // Reset after firing once

            previous.diff(
                    previous.copy(fabClickListener = null) == UiState.freshState(),
                    newState = value,
                    showsFabConsumer = fabHider::set,
                    showsToolbarConsumer = toolbarHider::set,
                    showsAltToolbarConsumer = this::toggleAltToolbar,
                    showsBottomNavConsumer = bottomNavHider::set,
                    grassShowsConsumer = grassHider::set,
                    showsSystemUIConsumer = this::toggleSystemUI,
                    hasLightNavBarConsumer = this::toggleLightNavBar,
                    navBarColorConsumer = this::setNavBarColor,
                    fabStateConsumer = this::setFabState,
                    fabExtendedConsumer = fabExtensionAnimator::isExtended::set,
                    toolbarStateConsumer = this::updateMainToolBar,
                    altToolbarStateConsumer = this::updateAltToolBar,
                    fabClickListenerConsumer = this::setFabClickListener
            )

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val decorView = host.window.decorView
            val visibility = decorView.systemUiVisibility
            val isLight = ColorUtils.calculateLuminance(value.navBarColor) > 0.5
            val systemUiVisibility = if (isLight) visibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else visibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()

            decorView.systemUiVisibility = systemUiVisibility
            host.window.navigationBarColor = value.navBarColor
        }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        val fragment = getCurrentFragment()
        val selected = fragment != null && fragment.onOptionsItemSelected(item)

        return selected || host.onOptionsItemSelected(item)
    }

    private fun toggleAltToolbar(show: Boolean) {
        if (show) toolbarHider.hide()
        else if (uiState.toolbarShows) toolbarHider.show()

        altToolbar.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun setNavBarColor(color: Int) {
        host.window.navigationBarColor = color
    }

    private fun updateMainToolBar(menu: Int, invalidatedAlone: Boolean, title: CharSequence) = toolbarHider.view.run {
        update(menu, invalidatedAlone, title)
        getCurrentFragment()?.onPrepareOptionsMenu(this.menu)
        Unit
    }

    private fun updateAltToolBar(menu: Int, invalidatedAlone: Boolean, title: CharSequence) = altToolbar.run {
        update(menu, invalidatedAlone, title)
        getCurrentFragment()?.onPrepareOptionsMenu(this.menu)
        Unit
    }

    private fun setFabState(@DrawableRes icon: Int, @StringRes title: Int) = host.runOnUiThread {
        val titleSequence = if (title == 0) "" else host.getString(title)
        if (icon != 0 && titleSequence.isNotBlank())
            fabExtensionAnimator.updateGlyphs(FabExtensionAnimator.SimpleGlyphState(titleSequence, host.drawableAt(icon)!!))
    }

    private fun setFabClickListener(onClickListener: View.OnClickListener?) =
            fabHider.view.setOnClickListener(onClickListener)

    private fun toggleSystemUI(show: Boolean) {
        if (!show) hasHiddenSystemUi = true
        if (show) showSystemUI()
        else hideSystemUI()
    }

    private fun hideSystemUI() {
        val decorView = host.window.decorView
        val visibility = (decorView.systemUiVisibility
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        decorView.systemUiVisibility = visibility
    }

    private fun showSystemUI() {
        val decorView = host.window.decorView
        var visibility = decorView.systemUiVisibility
        visibility = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
        visibility = visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
        visibility = visibility and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()

        decorView.systemUiVisibility = visibility
    }

    private fun toggleLightNavBar(isLight: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val decorView = host.window.decorView
        var visibility = decorView.systemUiVisibility

        visibility = if (isLight) visibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        else visibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()

        decorView.systemUiVisibility = visibility
    }

    private fun Toolbar.update(@MenuRes menu: Int, invalidatedAlone: Boolean, title: CharSequence) = when {
        invalidatedAlone -> refreshMenu()
        visibility != View.VISIBLE || this.title == null || this.title.isBlank() -> {
            setTitle(title)
            refreshMenu(menu)
        }
        else -> (0 until childCount)
                .map(this::getChildAt)
                .filter { it !is ImageView }
                .forEach {
                    it.springCrossFade {
                        if (it is TextView) setTitle(title)
                        else if (it is ActionMenuView) refreshMenu(menu)
                    }
                }
    }

    private fun Toolbar.refreshMenu(menu: Int? = null) {
        if (menu != null) {
            this.menu.clear()
            if (menu != 0) inflateMenu(menu)
        }
        getCurrentFragment()?.onPrepareOptionsMenu(this.menu)
    }

    companion object {
        private const val TOOLBAR_ANIM_DELAY = 200L

        private const val DEFAULT_SYSTEM_UI_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
    }
}

private fun ViewHider<*>.set(show: Boolean) =
        if (show) show()
        else hide()