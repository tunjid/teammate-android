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
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.persistence.entity.GameEntity
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.ModelUtils
import com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING
import com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

/**
 * Event events
 */

class Game : GameEntity,
        TeamHost,
        Model<Game>,
        HeaderedModel<Game>,
        ListableModel<Game> {

    constructor(
            id: String,
            name: String,
            refPath: String,
            score: String,
            matchUp: String,
            homeEntityId: String,
            awayEntityId: String,
            winnerEntityId: String,
            created: Date,
            sport: Sport,
            referee: User,
            host: Team,
            event: Event,
            tournament: Tournament,
            home: Competitor,
            away: Competitor,
            winner: Competitor,
            seed: Int,
            leg: Int,
            round: Int,
            homeScore: Int,
            awayScore: Int,
            ended: Boolean,
            canDraw: Boolean
    ) : super(id, name, refPath, score, matchUp, homeEntityId, awayEntityId, winnerEntityId, created, sport, referee, host, event, tournament, home, away, winner, seed, leg, round, homeScore, awayScore, ended, canDraw)

    private constructor(`in`: Parcel) : super(`in`)

    override val team: Team
        get() = host

    override val isEmpty: Boolean
        get() = TextUtils.isEmpty(id)

    override val headerItem: Item<Game>
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, { "" }, Item.IGNORE_SET, this)

    override fun asItems(): List<Item<Game>> = listOf(
            Item.text(holder.get(0), 0, Item.NUMBER, R.string.game_competitors, this::name, Item.IGNORE_SET, this),
            Item.number(holder.get(1), 1, Item.INPUT, R.string.game_home_score, homeScore::toString, this::setHomeScore, this),
            Item.number(holder.get(2), 2, Item.INPUT, R.string.game_away_score, awayScore::toString, this::setAwayScore, this),
            Item.number(holder.get(3), 3, Item.NUMBER, R.string.game_round, round::toString, Item.IGNORE_SET, this),
            Item.number(holder.get(4), 4, Item.NUMBER, R.string.game_leg, leg::toString, Item.IGNORE_SET, this)
    )

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is Game) id == other.id else score == other.score
                    && home.areContentsTheSame(other.home)
                    && away.areContentsTheSame(other.away)

    override fun hasMajorFields(): Boolean =
            areNotEmpty(id, refPath, score) && home.hasMajorFields() && away.hasMajorFields()

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun update(updated: Game) {
        this.id = updated.id
        this.score = updated.score
        this.matchUp = updated.matchUp
        this.created = updated.created
        this.leg = updated.leg
        this.seed = updated.seed
        this.round = updated.round
        this.homeScore = updated.homeScore
        this.awayScore = updated.awayScore
        this.isEnded = updated.isEnded
        this.canDraw = updated.canDraw
        this.sport.update(updated.sport)
        this.homeEntityId = updated.homeEntityId
        this.awayEntityId = updated.awayEntityId
        this.winnerEntityId = updated.winnerEntityId
        if (updated.referee.hasMajorFields())
            this.referee.update(updated.referee)
        if (updated.host.hasMajorFields())
            this.host.update(updated.host)
        if (updated.tournament.hasMajorFields())
            this.tournament.update(updated.tournament)
        if (updated.home.hasMajorFields() && this.home.hasSameType(updated.home))
            this.home.update(updated.home)
        else
            this.home = updated.home
        if (updated.away.hasMajorFields() && this.away.hasSameType(updated.away))
            this.away.update(updated.away)
        else
            this.away = updated.away
        if (updated.winner.hasMajorFields() && this.winner.hasSameType(updated.winner))
            this.winner.update(updated.winner)
        else
            this.winner = updated.winner
        if (updated.event.hasMajorFields()) this.event.update(updated.event)
    }

    override fun compareTo(other: Game): Int {
        val createdComparison = created.compareTo(other.created)

        return if (createdComparison != 0) createdComparison else id.compareTo(other.id)
    }

    override fun describeContents(): Int = 0

    class GsonAdapter : JsonSerializer<Game>, JsonDeserializer<Game> {

        override fun serialize(src: Game, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val body = JsonObject()
            body.addProperty(ENDED, src.isEnded)
            body.addProperty(REF_PATH, src.refPath)
            body.addProperty(HOME_SCORE, src.homeScore)
            body.addProperty(AWAY_SCORE, src.awayScore)
            body.addProperty(HOME, src.home.entity.getId())
            body.addProperty(AWAY, src.away.entity.getId())
            body.addProperty(REFEREE, if (src.referee.isEmpty) null else src.referee.id)
            return body
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Game {
            if (json.isJsonPrimitive) {
                return Game(json.asString, "", "", "TBD", "", "", "", "",
                        Date(), Sport.empty(), User.empty(), Team.empty(), Event.empty(), Tournament.empty(Team.empty()),
                        Competitor.empty(), Competitor.empty(), Competitor.empty(),
                        0, 0, 0, 0, 0, ended = false, canDraw = false)
            }

            val body = json.asJsonObject

            val id = ModelUtils.asString(ID_KEY, body)
            val name = ModelUtils.asString(NAME, body)
            val refPath = ModelUtils.asString(REF_PATH, body)
            val score = ModelUtils.asString(SCORE, body)
            val matchUp = ModelUtils.asString(MATCH_UP, body)
            val homeEntityId = ModelUtils.asString(HOME_ENTITY_ID, body)
            val awayEntityId = ModelUtils.asString(AWAY_ENTITY_ID, body)
            val winnerEntityId = ModelUtils.asString(WINNER_ENTITY_ID, body)
            val created = ModelUtils.asString(CREATED_KEY, body)
            val sportCode = ModelUtils.asString(SPORT_KEY, body)

            val seed = ModelUtils.asFloat(SEED, body).toInt()
            val leg = ModelUtils.asFloat(LEG, body).toInt()
            val round = ModelUtils.asFloat(ROUND, body).toInt()
            val homeScore = ModelUtils.asFloat(HOME_SCORE, body).toInt()
            val awayScore = ModelUtils.asFloat(AWAY_SCORE, body).toInt()
            val ended = ModelUtils.asBoolean(ENDED, body)
            val canDraw = ModelUtils.asBoolean(CAN_DRAW, body)

            val sport = Config.sportFromCode(sportCode)
            val referee: User = context.deserialize<User>(body.get(REFEREE), User::class.java)
                    ?: User.empty()
            val host: Team = context.deserialize<Team>(body.get(HOST), Team::class.java)
                    ?: Team.empty()
            val event: Event = context.deserialize<Event>(body.get(EVENT), Event::class.java)
                    ?: Event.empty()
            var tournament: Tournament? = context.deserialize<Tournament>(body.get(TOURNAMENT), Tournament::class.java)
            val home: Competitor = context.deserialize<Competitor>(body.get(HOME), Competitor::class.java)
                    ?: Competitor.empty()
            val away: Competitor = context.deserialize<Competitor>(body.get(AWAY), Competitor::class.java)
                    ?: Competitor.empty()
            val winner = if (body.has(WINNER)) context.deserialize(body.get(WINNER), Competitor::class.java) else Competitor.empty()

            if (tournament == null) tournament = Tournament.empty()

            return Game(id, name, refPath, score, matchUp, homeEntityId, awayEntityId, winnerEntityId,
                    ModelUtils.parseDate(created), sport, referee, host, event, tournament,
                    home, away, winner, seed, leg, round, homeScore, awayScore, ended, canDraw)
        }

        companion object {

            private const val ID_KEY = "_id"
            private const val NAME = "name"
            private const val REF_PATH = "refPath"
            private const val SCORE = "score"
            private const val MATCH_UP = "matchUp"
            private const val CREATED_KEY = "created"
            private const val SPORT_KEY = "sport"
            private const val REFEREE = "referee"
            private const val EVENT = "event"
            private const val TOURNAMENT = "tournament"
            private const val HOST = "host"
            private const val HOME_ENTITY_ID = "homeEntity"
            private const val AWAY_ENTITY_ID = "awayEntity"
            private const val WINNER_ENTITY_ID = "winnerEntity"
            private const val HOME = "home"
            private const val AWAY = "away"
            private const val WINNER = "winner"
            private const val LEG = "leg"
            private const val SEED = "seed"
            private const val ROUND = "round"
            private const val HOME_SCORE = "homeScore"
            private const val AWAY_SCORE = "awayScore"
            private const val ENDED = "ended"
            private const val CAN_DRAW = "canDraw"
        }
    }

    companion object {

        @Ignore
        private val holder = IdCache.cache(5)

        fun empty(team: Team): Game {
            val sport = team.sport
            return Game(
                    id = "",
                    name = "",
                    refPath = sport.refType(),
                    score = "TBD",
                    matchUp = "",
                    homeEntityId = "",
                    awayEntityId = "",
                    winnerEntityId = "",
                    created = Date(),
                    sport = sport,
                    referee = User.empty(),
                    host = team,
                    event = Event.empty(),
                    tournament = Tournament.empty(),
                    home = Competitor.empty(),
                    away = Competitor.empty(),
                    winner = Competitor.empty(),
                    seed = 0,
                    leg = 0,
                    round = 0,
                    homeScore = 0,
                    awayScore = 0,
                    ended = false,
                    canDraw = true
            )
        }

        fun withId(id: String): Game {
            val empty = empty(Team.empty())
            empty.id = id
            return empty
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Game> = object : Parcelable.Creator<Game> {
            override fun createFromParcel(`in`: Parcel): Game = Game(`in`)

            override fun newArray(size: Int): Array<Game?> = arrayOfNulls(size)
        }
    }
}
