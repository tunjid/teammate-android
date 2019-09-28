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

import android.text.TextUtils.isEmpty
import androidx.room.Ignore
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.enums.TournamentType
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.asFloatOrZero
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.tunjid.androidx.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

class HeadToHead {

    class Request private constructor(
            private val home: Competitor,
            private val away: Competitor,
            private var type: TournamentType,
            sport: Sport
    ) {

        var sport: Sport
            private set

        val items: List<Differentiable>

        internal val homeId: String
            get() = home.entity.id

        internal val awayId: String
            get() = away.entity.id

        val refPath: String
            get() = type.refPath

        init {
            this.sport = sport
            items = buildItems()
        }

        fun hasInvalidType(): Boolean = type.isInvalid

        private fun setSport(sport: String) {
            this.sport = Config.sportFromCode(sport)
        }

        private fun setType(type: String) {
            this.type = Config.tournamentTypeFromCode(type)
        }

        fun updateHome(entity: Competitive) = update(home, entity)

        fun updateAway(entity: Competitive) = update(away, entity)

        private fun update(competitor: Competitor, entity: Competitive) =
                competitor.updateEntity(entity)

        private fun buildItems(): List<Differentiable> = listOf(
                Item.text(holder[0], 0, Item.TOURNAMENT_TYPE, R.string.tournament_type, type::code, this::setType)
                        .textTransformer { value -> Config.tournamentTypeFromCode(value.toString()).getName() },
                Item.text(holder[1], 1, Item.SPORT, R.string.team_sport, sport::getName, this::setSport)
                        .textTransformer { value -> Config.sportFromCode(value.toString()).getName() },
                home,
                away
        )

        class GsonAdapter : JsonSerializer<Request> {

            override fun serialize(src: Request, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                val serialized = JsonObject()

                serialized.add(HOME, context.serialize(src.home))
                serialized.add(AWAY, context.serialize(src.away))

                if (!src.sport.isInvalid) serialized.addProperty(SPORT_KEY, src.sport.code)

                return serialized
            }

            companion object {

                private const val SPORT_KEY = "sport"
                private const val HOME = "home"
                private const val AWAY = "away"
            }

        }

        companion object {

            @Ignore
            private val holder = IdCache(2)

            fun empty(): Request =
                    Request(Competitor.empty(), Competitor.empty(), Config.tournamentTypeFromCode(""), Config.sportFromCode(""))
        }
    }

    class Result {

        private val aggregates = ArrayList<Aggregate>()

        fun getSummary(request: Request): Summary {
            val homeId = request.homeId
            val awayId = request.awayId
            val summary = Summary()

            for (aggregate in aggregates) when {
                isEmpty(aggregate.id) -> summary.draws = aggregate.count
                aggregate.id == homeId -> summary.wins = aggregate.count
                aggregate.id == awayId -> summary.losses = aggregate.count
            }

            return summary
        }

        class GsonAdapter : JsonDeserializer<Result> {

            @Throws(JsonParseException::class)
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Result {

                val jsonArray = json.asJsonArray
                val result = Result()

                for (element in jsonArray) {
                    val `object` = element.asJsonObject
                    val count = `object`.asFloatOrZero(COUNT).toInt()
                    val id = `object`.get(ID).asJsonObject.asStringOrEmpty(WINNER)

                    result.aggregates.add(Aggregate(count, id))
                }

                return result
            }

            companion object {

                private const val ID = "_id"
                private const val COUNT = "count"
                private const val WINNER = "winner"
            }
        }

    }

    class Summary internal constructor() {
        var wins: Int = 0
            internal set
        var draws: Int = 0
            internal set
        var losses: Int = 0
            internal set
    }

    private class Aggregate internal constructor(internal var count: Int, internal var id: String)
}
