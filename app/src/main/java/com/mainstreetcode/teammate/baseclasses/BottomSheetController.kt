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


import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.MenuRes

import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment

interface BottomSheetController {

    val isBottomSheetShowing: Boolean

    fun hideBottomSheet()

    fun showBottomSheet(args: Args)

    class ToolbarState : Parcelable {
        @MenuRes
        val menuRes: Int
        val title: String?

        internal constructor(menuRes: Int, title: String) {
            this.menuRes = menuRes
            this.title = title
        }

        private constructor(`in`: Parcel) {
            menuRes = `in`.readInt()
            title = `in`.readString()
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(menuRes)
            dest.writeString(title)
        }

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<ToolbarState> = object : Parcelable.Creator<ToolbarState> {
                override fun createFromParcel(`in`: Parcel): ToolbarState = ToolbarState(`in`)

                override fun newArray(size: Int): Array<ToolbarState?> = arrayOfNulls(size)
            }
        }
    }

    class Args internal constructor(menuRes: Int, title: String, val fragment: BaseFragment) {
        val toolbarState: ToolbarState = ToolbarState(menuRes, title)

        companion object {
            fun builder(): Builder = Builder()
        }
    }

    class Builder {
        private var menuRes: Int = 0
        private var title: String = ""
        private var fragment: BaseFragment? = null

        fun setMenuRes(menuRes: Int): Builder {
            this.menuRes = menuRes
            return this
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setFragment(fragment: BaseFragment): Builder {
            this.fragment = fragment
            return this
        }

        fun build(): Args = Args(menuRes, title, fragment ?: throw IllegalArgumentException("Fragment cannot be null"))
    }
}
