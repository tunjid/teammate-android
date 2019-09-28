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

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.tunjid.androidx.recyclerview.diff.Differentiable
import java.lang.reflect.Type

/**
 * Event events
 */

class Row private constructor(
        private val id: String,
        val competitor: Competitor
) : Differentiable {

    private val tableValues = mutableListOf<String>()

    override val diffId: String
        get() = id

    val imageUrl: String
        get() = competitor.imageUrl

    val name: CharSequence
        get() = competitor.name

    val columns: List<String>
        get() = tableValues

    fun add(column: String) {
        tableValues.add(column)
    }

    fun update(updated: Row) {
        tableValues.clear()
        tableValues.addAll(updated.tableValues)
    }

    class GsonAdapter : JsonDeserializer<Row> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Row {
            val body = json.asJsonObject

            val id = body.asStringOrEmpty(ID_KEY)
            val competitor = context.deserialize<Competitor>(body.get(COMPETITOR), Competitor::class.java)

            val row = Row(id, competitor)

            val columnElement = body.get(COLUMNS)
            if (columnElement == null || !columnElement.isJsonObject) return row

            val columnObject = columnElement.asJsonObject
            for ((_, value) in columnObject.entrySet())
                row.tableValues.add(value.toString())

            return row
        }

        companion object {

            private const val ID_KEY = "_id"
            private const val COMPETITOR = "competitor"
            private const val COLUMNS = "columns"
        }
    }

    companion object {

        fun empty(): Row = Row("", Competitor.empty())
    }
}
