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
import android.content.Context
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
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
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.UiState
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.FULL_RES_LOAD_DELAY
import com.mainstreetcode.teammate.util.resolveThemeColor
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.view.util.InsetFlags
import io.reactivex.disposables.CompositeDisposable
import kotlin.math.abs

/**
 * Base Fragment for this app
 */

open class TeammatesBaseFragment : BaseFragment(), View.OnClickListener {

    protected var disposables = CompositeDisposable()
    protected var emptyErrorHandler: (Throwable) -> Unit = ErrorHandler.EMPTY
    protected lateinit var defaultErrorHandler: ErrorHandler

    protected open val fabStringResource: Int @StringRes get() = R.string.empty_string

    protected open val fabIconResource: Int @DrawableRes get() = R.drawable.ic_add_white_24dp

    protected open val toolbarMenu: Int @MenuRes get() = 0

    protected open val altToolbarMenu: Int @MenuRes get() = 0

    protected open val navBarColor: Int @ColorInt get() = if (SDK_INT >= O) requireActivity().resolveThemeColor(R.attr.nav_bar_color) else Color.BLACK

    protected open val toolbarTitle: CharSequence get() = ""

    protected open val altToolbarTitle: CharSequence get() = ""

    open val insetFlags: InsetFlags get() = InsetFlags.ALL

    open val staticViews: IntArray get() = intArrayOf()

    open val showsFab: Boolean get() = false

    open val showsToolBar: Boolean get() = true

    open val showsAltToolBar: Boolean get() = false

    open val showsBottomNav: Boolean get() = true

    protected open val showsSystemUI: Boolean get() = true

    protected open val hasLightNavBar: Boolean get() = SDK_INT >= O

    protected val persistentUiController: PersistentUiController
        get() {
            val ref = activity
            return if (ref == null) PersistentUiController.DUMMY else ref as PersistentUiController
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        defaultErrorHandler = ErrorHandler.builder()
                .defaultMessage(getString(R.string.error_default))
                .add(this::handleErrorMessage)
                .build()
    }

    override fun onResume() {
        super.onResume()
        if (view != null) togglePersistentUi()
    }

    override fun onPause() {
        disposables.clear()
        super.onPause()
    }

    override fun onDestroyView() {
        disposables.clear()
        persistentUiController.setFabClickListener(null)
        super.onDestroyView()
    }

    override fun onClick(view: View) = Unit

    protected open fun toggleProgress(show: Boolean) = persistentUiController.toggleProgress(show)

    protected fun setFabExtended(extended: Boolean) =
            persistentUiController.setFabExtended(extended)

    protected fun showSnackbar(message: CharSequence) = persistentUiController.showSnackBar(message)

    protected fun showSnackbar(consumer: (Snackbar) -> Unit) =
            persistentUiController.showSnackBar(consumer)

    protected fun showChoices(consumer: (ChoiceBar) -> Unit) =
            persistentUiController.showChoices(consumer)

    @SuppressLint("CommitTransaction")
    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? = beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)

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
        showSnackbar(message.message)
        toggleProgress(false)
    }

    protected fun updateFabOnScroll(dx: Int, dy: Int) =
            if (showsFab && abs(dy) > 3) toggleFab(dy < 0) else Unit

    open fun onKeyBoardChanged(appeared: Boolean) = Unit

    open fun togglePersistentUi() = persistentUiController.update(fromThis())

    @SuppressLint("CommitTransaction")
    protected fun beginTransaction(): FragmentTransaction = fragmentManager!!.beginTransaction()

    protected fun hideKeyboard() {
        val root = view ?: return

        val imm = root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(root.windowToken, 0)
    }

    private fun toggleFab(show: Boolean) = persistentUiController.toggleFab(show)

    private fun fromThis(): UiState = UiState(
            this.fabIconResource,
            this.fabStringResource,
            this.toolbarMenu,
            this.altToolbarMenu,
            this.navBarColor,
            this.showsFab,
            this.showsToolBar,
            this.showsAltToolBar,
            this.showsBottomNav,
            this.showsSystemUI,
            this.hasLightNavBar,
            this.insetFlags,
            this.toolbarTitle,
            this.altToolbarTitle,
            if (view == null) null else this
    )

    protected fun sharedFadeTransition() = Fade().apply { duration = FULL_RES_LOAD_DELAY.toLong() }

    private fun cardTransition(): TransitionSet = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeImageTransform())
            .setOrdering(TransitionSet.ORDERING_TOGETHER)
            .apply { startDelay = 25; duration = FULL_RES_LOAD_DELAY.toLong() }

    companion object {

        val NO_TOP: InsetFlags = InsetFlags.NO_TOP
        val NONE: InsetFlags = InsetFlags.NONE
    }

}
