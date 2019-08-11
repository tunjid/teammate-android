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

import android.text.TextUtils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import com.mainstreetcode.teammate.util.ModelUtils

import java.lang.reflect.Type
import java.util.Objects

open class MetaData internal constructor(
        code: String,
        internal var name: String
) : Differentiable {

    var code: String
        internal set

    val isInvalid: Boolean
        get() = TextUtils.isEmpty(code)

    init {
        this.code = code
    }

    override fun getId(): String = code

    open fun getName(): CharSequence = name

    fun update(updated: MetaData) {
        this.code = updated.code
        this.name = updated.name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MetaData) return false
        val metaData = other as MetaData?
        return code == metaData!!.code
    }

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is MetaData) id == other.id else this.code == other.code && this.name == other.name

    override fun hashCode(): Int = Objects.hash(code)

    abstract class GsonAdapter<T : MetaData> : JsonSerializer<T>, JsonDeserializer<T> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): T {
            val baseEnumJson = json.asJsonObject

            val code = ModelUtils.asString(CODE_KEY, baseEnumJson)
            val name = ModelUtils.asString(NAME_KEY, baseEnumJson)

            return fromJson(code, name, baseEnumJson, context)
        }

        override fun serialize(src: T, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val serialized = JsonObject()

            serialized.addProperty(CODE_KEY, src.code)
            serialized.addProperty(NAME_KEY, src.name)

            return toJson(serialized, src, context)
        }

        internal abstract fun fromJson(code: String, name: String, body: JsonObject, context: JsonDeserializationContext): T

        internal open fun toJson(serialized: JsonObject, src: T, context: JsonSerializationContext): JsonObject =
                serialized

        companion object {

            private const val CODE_KEY = "code"
            private const val NAME_KEY = "name"
        }
    }
}


