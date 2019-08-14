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

package com.mainstreetcode.teammate.util.nav

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes

class NavItem : Parcelable {
    @IdRes
    internal val idRes: Int
    @StringRes
    internal val titleRes: Int
    @DrawableRes
    internal val drawableRes: Int

    private constructor(idRes: Int, titleRes: Int, drawableRes: Int) {
        this.idRes = idRes
        this.titleRes = titleRes
        this.drawableRes = drawableRes
    }

    private constructor(`in`: Parcel) {
        idRes = `in`.readInt()
        titleRes = `in`.readInt()
        drawableRes = `in`.readInt()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(idRes)
        dest.writeInt(titleRes)
        dest.writeInt(drawableRes)
    }

    companion object {

        fun create(@IdRes idRes: Int, @StringRes titleRes: Int, @DrawableRes drawableRes: Int): NavItem =
                NavItem(idRes, titleRes, drawableRes)

        @JvmField
        val CREATOR: Parcelable.Creator<NavItem> = object : Parcelable.Creator<NavItem> {
            override fun createFromParcel(`in`: Parcel): NavItem = NavItem(`in`)

            override fun newArray(size: Int): Array<NavItem?> = arrayOfNulls(size)
        }
    }
}
