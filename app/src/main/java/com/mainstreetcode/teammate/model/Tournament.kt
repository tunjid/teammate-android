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
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.enums.TournamentStyle
import com.mainstreetcode.teammate.model.enums.TournamentType
import com.mainstreetcode.teammate.persistence.entity.TournamentEntity
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.areNotEmpty
import com.mainstreetcode.teammate.util.asBooleanOrFalse
import com.mainstreetcode.teammate.util.asFloatOrZero
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.parseDate
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

/**
 * Event events
 */

class Tournament : TournamentEntity,
        TeamHost,
        Model<Tournament>,
        HeaderedModel<Tournament>,
        ListableModel<Tournament> {

    constructor(
            id: String,
            imageUrl: String,
            refPath: String,
            name: CharSequence,
            description: CharSequence,
            created: Date,
            host: Team,
            sport: Sport,
            type: TournamentType,
            style: TournamentStyle,
            winner: Competitor,
            numLegs: Int,
            numRounds: Int,
            currentRound: Int,
            numCompetitors: Int,
            singleFinal: Boolean
    ) : super(id, imageUrl, refPath, name, description, created, host, sport, type, style, winner, numLegs, numRounds, currentRound, numCompetitors, singleFinal)

    private constructor(`in`: Parcel) : super(`in`)

    override val team: Team
        get() = host

    override val isEmpty: Boolean
        get() = id.isBlank()

    override val headerItem: Item<Tournament>
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, Item.nullToEmpty(imageUrl), { this.imageUrl = it }, this)

    override fun asItems(): List<Item<Tournament>> = listOf(
            Item.text(holder.get(0), 0, Item.INPUT, R.string.tournament_name, Item.nullToEmpty(name), this::setName, this),
            Item.text(holder.get(1), 1, Item.DESCRIPTION, R.string.tournament_description, Item.nullToEmpty(description), this::setDescription, this),
            Item.text(holder.get(2), 2, Item.TOURNAMENT_TYPE, R.string.tournament_type, type::code, this::setType, this)
                    .textTransformer { value -> Config.tournamentTypeFromCode(value.toString()).getName() },
            Item.text(holder.get(3), 3, Item.TOURNAMENT_STYLE, R.string.tournament_style, style::code, this::setStyle, this)
                    .textTransformer { value -> Config.tournamentStyleFromCode(value.toString()).getName() },
            Item.number(holder.get(4), 4, Item.NUMBER, R.string.tournament_legs, numLegs::toString, this::setNumLegs, this),
            Item.number(holder.get(5), 5, Item.INFO, R.string.tournament_single_final, { App.getInstance().getString(if (isSingleFinal) R.string.yes else R.string.no) }, this::setSingleFinal, this)
    )

    override fun areContentsTheSame(other: Differentiable): Boolean = when (other) {
        !is Tournament -> id == other.id
        else -> name == other.name
                && description == other.description && currentRound == other.currentRound
                && imageUrl == other.imageUrl
    }

    override fun hasMajorFields(): Boolean = areNotEmpty(id, name)

    override fun getChangePayload(other: Differentiable?): Any? = other

    fun updateHost(team: Team) = host.update(team)

    override fun update(updated: Tournament) {
        this.id = updated.id
        this.name = updated.name
        this.refPath = updated.refPath
        this.description = updated.description
        this.imageUrl = updated.imageUrl
        this.created = updated.created
        this.numLegs = updated.numLegs
        this.numRounds = updated.numRounds
        this.currentRound = updated.currentRound
        this.numCompetitors = updated.numCompetitors
        this.isSingleFinal = updated.isSingleFinal
        this.type.update(updated.type)
        this.style.update(updated.style)
        this.sport.update(updated.sport)
        if (updated.host.hasMajorFields()) this.host.update(updated.host)
        if (this.winner.hasSameType(updated.winner))
            winner.update(updated.winner)
        else
            this.winner = updated.winner
    }

    override fun compareTo(other: Tournament): Int {
        val createdComparison = created.compareTo(other.created)
        return if (createdComparison != 0) createdComparison else id.compareTo(other.id)
    }

    override fun describeContents(): Int = 0

    class GsonAdapter : JsonSerializer<Tournament>, JsonDeserializer<Tournament> {

        override fun serialize(src: Tournament, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val serialized = JsonObject()

            serialized.addProperty(NAME_KEY, src.name.toString())
            serialized.addProperty(DESCRIPTION_KEY, src.description.toString())
            serialized.addProperty(TYPE_KEY, src.type.toString())
            serialized.addProperty(STYLE_KEY, src.style.toString())
            serialized.addProperty(NUM_LEGS, src.numLegs)
            serialized.addProperty(HOST_KEY, src.host.id)
            serialized.addProperty(SINGLE_FINAL, src.isSingleFinal)

            val typeCode = src.type.code
            val styleCode = src.style.code

            if (!TextUtils.isEmpty(typeCode)) serialized.addProperty(TYPE_KEY, typeCode)
            if (!TextUtils.isEmpty(styleCode)) serialized.addProperty(STYLE_KEY, styleCode)

            return serialized
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Tournament {
            if (json.isJsonPrimitive) {
                return Tournament(json.asString, "", "", "", "", Date(), Team.empty(),
                        Sport.empty(), TournamentType.empty(), TournamentStyle.empty(), Competitor.empty(),
                        1, 1, 0, 0, false)
            }

            val body = json.asJsonObject

            val id = body.asStringOrEmpty(ID_KEY)
            val imageUrl = body.asStringOrEmpty(IMAGE_KEY)
            val name = body.asStringOrEmpty(NAME_KEY)
            val description = body.asStringOrEmpty(DESCRIPTION_KEY)

            val refPath = body.asStringOrEmpty(REF_PATH)
            val sportCode = body.asStringOrEmpty(SPORT_KEY)
            val typeCode = body.asStringOrEmpty(TYPE_KEY)
            val styleCode = body.asStringOrEmpty(STYLE_KEY)

            val created = body.asStringOrEmpty(CREATED_KEY)
            val numLegs = body.asFloatOrZero(NUM_LEGS).toInt()
            val numRounds = body.asFloatOrZero(NUM_ROUNDS).toInt()
            val currentRound = body.asFloatOrZero(CURRENT_ROUND).toInt()
            val numCompetitors = body.asFloatOrZero(NUM_COMPETITORS).toInt()
            val singleFinal = body.asBooleanOrFalse(SINGLE_FINAL)

            var host: Team? = context.deserialize<Team>(body.get(HOST_KEY), Team::class.java)
            val sport = Config.sportFromCode(sportCode)
            val type = Config.tournamentTypeFromCode(typeCode)
            val style = Config.tournamentStyleFromCode(styleCode)

            val winnerObject = if (body.has(WINNER) && body.get(WINNER).isJsonObject)
                body.get(WINNER).asJsonObject
            else
                null

            winnerObject?.addProperty("tournament", id)
            val winner = if (winnerObject != null) context.deserialize(winnerObject, Competitor::class.java) else Competitor.empty()

            if (host == null) host = Team.empty()

            return Tournament(id, imageUrl, refPath, name, description, parseDate(created), host,
                    sport, type, style, winner, numLegs, numRounds, currentRound, numCompetitors, singleFinal)
        }

        companion object {

            private const val ID_KEY = "_id"
            private const val IMAGE_KEY = "imageUrl"
            private const val NAME_KEY = "name"
            private const val DESCRIPTION_KEY = "description"
            private const val HOST_KEY = "host"
            private const val CREATED_KEY = "created"
            private const val SPORT_KEY = "sport"
            private const val TYPE_KEY = "type"
            private const val STYLE_KEY = "style"
            private const val REF_PATH = "refPath"
            private const val WINNER = "winner"
            private const val NUM_LEGS = "numLegs"
            private const val NUM_ROUNDS = "numRounds"
            private const val CURRENT_ROUND = "currentRound"
            private const val NUM_COMPETITORS = "numCompetitors"
            private const val SINGLE_FINAL = "singleFinal"
        }
    }

    companion object {

        const val PHOTO_UPLOAD_KEY = "tournament-photo"

        @Ignore
        private val holder = IdCache.cache(6)

        @JvmOverloads
        fun empty(host: Team = Team.empty()): Tournament {
            val date = Date()
            val sport = host.sport
            return Tournament("", Config.getDefaultTournamentLogo(), "", "", "", date, host, sport,
                    sport.defaultTournamentType(), sport.defaultTournamentStyle(), Competitor.empty(),
                    1, 1, 0, 0, false)
        }

        fun withId(id: String): Tournament {
            val empty = empty()
            empty.id = id
            return empty
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Tournament> = object : Parcelable.Creator<Tournament> {
            override fun createFromParcel(`in`: Parcel): Tournament = Tournament(`in`)

            override fun newArray(size: Int): Array<Tournament?> = arrayOfNulls(size)
        }
    }
}
