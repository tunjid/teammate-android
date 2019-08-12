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
import android.text.TextUtils
import androidx.room.Ignore
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.enums.StatAttributes
import com.mainstreetcode.teammate.model.enums.StatType
import com.mainstreetcode.teammate.persistence.entity.StatEntity
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.areNotEmpty
import com.mainstreetcode.teammate.util.asFloatOrZero
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.parseDate
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

/**
 * Event events
 */

class Stat : StatEntity,
        Model<Stat>,
        HeaderedModel<Stat>,
        ListableModel<Stat> {

    constructor(
            id: String,
            created: Date,
            statType: StatType,
            sport: Sport,
            user: User,
            team: Team,
            game: Game,
            attributes: StatAttributes,
            value: Int,
            time: Float
    ) : super(id, created, statType, sport, user, team, game, attributes, value, time)

    private constructor(`in`: Parcel) : super(`in`)

    override val isEmpty: Boolean
        get() = id.isBlank()

    override fun asItems(): List<Item<Stat>> = listOf(
            Item.number(holder.get(0), 0, Item.NUMBER, R.string.stat_time, time::toString, this::setTime, this),
            Item.text(holder.get(1), 1, Item.STAT_TYPE, R.string.stat_type, statType::code, this::setStatType, this)
                    .textTransformer { value -> sport.statTypeFromCode(value.toString()).getName() }
    )

    override val headerItem: Item<Stat>
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, { "" }, Item.IGNORE_SET, this)

    override fun areContentsTheSame(other: Differentiable): Boolean = when (other) {
        !is Stat -> id == other.id
        else -> statType.areContentsTheSame(other.statType) && user.areContentsTheSame(other.user)
                && value == other.value && time == other.time
    }

    override fun hasMajorFields(): Boolean = areNotEmpty(id)

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun update(updated: Stat) {
        this.id = updated.id
        this.created = updated.created
        this.value = updated.value
        this.time = updated.time
        this.statType.update(updated.statType)
        this.sport.update(updated.sport)
        if (updated.user.hasMajorFields()) this.user.update(updated.user)
        if (updated.team.hasMajorFields()) this.team.update(updated.team)
        if (updated.game.hasMajorFields()) this.game.update(updated.game)
    }

    override fun compareTo(other: Stat): Int {
        val timeComparison = -time.compareTo(other.time)
        return if (timeComparison != 0) timeComparison else -created.compareTo(other.created)
    }

    override fun describeContents(): Int = 0

    class GsonAdapter : JsonSerializer<Stat>, JsonDeserializer<Stat> {

        override fun serialize(src: Stat, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val stat = JsonObject()
            val attributes = JsonArray()

            stat.addProperty(STAT_TYPE, src.statType.code)
            stat.addProperty(USER, src.user.id)
            stat.addProperty(TEAM, src.team.id)
            stat.addProperty(GAME, src.game.id)
            stat.addProperty(TIME, src.time)
            stat.addProperty(VALUE, src.value)
            stat.add(ATTRIBUTES, attributes)

            val sportCode = src.sport.code
            if (!TextUtils.isEmpty(sportCode)) stat.addProperty(SPORT_KEY, sportCode)

            for (attribute in src.attributes) attributes.add(attribute.code)

            return stat
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Stat {
            if (json.isJsonPrimitive)
                return Stat(
                        json.asString, Date(), StatType.empty(), Sport.empty(), User.empty(),
                        Team.empty(), Game.empty(Team.empty()), StatAttributes(), 0, 0f)

            val body = json.asJsonObject

            val id = body.asStringOrEmpty(ID_KEY)
            val created = body.asStringOrEmpty(CREATED_KEY)
            val typeCode = body.asStringOrEmpty(STAT_TYPE)
            val sportCode = body.asStringOrEmpty(SPORT_KEY)

            val value = body.asFloatOrZero(VALUE).toInt()
            val time = body.asFloatOrZero(TIME)

            val user = context.deserialize<User>(body.get(USER), User::class.java)
            val team = context.deserialize<Team>(body.get(TEAM), Team::class.java)
            val game = context.deserialize<Game>(body.get(GAME), Game::class.java)

            val sport = Config.sportFromCode(sportCode)
            val statType = sport.statTypeFromCode(typeCode)
            val attributes = StatAttributes()

            val stat = Stat(id, parseDate(created), statType, sport,
                    user, team, game, attributes, value, time)

            if (!body.has(ATTRIBUTES) || !body.get(ATTRIBUTES).isJsonArray) return stat

            val attributeElements = body.get(ATTRIBUTES).asJsonArray

            for (element in attributeElements) {
                if (!element.isJsonPrimitive) continue
                val attribute = statType.fromCode(element.asString)
                if (!attribute.isInvalid) stat.attributes.add(attribute)
            }

            return stat
        }

        companion object {

            private const val ID_KEY = "_id"
            private const val CREATED_KEY = "created"
            private const val STAT_TYPE = "name"
            private const val SPORT_KEY = "sport"
            private const val USER = "user"
            private const val TEAM = "team"
            private const val GAME = "game"
            private const val TIME = "time"
            private const val VALUE = "value"
            private const val ATTRIBUTES = "attributes"
        }
    }

    companion object {

        @Ignore
        private val holder = IdCache.cache(2)

        fun empty(game: Game): Stat {
            val sport = game.sport
            return Stat("", Date(), sport.statTypeFromCode(""), sport, User.empty(),
                    Team.empty(), game, StatAttributes(), 0, 0f)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Stat> = object : Parcelable.Creator<Stat> {
            override fun createFromParcel(`in`: Parcel): Stat = Stat(`in`)

            override fun newArray(size: Int): Array<Stat?> = arrayOfNulls(size)
        }
    }
}
