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
import com.mainstreetcode.teammate.model.enums.StatType
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.ModelUtils
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

class StatAggregate {

    class Request private constructor(
            private val user: User,
            private val team: Team,
            sport: Sport
    ) {
        var sport: Sport
            private set

        val items: List<Differentiable>

        init {
            this.sport = sport
            items = buildItems()
        }

        fun updateUser(user: User) {
            this.user.update(user)
        }

        fun updateTeam(team: Team) {
            this.team.update(team)
        }

        fun setSport(sport: String) {
            this.sport = Config.sportFromCode(sport)
        }

        private fun buildItems(): List<Differentiable> = listOf<Differentiable>(
                Item.text(holder.get(0), 0, Item.SPORT, R.string.team_sport, sport::getName, this::setSport, this)
                        .textTransformer { value -> Config.sportFromCode(value.toString()).getName() },
                user,
                team
        )

        class GsonAdapter : JsonSerializer<Request> {

            override fun serialize(src: Request, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                val serialized = JsonObject()

                if (!src.user.isEmpty) serialized.addProperty(USER, src.user.id)
                if (!src.team.isEmpty) serialized.addProperty(TEAM, src.team.id)

                if (!src.sport.isInvalid) serialized.addProperty(SPORT_KEY, src.sport.code)

                return serialized
            }

            companion object {

                private const val SPORT_KEY = "sport"
                private const val USER = "user"
                private const val TEAM = "team"
            }

        }

        companion object {
            @Ignore
            private val holder = IdCache.cache(1)

            fun empty(): Request =
                    Request(User.empty(), Team.empty(), Config.sportFromCode(""))
        }
    }

    class Result {

        private val aggregates = ArrayList<Aggregate>()

        fun getAggregates(): List<Aggregate> = aggregates

        class GsonAdapter : JsonDeserializer<Result> {

            @Throws(JsonParseException::class)
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Result {

                val jsonArray = json.asJsonArray
                val result = Result()

                for (element in jsonArray) {
                    val `object` = element.asJsonObject
                    val count = ModelUtils.asFloat(COUNT, `object`).toInt()
                    val type = Config.statTypeFromCode(ModelUtils.asString(ID, `object`))

                    result.aggregates.add(Aggregate(count, type))
                }

                return result
            }

            companion object {

                private const val ID = "_id"
                private const val COUNT = "count"
            }
        }
    }

    class Aggregate internal constructor(
            private var countValue: Int,
            private var statType: StatType
    ) : Differentiable {

        val count: String
            get() = countValue.toString()

        val type: CharSequence
            get() = statType.emojiAndName

        override fun getId(): String = statType.id
    }
}
