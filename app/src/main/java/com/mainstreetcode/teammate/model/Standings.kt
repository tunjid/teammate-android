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
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.deserializeList
import java.lang.reflect.Type

/**
 * Event events
 */

class Standings private constructor(private var id: String?, private var tournamentId: String?) {

    val table = mutableListOf<Row>()
    private val titleRow = Row.empty()

    val columnNames: List<String>
        get() = titleRow.columns

    fun update(other: Standings): Standings {
        this.id = other.id
        this.tournamentId = other.tournamentId
        table.clear()
        table.addAll(other.table)
        titleRow.update(other.titleRow)
        return this
    }

    class GsonAdapter : JsonDeserializer<Standings> {

        @SuppressLint("CheckResult")
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Standings {

            val body = json.asJsonObject

            val id = body.asStringOrEmpty(ID)
            val tournament = body.asStringOrEmpty(TOURNAMENT)
            val standings = Standings(id, tournament)

            val table = body.get(TABLE).asJsonArray
            if (table.size() == 0) return standings

            context.deserializeList(table, standings.table, Row::class.java)
            val columnObject = table.get(0).asJsonObject.get(COLUMNS).asJsonObject

            columnObject.entrySet()
                    .filter { entry -> entry.key != "competitor" }
                    .map(MutableMap.MutableEntry<String, JsonElement>::key)
                    .map(String::toString)
                    .forEach(standings.titleRow::add)

            return standings
        }

        companion object {

            private const val ID = "_id"
            private const val TOURNAMENT = "tournament"
            private const val TABLE = "table"
            private const val COLUMNS = "columns"
        }
    }

    companion object {

        fun forTournament(tournament: Tournament): Standings = Standings("", tournament.id)
    }
}
