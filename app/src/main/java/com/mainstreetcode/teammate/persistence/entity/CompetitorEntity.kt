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
import androidx.annotation.NonNull

import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.EmptyCompetitor
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User

import java.util.Date

import androidx.room.ForeignKey.CASCADE

@Entity(tableName = "competitors", foreignKeys = [ForeignKey(entity = TournamentEntity::class, parentColumns = ["tournament_id"], childColumns = ["competitor_tournament"], onDelete = CASCADE), ForeignKey(entity = GameEntity::class, parentColumns = ["game_id"], childColumns = ["competitor_game"], onDelete = CASCADE)])
open class CompetitorEntity : Parcelable {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "competitor_id")
    private var id: String

    @ColumnInfo(name = "competitor_ref_path")
    var refPath: String
        protected set

    @ColumnInfo(name = "competitor_tournament")
    var tournamentId: String?
        protected set

    @ColumnInfo(name = "competitor_game")
    var gameId: String?
        protected set

    @ColumnInfo(name = "competitor_entity")
    var entity: Competitive

    @ColumnInfo(name = "competitor_created")
    var created: Date
        protected set

    @ColumnInfo(name = "competitor_seed")
    var seed: Int = 0

    @ColumnInfo(name = "competitor_accepted")
    var isAccepted: Boolean = false

    @ColumnInfo(name = "competitor_declined")
    var isDeclined: Boolean = false

    val seedText: String
        get() = if (seed > -1) (seed + 1).toString() else ""

    constructor(id: String, refPath: String,
                tournamentId: String?, gameId: String?,
                entity: Competitive, created: Date,
                seed: Int, accepted: Boolean, declined: Boolean) {
        this.id = id
        this.refPath = refPath
        this.tournamentId = tournamentId
        this.gameId = gameId
        this.entity = entity
        this.created = created
        this.seed = seed
        this.isAccepted = accepted
        this.isDeclined = declined
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()!!
        refPath = `in`.readString()!!
        tournamentId = `in`.readString()
        gameId = `in`.readString()
        entity = fromParcel(refPath, `in`)
        created = Date(`in`.readLong())
        seed = `in`.readInt()
        isAccepted = `in`.readByte().toInt() != 0x00
        isDeclined = `in`.readByte().toInt() != 0x00
    }

    fun getId(): String = id

    protected fun setId(id: String) {
        this.id = id
    }

    fun hasNotResponded(): Boolean = !isAccepted && !isDeclined

    fun accept() {
        isAccepted = true
        isDeclined = !isAccepted
    }

    fun decline() {
        isDeclined = true
        isAccepted = !isDeclined
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompetitorEntity) return false

        val that = other as CompetitorEntity?

        return id == that!!.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(refPath)
        dest.writeString(tournamentId)
        dest.writeString(gameId)
        writeToParcel(entity, dest)
        dest.writeLong(created.time)
        dest.writeInt(seed)
        dest.writeByte((if (isAccepted) 0x01 else 0x00).toByte())
        dest.writeByte((if (isDeclined) 0x01 else 0x00).toByte())
    }

    companion object {

        private fun fromParcel(refPath: String, `in`: Parcel): Competitive = when (refPath) {
            User.COMPETITOR_TYPE -> `in`.readValue(User::class.java.classLoader) as User
            Team.COMPETITOR_TYPE -> `in`.readValue(Team::class.java.classLoader) as Team
            else -> `in`.readValue(EmptyCompetitor::class.java.classLoader) as EmptyCompetitor
        }

        private fun writeToParcel(competitive: Competitive, dest: Parcel) =
                dest.writeValue(competitive)

        @JvmField
        val CREATOR: Parcelable.Creator<CompetitorEntity> = object : Parcelable.Creator<CompetitorEntity> {
            override fun createFromParcel(`in`: Parcel): CompetitorEntity = CompetitorEntity(`in`)

            override fun newArray(size: Int): Array<CompetitorEntity?> = arrayOfNulls(size)
        }
    }
}
