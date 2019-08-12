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

package com.mainstreetcode.teammate.persistence.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.asIntOrZero
import java.text.SimpleDateFormat
import java.util.*


@Entity(
        tableName = "games",
        foreignKeys = [
            ForeignKey(entity = TournamentEntity::class, parentColumns = ["tournament_id"], childColumns = ["game_tournament"], onDelete = CASCADE)
        ]
)
open class GameEntity : Parcelable {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "game_id")
    private var id: String

    @ColumnInfo(name = "game_name")
    var name: String
        protected set

    @ColumnInfo(name = "game_ref_path")
    var refPath: String
        protected set

    @ColumnInfo(name = "game_score")
    var score: String
        protected set

    @ColumnInfo(name = "game_match_up")
    var matchUp: String
        protected set

    @ColumnInfo(name = "game_home_entity")
    var homeEntityId: String
        protected set

    @ColumnInfo(name = "game_away_entity")
    var awayEntityId: String
        protected set

    @ColumnInfo(name = "game_winner_entity")
    var winnerEntityId: String
        protected set

    @ColumnInfo(name = "game_created")
    var created: Date
        protected set

    @ColumnInfo(name = "game_sport")
    var sport: Sport
        protected set

    @ColumnInfo(name = "game_referee")
    var referee: User
        protected set

    @ColumnInfo(name = "game_host")
    var host: Team
        protected set

    @ColumnInfo(name = "game_event")
    var event: Event
        protected set

    @ColumnInfo(name = "game_tournament")
    var tournament: Tournament
        protected set

    @ColumnInfo(name = "game_home")
    var home: Competitor

    @ColumnInfo(name = "game_away")
    var away: Competitor
        protected set

    @ColumnInfo(name = "game_winner")
    var winner: Competitor
        protected set

    @ColumnInfo(name = "game_leg")
    var leg: Int = 0
        protected set

    @ColumnInfo(name = "game_seed")
    var seed: Int = 0
        protected set

    @ColumnInfo(name = "game_round")
    var round: Int = 0
        protected set

    @ColumnInfo(name = "game_home_score")
    var homeScore: Int = 0
        protected set

    @ColumnInfo(name = "game_away_score")
    var awayScore: Int = 0
        protected set

    @ColumnInfo(name = "game_ended")
    var isEnded: Boolean = false

    @ColumnInfo(name = "game_can_draw")
    var canDraw: Boolean = false
        protected set

    val imageUrl: String
        get() = EMPTY_STRING

    val date: String
        get() = if (event.isEmpty) "" else prettyPrinter.format(event.startDate)

    constructor(id: String, name: String, refPath: String, score: String, matchUp: String,
                homeEntityId: String, awayEntityId: String, winnerEntityId: String,
                created: Date, sport: Sport, referee: User, host: Team, event: Event, tournament: Tournament,
                home: Competitor, away: Competitor, winner: Competitor,
                seed: Int, leg: Int, round: Int, homeScore: Int, awayScore: Int,
                ended: Boolean, canDraw: Boolean) {
        this.id = id
        this.name = name
        this.refPath = refPath
        this.score = score
        this.matchUp = matchUp
        this.homeEntityId = homeEntityId
        this.awayEntityId = awayEntityId
        this.winnerEntityId = winnerEntityId
        this.created = created
        this.sport = sport
        this.referee = referee
        this.host = host
        this.event = event
        this.tournament = tournament
        this.home = home
        this.away = away
        this.winner = winner
        this.seed = seed
        this.leg = leg
        this.round = round
        this.homeScore = homeScore
        this.awayScore = awayScore
        this.isEnded = ended
        this.canDraw = canDraw
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()!!
        name = `in`.readString()!!
        refPath = `in`.readString()!!
        score = `in`.readString()!!
        matchUp = `in`.readString()!!
        homeEntityId = `in`.readString()!!
        awayEntityId = `in`.readString()!!
        winnerEntityId = `in`.readString()!!
        created = Date(`in`.readLong())
        sport = Config.sportFromCode(`in`.readString()!!)
        referee = `in`.readValue(User::class.java.classLoader) as User
        host = `in`.readValue(Team::class.java.classLoader) as Team
        event = `in`.readValue(Event::class.java.classLoader) as Event
        tournament = `in`.readValue(Tournament::class.java.classLoader) as Tournament
        home = `in`.readValue(Competitor::class.java.classLoader) as Competitor
        away = `in`.readValue(Competitor::class.java.classLoader) as Competitor
        winner = `in`.readValue(Competitor::class.java.classLoader) as Competitor
        seed = `in`.readInt()
        leg = `in`.readInt()
        round = `in`.readInt()
        homeScore = `in`.readInt()
        awayScore = `in`.readInt()
        isEnded = `in`.readByte().toInt() != 0x00
        canDraw = `in`.readByte().toInt() != 0x00
    }

    fun getId(): String = id

    protected fun setId(id: String) {
        this.id = id
    }

    fun betweenUsers(): Boolean = User.COMPETITOR_TYPE == refPath

    fun isCompeting(competitive: Competitive): Boolean =
            home.entity == competitive || away.entity == competitive

    fun competitorsNotAccepted(): Boolean = !home.isAccepted || !away.isAccepted

    fun competitorsDeclined(): Boolean = home.isDeclined || away.isDeclined

    fun setHomeScore(homeScore: String) {
        this.homeScore = homeScore.asIntOrZero()
    }

    fun setAwayScore(awayScore: String) {
        this.awayScore = awayScore.asIntOrZero()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameEntity) return false

        val event = other as GameEntity?

        return id == event!!.id
    }


    override fun hashCode(): Int = id.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(refPath)
        dest.writeString(score)
        dest.writeString(matchUp)
        dest.writeString(homeEntityId)
        dest.writeString(awayEntityId)
        dest.writeString(winnerEntityId)
        dest.writeLong(created.time)
        dest.writeString(sport.code)
        dest.writeValue(referee)
        dest.writeValue(host)
        dest.writeValue(event)
        dest.writeValue(tournament)
        dest.writeValue(home)
        dest.writeValue(away)
        dest.writeValue(winner)
        dest.writeInt(seed)
        dest.writeInt(leg)
        dest.writeInt(round)
        dest.writeInt(homeScore)
        dest.writeInt(awayScore)
        dest.writeByte((if (isEnded) 0x01 else 0x00).toByte())
        dest.writeByte((if (canDraw) 0x01 else 0x00).toByte())
    }

    companion object {

        private val prettyPrinter = SimpleDateFormat("dd MMM", Locale.US)

        @JvmField
        val CREATOR: Parcelable.Creator<GameEntity> = object : Parcelable.Creator<GameEntity> {
            override fun createFromParcel(`in`: Parcel): GameEntity = GameEntity(`in`)

            override fun newArray(size: Int): Array<GameEntity?> = arrayOfNulls(size)
        }
    }
}
