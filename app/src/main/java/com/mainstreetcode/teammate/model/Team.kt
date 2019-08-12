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

import android.location.Address
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import androidx.room.Ignore
import com.google.android.gms.maps.model.LatLng
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
import com.mainstreetcode.teammate.persistence.entity.TeamEntity
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.areNotEmpty
import com.mainstreetcode.teammate.util.asFloatOrZero
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.parseCoordinates
import com.mainstreetcode.teammate.util.parseDateISO8601
import com.mainstreetcode.teammate.util.processEmoji
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

/**
 * Teams
 */

class Team : TeamEntity,
        TeamHost,
        Competitive,
        Model<Team>,
        HeaderedModel<Team>,
        ListableModel<Team> {

    override val refType: String
        get() = COMPETITOR_TYPE

    override val isEmpty: Boolean
        get() = this == empty()

    override var imageUrl: String
        get() = if (super.imageUrl.isBlank()) Config.getDefaultTeamLogo() else super.imageUrl
        set(value) {
            super.imageUrl = value
        }

    constructor(
            id: String,
            imageUrl: String,
            screenName: String,
            city: String,
            state: String,
            zip: String,
            name: CharSequence,
            description: CharSequence,
            created: Date,
            location: LatLng?,
            sport: Sport,
            storageUsed: Long,
            maxStorage: Long,
            minAge: Int,
            maxAge: Int
    ) : super(id, imageUrl, screenName, city, state, zip, name, description, created, location, sport, storageUsed, maxStorage, minAge, maxAge)

    private constructor(`in`: Parcel) : super(`in`)

    override val team: Team
        get() = this

    override val headerItem: Item<Team>
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, Item.nullToEmpty(imageUrl), { this.imageUrl = it }, this)

    override fun asItems(): List<Item<Team>> = listOf(
            Item.text(holder.get(0), 0, Item.INPUT, R.string.team_name, Item.nullToEmpty(name), this::setName, this),
            Item.text(holder.get(1), 1, Item.SPORT, R.string.team_sport, sport::code, this::setSport, this)
                    .textTransformer { value -> Config.sportFromCode(value.toString()).getName() },
            Item.text(holder.get(2), 2, Item.INFO, R.string.screen_name, Item.nullToEmpty(screenName), { this.screenName= it }, this),
            Item.text(holder.get(3), 3, Item.CITY, R.string.city, Item.nullToEmpty(city), { this.city= it }, this),
            Item.text(holder.get(4), 4, Item.STATE, R.string.state, Item.nullToEmpty(state), { this.state= it }, this),
            Item.text(holder.get(5), 5, Item.ZIP, R.string.zip, Item.nullToEmpty(zip), { this.zip= it }, this),
            Item.text(holder.get(6), 6, Item.DESCRIPTION, R.string.team_description, Item.nullToEmpty(description), this::setDescription, this),
            Item.number(holder.get(7), 7, Item.NUMBER, R.string.team_min_age, minAge::toString, this::setMinAge, this),
            Item.number(holder.get(8), 8, Item.NUMBER, R.string.team_max_age, maxAge::toString, this::setMaxAge, this),
            Item.text(holder.get(9), 9, Item.ABOUT, R.string.team_storage_used, { "$storageUsed/$maxStorage MB" }, null, this)
    )

    override fun areContentsTheSame(other: Differentiable): Boolean {
        if (other !is Team) return id == other.id
        val same = (name == other.name && city == other.city
                && imageUrl == other.imageUrl)

        if (!same) return false
        sport
        return sport == other.sport
    }

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun update(updated: Team) {
        this.id = updated.id
        this.name = updated.name
        this.screenName = updated.screenName
        this.city = updated.city
        this.state = updated.state
        this.zip = updated.zip
        this.description = updated.description
        this.minAge = updated.minAge
        this.maxAge = updated.maxAge
        this.imageUrl = updated.imageUrl
        this.storageUsed = updated.storageUsed

        this.location = updated.location
        this.sport.update(updated.sport)
    }

    override fun update(other: Competitive): Boolean {
        if (other !is Team) return false
        update(other)
        return true
    }

    override fun makeCopy(): Competitive {
        val copy = empty()
        copy.update(this)
        return copy
    }

    override fun compareTo(other: Team): Int {
        val nameComparision = name.toString().compareTo(other.name.toString())
        return if (nameComparision != 0) nameComparision else id.compareTo(other.id)
    }

    override fun hasMajorFields(): Boolean = areNotEmpty(id, name, city, state)

    fun setAddress(address: Address) {
        city = address.locality ?: address.subLocality ?: "N/A"
        state = address.adminArea ?: "N/A"
        zip = address.postalCode ?: "N/A"

        city = city
        state = state
        zip = zip

        location = LatLng(address.latitude, address.longitude)
    }

    override fun describeContents(): Int = 0

    open class GsonAdapter : JsonSerializer<Team>, JsonDeserializer<Team> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Team {
            if (json.isJsonPrimitive) {
                return Team(json.asString, "", "", "", "", "", "", "", Date(), LatLng(0.0, 0.0), Sport.empty(), 0, 0, 0, 0)
            }

            val teamJson = json.asJsonObject

            val id = teamJson.asStringOrEmpty(UID_KEY)
            val name = teamJson.asStringOrEmpty(NAME_KEY)
            val screenName = teamJson.asStringOrEmpty(SCREEN_NAME)
            val city = teamJson.asStringOrEmpty(CITY_KEY)
            val state = teamJson.asStringOrEmpty(STATE_KEY)
            val zip = teamJson.asStringOrEmpty(ZIP_KEY)
            val sportCode = teamJson.asStringOrEmpty(SPORT_KEY)
            val description = teamJson.asStringOrEmpty(DESCRIPTION_KEY)
            val imageUrl = teamJson.asStringOrEmpty(IMAGE_URL_KEY)
            val created = teamJson.asStringOrEmpty(CREATED_KEY).parseDateISO8601()
            val location = teamJson.parseCoordinates(LOCATION_KEY)
            val sport = Config.sportFromCode(sportCode)
            val storageUsed = teamJson.asFloatOrZero(STORAGE_USED_KEY).toLong()
            val maxStorage = teamJson.asFloatOrZero(MAX_STORAGE_KEY).toLong()
            val minAge = teamJson.asFloatOrZero(MIN_AGE_KEY).toInt()
            val maxAge = teamJson.asFloatOrZero(MAX_AGE_KEY).toInt()

            return Team(id, imageUrl, screenName, city, state, zip,
                    name.processEmoji(), description.processEmoji(),
                    created, location, sport, storageUsed, maxStorage, minAge, maxAge)
        }

        override fun serialize(src: Team, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val team = JsonObject()
            team.addProperty(NAME_KEY, src.name.toString())
            team.addProperty(CITY_KEY, src.city)
            team.addProperty(STATE_KEY, src.state)
            team.addProperty(ZIP_KEY, src.zip)
            team.addProperty(DESCRIPTION_KEY, src.description.toString())
            team.addProperty(MIN_AGE_KEY, src.minAge)
            team.addProperty(MAX_AGE_KEY, src.maxAge)

            if (!TextUtils.isEmpty(src.screenName))
                team.addProperty(SCREEN_NAME, src.screenName)

            val sportCode = src.sport.code
            if (!TextUtils.isEmpty(sportCode)) team.addProperty(SPORT_KEY, sportCode)

            if (src.location != null) {
                val coordinates = JsonArray()
                coordinates.add(src.location!!.longitude)
                coordinates.add(src.location!!.latitude)
                team.add(LOCATION_KEY, coordinates)
            }

            return team
        }

        companion object {

            private const val UID_KEY = "_id"
            private const val NAME_KEY = "name"
            private const val SCREEN_NAME = "screenName"
            private const val CITY_KEY = "city"
            private const val STATE_KEY = "state"
            private const val ZIP_KEY = "zip"
            private const val SPORT_KEY = "sport"
            private const val DESCRIPTION_KEY = "description"
            private const val IMAGE_URL_KEY = "imageUrl"
            private const val CREATED_KEY = "created"
            private const val LOCATION_KEY = "location"
            private const val STORAGE_USED_KEY = "storageUsed"
            private const val MAX_STORAGE_KEY = "maxStorage"
            private const val MIN_AGE_KEY = "minAge"
            private const val MAX_AGE_KEY = "maxAge"
        }
    }

    companion object {

        const val PHOTO_UPLOAD_KEY = "team-photo"
        const val COMPETITOR_TYPE = "team"
        private const val NEW_TEAM = "new.team"

        @Ignore
        private val holder = IdCache.cache(10)

        fun empty(): Team =
                Team(
                        id = NEW_TEAM,
                        imageUrl = Config.getDefaultTeamLogo(),
                        screenName = "",
                        city = "Detroit️",
                        state = "",
                        zip = "",
                        name = "My Team",
                        description = "",
                        created = Date(),
                        location = null,
                        sport = Sport.empty(),
                        storageUsed = 0,
                        maxStorage = 0,
                        minAge = 0,
                        maxAge = 0
                )

        @JvmField
        val CREATOR: Parcelable.Creator<Team> = object : Parcelable.Creator<Team> {
            override fun createFromParcel(`in`: Parcel): Team = Team(`in`)

            override fun newArray(size: Int): Array<Team?> = arrayOfNulls(size)
        }
    }
}
