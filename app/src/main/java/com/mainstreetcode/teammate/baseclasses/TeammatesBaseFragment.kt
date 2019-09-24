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

import android.content.Context
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.TransitionSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.UiState
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.FULL_RES_LOAD_DELAY
import com.mainstreetcode.teammate.util.resolveThemeColor
import com.tunjid.androidbootstrap.core.components.Navigator
import com.tunjid.androidbootstrap.core.components.activityNavigationController
import com.tunjid.androidbootstrap.view.util.InsetFlags
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

/**
 * Base Fragment for this app
 */

open class TeammatesBaseFragment(layoutRes: Int = 0) : Fragment(layoutRes),
        InsetProvider,
        GlobalUiController,
        View.OnClickListener,
        TransientBarController,
        Navigator.TagProvider,
        Navigator.TransactionModifier,
        Navigator.NavigationController {

    private var lastSetUiState: AtomicReference<UiState> = AtomicReference()

    private var activityUiState by activityGlobalUiController()

    override var uiState: UiState
        get() = activityUiState
        set(value) {
            activityUiState = value
            lastSetUiState.set(value)
        }

    override val navigator: Navigator by activityNavigationController()

    override val transientBarDriver: TransientBarDriver
        get() = requireActivity().run { (this as TransientBarController).transientBarDriver }

    override val stableTag: String
        get() = javaClass.simpleName

    protected var restoredFromBackStack: Boolean = false

    protected var disposables = CompositeDisposable()

    protected var emptyErrorHandler: (Throwable) -> Unit = ErrorHandler.EMPTY

    protected lateinit var defaultErrorHandler: ErrorHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        defaultErrorHandler = ErrorHandler.builder()
                .defaultMessage(getString(R.string.error_default))
                .add(this::handleErrorMessage)
                .build()
    }

    override fun onResume() {
        super.onResume()
        if (view != null) updateUi(fabShows = showsFab)
    }

    override fun onPause() {
        disposables.clear()
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUi(fabClickListener = this)
    }

    override fun onDestroyView() {
        disposables.clear()
        restoredFromBackStack = true
        if (uiState.fabClickListener === this) updateUi(fabClickListener = null)
        super.onDestroyView()
    }

    override fun onClick(view: View) = Unit

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        )
    }

    protected fun setEnterExitTransitions() {
        if (Config.isStaticVariant) return

        sharedElementEnterTransition = cardTransition()
        sharedElementReturnTransition = cardTransition()
    }

    protected fun removeEnterExitTransitions() {
        enterTransition = Fade()
        exitTransition = Fade()
        sharedElementEnterTransition = null
        sharedElementReturnTransition = null
    }

    protected open fun handleErrorMessage(message: Message) {
        transientBarDriver.showSnackBar(message.message)
        transientBarDriver.toggleProgress(false)
    }

    protected fun updateFabOnScroll(dx: Int, dy: Int) =
            if (showsFab && abs(dy) > 3) uiState = uiState.copy(fabShows = dy < 0) else Unit

    override fun onKeyBoardChanged(appeared: Boolean) = Unit

    open fun togglePersistentUi() {
        lastSetUiState.get()?.apply { uiState = this }
    }

    protected open val altToolbarTitle: CharSequence get() = ""

    override val insetFlags: InsetFlags get() = InsetFlags.ALL

    open val staticViews: IntArray get() = intArrayOf()

    open val showsFab: Boolean get() = false

    protected fun defaultUi(
            @DrawableRes fabIcon: Int = uiState.fabIcon,
            @StringRes fabText: Int = uiState.fabText,
            fabShows: Boolean = false,
            fabExtended: Boolean = uiState.fabExtended,
            @MenuRes toolBarMenu: Int = 0,
            toolbarShows: Boolean = true,
            toolbarInvalidated: Boolean = false,
            toolbarTitle: CharSequence = "",
            @MenuRes altToolBarMenu: Int = 0,
            altToolBarShows: Boolean = false,
            altToolbarInvalidated: Boolean = false,
            altToolbarTitle: CharSequence = "",
            @ColorInt navBarColor: Int = if (SDK_INT >= O) requireActivity().resolveThemeColor(R.attr.nav_bar_color) else Color.BLACK,
            bottomNavShows: Boolean = true,
            systemUiShows: Boolean = true,
            hasLightNavBar: Boolean = SDK_INT >= O,
            fabClickListener: View.OnClickListener? = this
    ) = updateUi(
            fabIcon,
            fabText,
            fabShows,
            fabExtended,
            toolBarMenu,
            toolbarShows,
            toolbarInvalidated,
            toolbarTitle,
            altToolBarMenu,
            altToolBarShows,
            altToolbarInvalidated,
            altToolbarTitle,
            navBarColor,
            bottomNavShows,
            systemUiShows,
            hasLightNavBar,
            fabClickListener
    )

    protected fun updateUi(
            @DrawableRes fabIcon: Int = uiState.fabIcon,
            @StringRes fabText: Int = uiState.fabText,
            fabShows: Boolean = uiState.fabShows,
            fabExtended: Boolean = uiState.fabExtended,
            @MenuRes toolBarMenu: Int = uiState.toolBarMenu,
            toolbarShows: Boolean = uiState.toolbarShows,
            toolbarInvalidated: Boolean = uiState.toolbarInvalidated,
            toolbarTitle: CharSequence = uiState.toolbarTitle,
            @MenuRes altToolBarMenu: Int = uiState.altToolBarMenu,
            altToolBarShows: Boolean = uiState.altToolBarShows,
            altToolbarInvalidated: Boolean = uiState.altToolbarInvalidated,
            altToolbarTitle: CharSequence = uiState.altToolbarTitle,
            @ColorInt navBarColor: Int = uiState.navBarColor,
            bottomNavShows: Boolean = uiState.bottomNavShows,
            systemUiShows: Boolean = uiState.systemUiShows,
            hasLightNavBar: Boolean = uiState.hasLightNavBar,
            fabClickListener: View.OnClickListener? = uiState.fabClickListener
    ) {
        if (navigator.currentFragment !== this) return

        uiState = uiState.copy(
                fabIcon = fabIcon,
                fabText = fabText,
                fabShows = fabShows,
                fabExtended = fabExtended,
                toolBarMenu = toolBarMenu,
                toolbarShows = toolbarShows,
                toolbarInvalidated = toolbarInvalidated,
                toolbarTitle = toolbarTitle,
                altToolBarMenu = altToolBarMenu,
                altToolBarShows = altToolBarShows,
                altToolbarInvalidated = altToolbarInvalidated,
                altToolbarTitle = altToolbarTitle,
                navBarColor = navBarColor,
                bottomNavShows = bottomNavShows,
                systemUiShows = systemUiShows,
                hasLightNavBar = hasLightNavBar,
                fabClickListener = fabClickListener
        )
    }

    protected fun hideKeyboard() {
        val root = view ?: return

        val imm = root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(root.windowToken, 0)
    }

    protected fun sharedFadeTransition() = Fade().apply { duration = FULL_RES_LOAD_DELAY.toLong() }

    private fun cardTransition(): TransitionSet = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeImageTransform())
            .setOrdering(TransitionSet.ORDERING_TOGETHER)
            .apply { startDelay = 25; duration = FULL_RES_LOAD_DELAY.toLong() }

    companion object {
        const val GRASS_COLOR = -0x6c44af

        val NO_TOP: InsetFlags = InsetFlags.NO_TOP
        val NONE: InsetFlags = InsetFlags.NONE
    }

}
