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


import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar
import com.mainstreetcode.teammate.model.UiState

interface PersistentUiController {

    fun update(state: UiState)

    fun toggleToolbar(show: Boolean)

    fun toggleAltToolbar(show: Boolean)

    fun toggleBottombar(show: Boolean)

    fun toggleFab(show: Boolean)

    fun toggleProgress(show: Boolean)

    fun toggleSystemUI(show: Boolean)

    fun toggleLightNavBar(isLight: Boolean)

    fun setNavBarColor(@ColorInt color: Int)

    fun setFabIcon(@DrawableRes icon: Int, @StringRes textRes: Int)

    fun updateMainToolBar(@MenuRes menu: Int, title: CharSequence)

    fun updateAltToolbar(@MenuRes menu: Int, title: CharSequence)

    fun setFabExtended(expanded: Boolean)

    fun showSnackBar(message: CharSequence)

    fun showSnackBar(consumer: (Snackbar) -> Unit)

    fun showChoices(consumer: (ChoiceBar) -> Unit)

    fun setFabClickListener(clickListener: View.OnClickListener?)

    companion object {

        val DUMMY: PersistentUiController = object : PersistentUiController {

            override fun update(state: UiState) = Unit

            override fun toggleToolbar(show: Boolean) = Unit

            override fun toggleAltToolbar(show: Boolean) = Unit

            override fun toggleBottombar(show: Boolean) = Unit

            override fun toggleFab(show: Boolean) = Unit

            override fun toggleProgress(show: Boolean) = Unit

            override fun toggleSystemUI(show: Boolean) = Unit

            override fun toggleLightNavBar(isLight: Boolean) = Unit

            override fun setNavBarColor(color: Int) = Unit

            override fun setFabIcon(icon: Int, textRes: Int) = Unit

            override fun setFabExtended(expanded: Boolean) = Unit

            override fun updateMainToolBar(menu: Int, title: CharSequence) = Unit

            override fun updateAltToolbar(menu: Int, title: CharSequence) = Unit

            override fun showSnackBar(message: CharSequence) = Unit

            override fun showSnackBar(consumer: (Snackbar) -> Unit) = Unit

            override fun showChoices(consumer: (ChoiceBar) -> Unit) = Unit

            override fun setFabClickListener(clickListener: View.OnClickListener?) = Unit
        }
    }
}
