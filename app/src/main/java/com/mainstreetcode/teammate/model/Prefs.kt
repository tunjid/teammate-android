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


import android.annotation.SuppressLint
import android.os.Parcel

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer

import java.lang.reflect.Type

@SuppressLint("ParcelCreator")
class Prefs private constructor() : Model<Prefs> {

    var isOnBoarded: Boolean = false

    override val isEmpty: Boolean
        get() = false

    override val imageUrl: String
        get() = ""

    override fun update(updated: Prefs) {
        this.isOnBoarded = updated.isOnBoarded
    }

    override fun compareTo(other: Prefs): Int = 0

    override fun getId(): String = "1"

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    class GsonAdapter : JsonSerializer<Prefs> {

        override fun serialize(src: Prefs, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val serialized = JsonObject()

            serialized.addProperty(ON_BOARDED, src.isOnBoarded)

            return serialized
        }

        companion object {

            private const val ON_BOARDED = "onBoarded"
        }
    }

    companion object {

        fun empty(): Prefs = Prefs()
    }
}
