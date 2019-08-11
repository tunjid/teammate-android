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
import androidx.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

import com.google.android.gms.maps.model.LatLng
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.enums.Sport

import java.util.Date

import com.mainstreetcode.teammate.util.ModelUtils.parse
import com.mainstreetcode.teammate.util.ModelUtils.processString


@Entity(tableName = "teams")
open class TeamEntity : Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "team_id")
    var id: String
        protected set

    @ColumnInfo(name = "team_image_url")
    open var imageUrl: String

    @ColumnInfo(name = "team_screen_name")
    var screenName: String
        protected set

    @ColumnInfo(name = "team_city")
    var city: String
        protected set

    @ColumnInfo(name = "team_state")
    var state: String
        protected set

    @ColumnInfo(name = "team_zip")
    var zip: String
        protected set

    @ColumnInfo(name = "team_name")
    var name: CharSequence
        get() = processString(field)

    @ColumnInfo(name = "team_description")
    var description: CharSequence
        get() = processString(field)

    @ColumnInfo(name = "team_sport")
    var sport: Sport
        protected set
    @ColumnInfo(name = "team_created")
    var created: Date
        protected set
    @ColumnInfo(name = "team_location")
    var location: LatLng?
        protected set

    @ColumnInfo(name = "team_storage_used")
    var storageUsed: Long = 0
        protected set
    @ColumnInfo(name = "team_max_storage")
    var maxStorage: Long = 0
        protected set
    @ColumnInfo(name = "team_min_age")
    var minAge: Int = 0
        protected set
    @ColumnInfo(name = "team_max_age")
    var maxAge: Int = 0
        protected set

    val sportAndName: CharSequence
        get() = sport.appendEmoji(name)

    constructor(id: String, imageUrl: String, screenName: String,
                city: String, state: String, zip: String,
                name: CharSequence, description: CharSequence,
                created: Date, location: LatLng?, sport: Sport,
                storageUsed: Long, maxStorage: Long,
                minAge: Int, maxAge: Int) {
        this.id = id
        this.imageUrl = imageUrl
        this.screenName = screenName
        this.city = city
        this.state = state
        this.zip = zip
        this.name = name
        this.description = description
        this.sport = sport
        this.created = created
        this.location = location
        this.storageUsed = storageUsed
        this.maxStorage = maxStorage
        this.minAge = minAge
        this.maxAge = maxAge
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()!!
        imageUrl = `in`.readString()!!
        screenName = `in`.readString()!!
        city = `in`.readString()!!
        state = `in`.readString()!!
        zip = `in`.readString()!!
        name = `in`.readString()!!
        description = `in`.readString()!!
        created = Date(`in`.readLong())
        location = `in`.readValue(LatLng::class.java.classLoader) as? LatLng
        sport = Config.sportFromCode(`in`.readString())
        storageUsed = `in`.readLong()
        maxStorage = `in`.readLong()
        minAge = `in`.readInt()
        maxAge = `in`.readInt()
    }

    protected fun setName(name: String) {
        this.name = name
    }

    fun setSport(code: String) {
        this.sport = Config.sportFromCode(code)
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun setMinAge(minAge: String) {
        this.minAge = parse(minAge)
    }

    fun setMaxAge(maxAge: String) {
        this.maxAge = parse(maxAge)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TeamEntity) return false

        val team = other as TeamEntity?

        return id == team!!.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(imageUrl)
        dest.writeString(screenName)
        dest.writeString(city)
        dest.writeString(state)
        dest.writeString(zip)
        dest.writeString(name.toString())
        dest.writeString(description.toString())
        dest.writeLong(created.time)
        dest.writeValue(location)
        dest.writeString(sport.code)
        dest.writeLong(storageUsed)
        dest.writeLong(maxStorage)
        dest.writeInt(minAge)
        dest.writeInt(maxAge)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<TeamEntity> = object : Parcelable.Creator<TeamEntity> {
            override fun createFromParcel(`in`: Parcel): TeamEntity = TeamEntity(`in`)

            override fun newArray(size: Int): Array<TeamEntity?> = arrayOfNulls(size)
        }
    }
}
