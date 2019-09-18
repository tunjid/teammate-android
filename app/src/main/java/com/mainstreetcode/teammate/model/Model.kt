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


import android.os.Parcelable
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Base interface for model interactions
 */
interface Model<T> : RemoteImage, Differentiable, Parcelable, Comparable<T> {

    val id : String

    /**
     * @return whether this object was created locally or exists in the remote.
     */
    val isEmpty: Boolean

    override val diffId: String
        get() = id

    /**
     * Update the current model with values in the model provided, while keeping values in
     * data structures like [lists][java.util.List]
     *
     * @param updated the model providing new values
     */
    fun update(updated: T)

    fun hasMajorFields(): Boolean = imageUrl.isNotBlank()
}
