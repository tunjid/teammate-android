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

package com.mainstreetcode.teammate.model

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import com.tunjid.androidbootstrap.view.util.InsetFlags

class UiState : Parcelable {

    @DrawableRes
    private val fabIcon: Int
    @StringRes
    private val fabText: Int
    @MenuRes
    private val toolBarMenu: Int
    @MenuRes
    private val altToolBarMenu: Int
    @ColorInt
    private val navBarColor: Int

    private val showsFab: Boolean
    private val showsToolbar: Boolean
    private val showsAltToolbar: Boolean
    private val showsBottomNav: Boolean
    private val showsSystemUI: Boolean
    private val hasLightNavBar: Boolean

    private val insetFlags: InsetFlags
    private val toolbarTitle: CharSequence
    private val altToolbarTitle: CharSequence
    private val fabClickListener: View.OnClickListener?

    constructor(fabIcon: Int,
                fabText: Int,
                toolBarMenu: Int,
                altToolBarMenu: Int,
                navBarColor: Int,
                showsFab: Boolean,
                showsToolbar: Boolean,
                showsAltToolbar: Boolean,
                showsBottomNav: Boolean,
                showsSystemUI: Boolean,
                hasLightNavBar: Boolean,
                insetFlags: InsetFlags,
                toolbarTitle: CharSequence,
                altToolbarTitle: CharSequence,
                fabClickListener: View.OnClickListener?) {
        this.fabIcon = fabIcon
        this.fabText = fabText
        this.toolBarMenu = toolBarMenu
        this.altToolBarMenu = altToolBarMenu
        this.navBarColor = navBarColor
        this.showsFab = showsFab
        this.showsToolbar = showsToolbar
        this.showsAltToolbar = showsAltToolbar
        this.showsBottomNav = showsBottomNav
        this.showsSystemUI = showsSystemUI
        this.hasLightNavBar = hasLightNavBar
        this.insetFlags = insetFlags
        this.toolbarTitle = toolbarTitle
        this.altToolbarTitle = altToolbarTitle
        this.fabClickListener = fabClickListener
    }

    fun diff(force: Boolean, newState: UiState,
             showsFabConsumer: (Boolean) -> Unit,
             showsToolbarConsumer: (Boolean) -> Unit,
             showsAltToolbarConsumer: (Boolean) -> Unit,
             showsBottomNavConsumer: (Boolean) -> Unit,
             showsSystemUIConsumer: (Boolean) -> Unit,
             hasLightNavBarConsumer: (Boolean) -> Unit,
             navBarColorConsumer: (Int) -> Unit,
             insetFlagsConsumer: (InsetFlags) -> Unit,
             fabStateConsumer: (Int, Int) -> Unit,
             toolbarStateConsumer: (Int, CharSequence) -> Unit,
             altToolbarStateConsumer: (Int, CharSequence) -> Unit,
             fabClickListenerConsumer: (View.OnClickListener?) -> Unit
    ): UiState {
        only(force, newState, { state -> state.showsFab }, showsFabConsumer)
        only(force, newState, { state -> state.showsToolbar }, showsToolbarConsumer)
        only(force, newState, { state -> state.showsAltToolbar }, showsAltToolbarConsumer)
        only(force, newState, { state -> state.showsBottomNav }, showsBottomNavConsumer)
        only(force, newState, { state -> state.showsSystemUI }, showsSystemUIConsumer)
        only(force, newState, { state -> state.hasLightNavBar }, hasLightNavBarConsumer)
        only(force, newState, { state -> state.navBarColor }, navBarColorConsumer)
        only(force, newState, { state -> state.insetFlags }, insetFlagsConsumer)

        either(force, newState, { state -> state.fabIcon }, { state -> state.fabText }, fabStateConsumer)
        either(force, newState, { state -> state.toolBarMenu }, { state -> state.toolbarTitle }, toolbarStateConsumer)
        either(force, newState, { state -> state.altToolBarMenu }, { state -> state.altToolbarTitle }, altToolbarStateConsumer)

        fabClickListenerConsumer.invoke(newState.fabClickListener)

        return newState
    }

