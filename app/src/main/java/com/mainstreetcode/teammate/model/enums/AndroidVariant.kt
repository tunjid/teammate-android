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

package com.mainstreetcode.teammate.model.enums

import android.os.Build

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject

import java.util.Objects

class AndroidVariant internal constructor(code: String, name: String) : MetaData(code, name) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is AndroidVariant) return false
        val variant = o as AndroidVariant?
        return code == variant!!.code && name == variant.name
    }

    override fun hashCode(): Int = Objects.hash(code, name)

    class GsonAdapter : MetaData.GsonAdapter<AndroidVariant>() {
        override fun fromJson(code: String, name: String, body: JsonObject, context: JsonDeserializationContext): AndroidVariant =
                AndroidVariant(code, name)
    }

    companion object {

        fun empty(): AndroidVariant = AndroidVariant(Build.VERSION.SDK_INT.toString(), Build.MODEL)
    }
}
