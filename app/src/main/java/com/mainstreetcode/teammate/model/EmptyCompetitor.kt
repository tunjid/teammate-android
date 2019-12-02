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

import android.os.Parcel
import android.os.Parcelable

class EmptyCompetitor : Competitive {

    override val refType: String
        get() = ""

    override val imageUrl: String
        get() = Config.getDefaultTeamLogo()

    override val name: CharSequence
        get() = ""

    override val isEmpty: Boolean
        get() = true

    constructor()

    private constructor(`in`: Parcel) {
        `in`.readString()
    }

    override val id: String
        get() = ""

    override fun makeCopy(): Competitive = EmptyCompetitor()

    override fun hasMajorFields(): Boolean = false

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString("")
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<EmptyCompetitor> = object : Parcelable.Creator<EmptyCompetitor> {
            override fun createFromParcel(`in`: Parcel): EmptyCompetitor = EmptyCompetitor(`in`)

            override fun newArray(size: Int): Array<EmptyCompetitor?> = arrayOfNulls(size)
        }
    }
}