    private fun <T> only(force: Boolean, that: UiState, first: (UiState) -> T, consumer: (T) -> Unit) {
        val thisFirst = first.invoke(this)
        val thatFirst = first.invoke(that)

        if (force || thisFirst != thatFirst) consumer.invoke(thatFirst)
    }

    private fun <S, T> either(force: Boolean,
                              that: UiState,
                              first: (UiState) -> S,
                              second: (UiState) -> T,
                              biConsumer: (S, T) -> Unit) {
        val thisFirst = first.invoke(this)
        val thatFirst = first.invoke(that)
        val thisSecond = second.invoke(this)
        val thatSecond = second.invoke(that)

        if (force || thisFirst != thatFirst || thisSecond != thatSecond)
            biConsumer.invoke(thatFirst, thatSecond)
    }

    private constructor(`in`: Parcel) {
        fabIcon = `in`.readInt()
        fabText = `in`.readInt()
        toolBarMenu = `in`.readInt()
        altToolBarMenu = `in`.readInt()
        navBarColor = `in`.readInt()
        showsFab = `in`.readByte().toInt() != 0x00
        showsToolbar = `in`.readByte().toInt() != 0x00
        showsAltToolbar = `in`.readByte().toInt() != 0x00
        showsBottomNav = `in`.readByte().toInt() != 0x00
        showsSystemUI = `in`.readByte().toInt() != 0x00
        hasLightNavBar = `in`.readByte().toInt() != 0x00

        val hasLeftInset = `in`.readByte().toInt() != 0x00
        val hasTopInset = `in`.readByte().toInt() != 0x00
        val hasRightInset = `in`.readByte().toInt() != 0x00
        val hasBottomInset = `in`.readByte().toInt() != 0x00
        insetFlags = InsetFlags.create(hasLeftInset, hasTopInset, hasRightInset, hasBottomInset)

        toolbarTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(`in`)
        altToolbarTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(`in`)

        fabClickListener = null
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(fabIcon)
        dest.writeInt(fabText)
        dest.writeInt(toolBarMenu)
        dest.writeInt(altToolBarMenu)
        dest.writeInt(navBarColor)
        dest.writeByte((if (showsFab) 0x01 else 0x00).toByte())
        dest.writeByte((if (showsToolbar) 0x01 else 0x00).toByte())
        dest.writeByte((if (showsAltToolbar) 0x01 else 0x00).toByte())
        dest.writeByte((if (showsBottomNav) 0x01 else 0x00).toByte())
        dest.writeByte((if (showsSystemUI) 0x01 else 0x00).toByte())
        dest.writeByte((if (hasLightNavBar) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasLeftInset()) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasTopInset()) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasRightInset()) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasBottomInset()) 0x01 else 0x00).toByte())

        TextUtils.writeToParcel(toolbarTitle, dest, 0)
        TextUtils.writeToParcel(altToolbarTitle, dest, 0)
    }

    companion object {
        fun freshState(): UiState {
            return UiState(
                    0,
                    0,
                    0,
                    0,
                    Color.BLACK,
                    showsFab = true,
                    showsToolbar = true,
                    showsAltToolbar = false,
                    showsBottomNav = true,
                    showsSystemUI = true,
                    hasLightNavBar = false,
                    insetFlags = InsetFlags.ALL,
                    toolbarTitle = "",
                    altToolbarTitle = "", fabClickListener = null
            )
        }

        @JvmField
        val CREATOR: Parcelable.Creator<UiState> = object : Parcelable.Creator<UiState> {
            override fun createFromParcel(`in`: Parcel): UiState = UiState(`in`)

            override fun newArray(size: Int): Array<UiState?> = arrayOfNulls(size)
        }
    }
}
