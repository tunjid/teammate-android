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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.enums.TournamentStyle
import com.mainstreetcode.teammate.model.enums.TournamentType

import java.util.Date

import androidx.room.ForeignKey.CASCADE
import com.mainstreetcode.teammate.util.ModelUtils.parse
import com.mainstreetcode.teammate.util.ModelUtils.parseBoolean
import com.mainstreetcode.teammate.util.ModelUtils.processString


@Entity(
        tableName = "tournaments",
        foreignKeys = [
            ForeignKey(entity = TeamEntity::class, parentColumns = ["team_id"], childColumns = ["tournament_host"], onDelete = CASCADE)
        ])
open class TournamentEntity : Parcelable {

    //private static final SimpleDateFormat timePrinter = new SimpleDateFormat("HH:mm", Locale.US);
    //    refPath:
    //    winner:

    @PrimaryKey
    @ColumnInfo(name = "tournament_id")
    var id: String
        protected set

    @ColumnInfo(name = "tournament_image_url")
    var imageUrl: String
        protected set

    @ColumnInfo(name = "tournament_ref_path")
    var refPath: String
        protected set

    @ColumnInfo(name = "tournament_name")
    var name: CharSequence
        get() = processString(field)

    @ColumnInfo(name = "tournament_description")
    var description: CharSequence
        get() = processString(field)

    @ColumnInfo(name = "tournament_host")
    var host: Team
        protected set

    @ColumnInfo(name = "tournament_created")
    var created: Date
        protected set

    @ColumnInfo(name = "tournament_sport")
    var sport: Sport
        protected set

    @ColumnInfo(name = "tournament_type")
    var type: TournamentType
        protected set

    @ColumnInfo(name = "tournament_style")
    var style: TournamentStyle
        protected set

    @ColumnInfo(name = "tournament_winner")
    var winner: Competitor
        protected set

    @ColumnInfo(name = "tournament_num_legs")
    var numLegs: Int = 0
        protected set

    @ColumnInfo(name = "tournament_num_rounds")
    var numRounds: Int = 0
        protected set

    @ColumnInfo(name = "tournament_current_round")
    var currentRound: Int = 0
        protected set

    @ColumnInfo(name = "tournament_num_competitors")
    var numCompetitors: Int = 0
        protected set

    @ColumnInfo(name = "tournament_single_final")
    var isSingleFinal: Boolean = false
        protected set

    val isKnockOut: Boolean
        get() = style.code.contains("noc")

    constructor(id: String, imageUrl: String, refPath: String,
                name: CharSequence, description: CharSequence,
                created: Date, host: Team, sport: Sport, type: TournamentType, style: TournamentStyle,
                winner: Competitor,
                numLegs: Int, numRounds: Int, currentRound: Int, numCompetitors: Int,
                singleFinal: Boolean) {
        this.id = id
        this.imageUrl = imageUrl
        this.refPath = refPath
        this.name = name
        this.description = description
        this.created = created
        this.host = host
        this.sport = sport
        this.type = type
        this.style = style
        this.winner = winner
        this.numLegs = numLegs
        this.numRounds = numRounds
        this.currentRound = currentRound
        this.numCompetitors = numCompetitors
        this.isSingleFinal = singleFinal
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()!!
        imageUrl = `in`.readString()!!
        refPath = `in`.readString()!!
        name = `in`.readString()!!
        description = `in`.readString()!!
        created = Date(`in`.readLong())
        host = `in`.readValue(Team::class.java.classLoader) as Team
        sport = Config.sportFromCode(`in`.readString())
        type = Config.tournamentTypeFromCode(`in`.readString())
        style = Config.tournamentStyleFromCode(`in`.readString())
        winner = `in`.readValue(Competitor::class.java.classLoader) as Competitor
        numLegs = `in`.readInt()
        numRounds = `in`.readInt()
        currentRound = `in`.readInt()
        numCompetitors = `in`.readInt()
        isSingleFinal = `in`.readByte().toInt() != 0x00
    }

    fun hasWinner(): Boolean = !winner.isEmpty

    fun hasCompetitors(): Boolean = numCompetitors > 0

    fun setName(name: String) {
        this.name = name
    }

    protected fun setDescription(description: String) {
        this.description = description
    }

    fun setType(type: String) {
        this.type = Config.tournamentTypeFromCode(type)
    }

    fun setStyle(style: String) {
        this.style = Config.tournamentStyleFromCode(style)
    }

    protected fun setNumLegs(numLegs: String) {
        this.numLegs = parse(numLegs)
    }

    protected fun setSingleFinal(singleFinal: String) {
        this.isSingleFinal = parseBoolean(singleFinal)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TournamentEntity) return false

        val event = other as TournamentEntity?

        return id == event!!.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(imageUrl)
        dest.writeString(refPath)
        dest.writeString(name.toString())
        dest.writeString(description.toString())
        dest.writeLong(created.time)
        dest.writeValue(host)
        dest.writeString(sport.code)
        dest.writeString(type.code)
        dest.writeString(style.code)
        dest.writeValue(winner)
        dest.writeInt(numLegs)
        dest.writeInt(numRounds)
        dest.writeInt(currentRound)
        dest.writeInt(numCompetitors)
        dest.writeByte((if (isSingleFinal) 0x01 else 0x00).toByte())
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<TournamentEntity> = object : Parcelable.Creator<TournamentEntity> {
            override fun createFromParcel(`in`: Parcel): TournamentEntity = TournamentEntity(`in`)

            override fun newArray(size: Int): Array<TournamentEntity?> = arrayOfNulls(size)
        }
    }
}
