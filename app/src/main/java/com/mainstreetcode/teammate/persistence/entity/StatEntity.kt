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
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.model.enums.StatAttribute
import com.mainstreetcode.teammate.model.enums.StatAttributes
import com.mainstreetcode.teammate.model.enums.StatType
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.asFloatOrFalse
import java.util.*


@Entity(
        tableName = "stats",
        foreignKeys = [
            ForeignKey(entity = GameEntity::class, parentColumns = ["game_id"], childColumns = ["stat_game"], onDelete = CASCADE),
            ForeignKey(entity = TeamEntity::class, parentColumns = ["team_id"], childColumns = ["stat_team"], onDelete = CASCADE),
            ForeignKey(entity = UserEntity::class, parentColumns = ["user_id"], childColumns = ["stat_user"], onDelete = CASCADE)
        ]
)
open class StatEntity : Parcelable {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "stat_id")
    private var id: String

    @ColumnInfo(name = "stat_created")
    var created: Date
        protected set

    @ColumnInfo(name = "stat_type")
    var statType: StatType
        protected set

    @ColumnInfo(name = "stat_sport")
    var sport: Sport
        protected set

    @ColumnInfo(name = "stat_user")
    var user: User
        protected set

    @ColumnInfo(name = "stat_team")
    var team: Team
        protected set

    @ColumnInfo(name = "stat_game")
    var game: Game
        protected set

    @ColumnInfo(name = "stat_attributes")
    var attributes: StatAttributes
        protected set

    @ColumnInfo(name = "stat_value")
    var value: Int = 0
        protected set

    @ColumnInfo(name = "stat_time")
    var time: Float = 0.toFloat()
        protected set

    val imageUrl: String
        get() = EMPTY_STRING

    val isHome: Boolean
        get() {
            val home = game.home.entity
            return user == home || team == home
        }

    constructor(id: String, created: Date,
                statType: StatType, sport: Sport, user: User, team: Team, game: Game,
                attributes: StatAttributes?, value: Int, time: Float) {
        this.id = id
        this.created = created
        this.statType = statType
        this.sport = sport
        this.user = user
        this.team = team
        this.game = game
        this.value = value
        this.time = time
        this.attributes = attributes ?: StatAttributes()
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()!!
        created = Date(`in`.readLong())
        user = `in`.readValue(User::class.java.classLoader) as User
        team = `in`.readValue(Team::class.java.classLoader) as Team
        game = `in`.readValue(Game::class.java.classLoader) as Game
        sport = Config.sportFromCode(`in`.readString()!!)
        statType = sport.statTypeFromCode(`in`.readString()!!)
        value = `in`.readInt()
        time = `in`.readFloat()
        this.attributes = StatAttributes()
    }

    fun getId(): String = id

    protected fun setId(id: String) {
        this.id = id
    }

    operator fun contains(attribute: StatAttribute): Boolean = attributes.contains(attribute)

    protected fun setStatType(statType: String) {
        this.statType = sport.statTypeFromCode(statType)
        attributes.clear()
    }

    protected fun setTime(time: String) {
        this.time = time.asFloatOrFalse()
    }

    fun compoundAttribute(attribute: StatAttribute) {
        if (attributes.contains(attribute)) attributes.remove(attribute)
        else attributes.add(attribute)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StatEntity) return false

        val event = other as StatEntity?

        return id == event!!.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeLong(created.time)
        dest.writeValue(user)
        dest.writeValue(team)
        dest.writeValue(game)
        dest.writeString(sport.code)
        dest.writeString(statType.code)
        dest.writeInt(value)
        dest.writeFloat(time)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<StatEntity> = object : Parcelable.Creator<StatEntity> {
            override fun createFromParcel(`in`: Parcel): StatEntity = StatEntity(`in`)

            override fun newArray(size: Int): Array<StatEntity?> = arrayOfNulls(size)
        }
    }
}
