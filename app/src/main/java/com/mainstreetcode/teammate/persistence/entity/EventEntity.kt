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
import com.google.android.gms.maps.model.LatLng
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.enums.Visibility
import com.mainstreetcode.teammate.util.asIntOrZero
import com.mainstreetcode.teammate.util.parsePrettyDate
import com.mainstreetcode.teammate.util.prettyPrint
import com.mainstreetcode.teammate.util.processEmoji
import java.util.*


@Entity(
        tableName = "events",
        foreignKeys = [
            ForeignKey(entity = TeamEntity::class, parentColumns = ["team_id"], childColumns = ["event_team"], onDelete = CASCADE)
        ]
)
open class EventEntity : Parcelable {

    //private static final SimpleDateFormat timePrinter = new SimpleDateFormat("HH:mm", Locale.US);

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    var id: String

    @ColumnInfo(name = "event_game_id")
    var gameId: String
        protected set

    @ColumnInfo(name = "event_image_url")
    var imageUrl: String
        protected set

    @ColumnInfo(name = "event_name")
    var name: CharSequence

    @ColumnInfo(name = "event_notes")
    var notes: CharSequence

    @ColumnInfo(name = "event_location_name")
    var locationName: CharSequence
        get() = field.processEmoji()

    @ColumnInfo(name = "event_team")
    var team: Team

    @ColumnInfo(name = "event_start_date")
    var startDate: Date

    @ColumnInfo(name = "event_end_date")
    var endDate: Date
        protected set

    @ColumnInfo(name = "event_location")
    var location: LatLng?
        protected set

    @ColumnInfo(name = "event_visibility")
    var visibility: Visibility
        protected set

    @ColumnInfo(name = "event_spots")
    var spots: Int = 0
        protected set

    val time: String
        get() = startDate.prettyPrint()

    val isPublic: Boolean
        get() = visibility.isPublic

    constructor(id: String, gameId: String, imageUrl: String,
                name: CharSequence, notes: CharSequence, locationName: CharSequence,
                startDate: Date, endDate: Date, team: Team, location: LatLng?, visibility: Visibility,
                spots: Int) {
        this.id = id
        this.gameId = gameId
        this.imageUrl = imageUrl
        this.name = name
        this.notes = notes
        this.visibility = visibility
        this.locationName = locationName
        this.startDate = startDate
        this.endDate = endDate
        this.team = team
        this.location = location
        this.spots = spots
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()!!
        gameId = `in`.readString()!!
        imageUrl = `in`.readString()!!
        name = `in`.readString()!!
        notes = `in`.readString()!!
        locationName = `in`.readString()!!
        startDate = Date(`in`.readLong())
        endDate = Date(`in`.readLong())
        team = `in`.readValue(Team::class.java.classLoader) as Team
        location = `in`.readValue(LatLng::class.java.classLoader) as? LatLng
        visibility = Config.visibilityFromCode(`in`.readString()!!)
        spots = `in`.readInt()
    }

    

    

    fun setName(name: String) {
        this.name = name
    }

    protected fun setNotes(notes: String) {
        this.notes = notes
    }

    fun setVisibility(visibility: String) {
        this.visibility = Config.visibilityFromCode(visibility)
    }

    protected fun setLocationName(locationName: String) {
        this.locationName = locationName
    }

    protected fun setStartDate(startDate: String) {
        this.startDate = startDate.parsePrettyDate()
    }

    protected fun setEndDate(endDate: String) {
        this.endDate = endDate.parsePrettyDate()
    }

    protected fun setSpots(spots: String) {
        this.spots = spots.asIntOrZero()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EventEntity) return false

        val event = other as EventEntity?

        return id == event!!.id
    }


    override fun hashCode(): Int = id.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(gameId)
        dest.writeString(imageUrl)
        dest.writeString(name.toString())
        dest.writeString(notes.toString())
        dest.writeString(locationName.toString())
        dest.writeLong(startDate.time)
        dest.writeLong(endDate.time)
        dest.writeValue(team)
        dest.writeValue(location)
        dest.writeString(visibility.code)
        dest.writeInt(spots)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<EventEntity> = object : Parcelable.Creator<EventEntity> {
            override fun createFromParcel(`in`: Parcel): EventEntity = EventEntity(`in`)

            override fun newArray(size: Int): Array<EventEntity?> = arrayOfNulls(size)
        }
    }
}
