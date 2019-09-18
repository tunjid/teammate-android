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

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.deserializeList
import com.mainstreetcode.teammate.util.processEmoji
import com.mainstreetcode.teammate.util.replace
import com.tunjid.androidbootstrap.core.text.SpanBuilder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.util.*

class StatType private constructor(
        code: String,
        name: String,
        emoji: CharSequence,
        private var sportCode: String
) : MetaData(code, name) {

    val attributes: StatAttributes = StatAttributes()

    var emoji: CharSequence = emoji
        get() = field.processEmoji()

    val emojiAndName: CharSequence
        get() = SpanBuilder.of(emoji).append("   ").append(name).build()

    fun fromCode(code: String): StatAttribute {
        for (attr in attributes) if (attr.code == code) return attr
        return StatAttribute.empty()
    }

    override fun toString(): String = name

    fun update(updated: StatType) {
        super.update(updated)
        this.emoji = updated.emoji
        this.sportCode = updated.sportCode
        attributes.replace(updated.attributes)
    }

    override fun areContentsTheSame(other: Differentiable): Boolean {
        val result = super.areContentsTheSame(other)
        return if (other !is StatType) result else result && sportCode == other.sportCode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StatType) return false
        val variant = other as StatType?
        return code == variant!!.code && name == variant.name
    }

    override fun hashCode(): Int = Objects.hash(code, name)

    class GsonAdapter : MetaData.GsonAdapter<StatType>() {

        override fun fromJson(code: String, name: String, body: JsonObject, context: JsonDeserializationContext): StatType {
            val emoji = body.asStringOrEmpty(EMOJI_KEY)
            val sportCode = body.asStringOrEmpty(SPORT)

            val type = StatType(code, name, emoji, sportCode)
            context.deserializeList(body.get(ATTRIBUTES), type.attributes, StatAttribute::class.java)
            return type
        }

        override fun toJson(serialized: JsonObject, src: StatType, context: JsonSerializationContext): JsonObject {
            serialized.addProperty(EMOJI_KEY, src.emoji.toString())
            serialized.addProperty(SPORT, src.sportCode)
            serialized.add(ATTRIBUTES, context.serialize(src.attributes))

            return serialized
        }

        companion object {

            private const val EMOJI_KEY = "emoji"
            private const val SPORT = "sport"
            private const val ATTRIBUTES = "attributes"
        }
    }

    companion object {

        fun empty(): StatType = StatType("", "", "", "")
    }
}
