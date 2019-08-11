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
import androidx.room.Ignore
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mainstreetcode.teammate.persistence.entity.CompetitorEntity
import com.mainstreetcode.teammate.util.ModelUtils
import com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

class Competitor : CompetitorEntity,
        Competitive,
        Model<Competitor> {

    override val refType: String
        get() = entity.refType

    override val name: CharSequence
        get() = entity.name

    override val isEmpty: Boolean
        get() = id.isBlank()

    @Ignore
    @Transient
    var competitionName: CharSequence = ""
        private set

    val tournament: Tournament
        get() = if (tournamentId.isNullOrBlank()) Tournament.empty() else Tournament.withId(tournamentId!!)

    val game: Game
        get() = if (gameId.isNullOrBlank()) Game.empty(Team.empty()) else Game.withId(gameId!!)

    override val imageUrl: String
        get() = entity.imageUrl

    constructor(
            id: String,
            refPath: String,
            tournamentId: String?,
            gameId: String?,
            entity: Competitive,
            created: Date,
            seed: Int,
            accepted: Boolean,
            declined: Boolean
    ) : super(id, refPath, tournamentId, gameId, entity, created, seed, accepted, declined)

    private constructor(`in`: Parcel) : super(`in`)

    internal fun hasSameType(other: Competitor): Boolean = refType == other.refType

    fun isOneOffGame(): Boolean = !gameId.isNullOrBlank()

    override fun hasMajorFields(): Boolean = areNotEmpty(id, refPath) && entity.hasMajorFields()

    override fun makeCopy(): Competitive = entity.makeCopy()

    override fun update(updated: Competitor) {
        this.id = updated.id
        this.seed = updated.seed
        this.isAccepted = updated.isAccepted
        this.isDeclined = updated.isDeclined

        this.tournamentId = updated.tournamentId
        this.gameId = updated.gameId

        updateEntity(updated.entity)
    }

    fun updateEntity(updated: Competitive) {
        if (entity.update(updated)) return
        entity = updated.makeCopy()
    }

    override fun areContentsTheSame(other: Differentiable): Boolean {
        return if (other !is Competitor) id == other.id else entity.javaClass == other.entity.javaClass
                && entity.refType == other.entity.refType
                && entity.getId() == other.entity.getId()
    }

    override fun compareTo(other: Competitor): Int {
        val otherEntity = other.entity
        if (entity is User && otherEntity is User) return (entity as User).compareTo(otherEntity)
        return if (entity is Team && otherEntity is Team) (entity as Team).compareTo(otherEntity) else 0
    }

    class GsonAdapter : JsonSerializer<Competitor>, JsonDeserializer<Competitor> {

        override fun serialize(src: Competitor, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            if (src.isEmpty) return JsonPrimitive(src.entity.getId())

            val json = JsonObject()
            json.addProperty(ACCEPTED, src.isAccepted)
            json.addProperty(DECLINED, src.isDeclined)

            return json
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Competitor {

            if (json.isJsonPrimitive) return Competitor(json.asString, "", "", "", EmptyCompetitor(), Date(), -1, accepted = false, declined = false)

            val jsonObject = json.asJsonObject

            var tournament: Tournament? = context.deserialize<Tournament>(jsonObject.get(TOURNAMENT), Tournament::class.java)
            var game: Game? = context.deserialize<Game>(jsonObject.get(GAME), Game::class.java)

            if (tournament == null) tournament = Tournament.empty()
            if (game == null) game = Game.empty(Team.empty())

            val id = ModelUtils.asString(ID, jsonObject)
            val refPath = ModelUtils.asString(REF_PATH, jsonObject)
            val created = ModelUtils.asString(CREATED, jsonObject)
            val tournamentId = if (tournament.isEmpty) null else tournament.id
            val gameId = if (game.isEmpty) null else game.id

            val seed = ModelUtils.asFloat(SEED, jsonObject).toInt()
            val accepted = ModelUtils.asBoolean(ACCEPTED, jsonObject)
            val declined = ModelUtils.asBoolean(DECLINED, jsonObject)

            val competitive = context.deserialize<Competitive>(jsonObject.get(ENTITY),
                    if (User.COMPETITOR_TYPE == refPath) User::class.java else Team::class.java)

            val competitor = Competitor(
                    id = id,
                    refPath = refPath,
                    tournamentId = tournamentId,
                    gameId = gameId,
                    entity = competitive,
                    created = ModelUtils.parseDate(created),
                    seed = seed,
                    accepted = accepted,
                    declined = declined
            )

            if (!game.isEmpty) competitor.competitionName = game.name
            else if (!tournament.isEmpty) competitor.competitionName = tournament.name

            return competitor
        }

        companion object {

            private const val ID = "_id"
            private const val REF_PATH = "refPath"
            private const val ENTITY = "entity"
            private const val TOURNAMENT = "tournament"
            private const val GAME = "game"
            private const val CREATED = "created"
            private const val SEED = "seed"
            private const val ACCEPTED = "accepted"
            private const val DECLINED = "declined"
        }
    }

    companion object {

        fun empty(): Competitor =
                Competitor("", "", null, null, EmptyCompetitor(), Date(), -1, accepted = false, declined = false)

        fun empty(entity: Competitive): Competitor =
                Competitor("", "", null, null, entity, Date(), -1, accepted = false, declined = false)

        @JvmField
        val CREATOR: Parcelable.Creator<Competitor> = object : Parcelable.Creator<Competitor> {
            override fun createFromParcel(`in`: Parcel): Competitor = Competitor(`in`)

            override fun newArray(size: Int): Array<Competitor?> = arrayOfNulls(size)
        }
    }
}
