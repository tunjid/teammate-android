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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.enums.Visibility
import com.mainstreetcode.teammate.persistence.entity.EventEntity
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.ISO8601Print
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.TextBitmapUtil
import com.mainstreetcode.teammate.util.areNotEmpty
import com.mainstreetcode.teammate.util.asFloatOrZero
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.fullName
import com.mainstreetcode.teammate.util.parseCoordinates
import com.mainstreetcode.teammate.util.parseISO8601Date
import com.mainstreetcode.teammate.util.prettyPrint
import com.mainstreetcode.teammate.util.processEmoji
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

/**
 * Event events
 */

class Event : EventEntity,
        TeamHost,
        Model<Event>,
        HeaderedModel<Event>,
        ListableModel<Event> {

    val markerOptions: MarkerOptions
        get() = MarkerOptions()
                .title(name.toString())
                .position(location!!)
                .snippet(locationName.toString())
                .icon(BitmapDescriptorFactory.fromBitmap(TextBitmapUtil.getBitmapMarker(team.sport.getEmoji())))

    constructor(
            id: String,
            gameId: String,
            imageUrl: String,
            name: CharSequence,
            notes: CharSequence,
            locationName: CharSequence,
            startDate: Date,
            endDate: Date,
            team: Team,
            location: LatLng?,
            visibility: Visibility,
            spots: Int
    ) : super(id, gameId, imageUrl, name, notes, locationName, startDate, endDate, team, location, visibility, spots)

    private constructor(`in`: Parcel) : super(`in`)

    override val isEmpty: Boolean
        get() = equals(empty())

    override val headerItem: Item<Event>
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.team_logo, Item.nullToEmpty(imageUrl), { this.imageUrl = it }, this)

    fun setName(game: Game) {
        setName(game.home.name.toString() + " Vs. " + game.away.name)
    }

    override fun asItems(): List<Item<Event>> = listOf(
            Item.text(holder[0], 0, Item.INPUT, R.string.event_name, Item.nullToEmpty(name), this::setName, this),
            Item.text(holder[1], 1, Item.VISIBILITY, R.string.event_visibility, visibility::code, this::setVisibility, this)
                    .textTransformer { value -> Config.visibilityFromCode(value.toString()).getName() },
            Item.number(holder[2], 2, Item.NUMBER, R.string.event_spots, spots::toString, this::setSpots, this),
            Item.text(holder[3], 3, Item.LOCATION, R.string.location, Item.nullToEmpty(locationName), this::setLocationName, this),
            Item.text(holder[4], 4, Item.DATE, R.string.start_date, startDate::prettyPrint, this::setStartDate, this),
            Item.text(holder[5], 5, Item.DATE, R.string.end_date, endDate::prettyPrint, this::setEndDate, this),
            Item.text(holder[6], 6, Item.TEXT, R.string.notes, Item.nullToEmpty(notes), this::setNotes, this)
    )

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is Event) id == other.id else name == other.name
                    && startDate == other.startDate && endDate == other.endDate
                    && locationName == other.locationName && imageUrl == other.imageUrl

    override fun hasMajorFields(): Boolean = areNotEmpty(id, name)

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun update(updated: Event) {
        this.id = updated.id
        this.name = updated.name
        this.notes = updated.notes
        this.spots = updated.spots
        this.gameId = updated.gameId
        this.imageUrl = updated.imageUrl
        this.endDate = updated.endDate
        this.startDate = updated.startDate
        this.location = updated.location
        this.locationName = updated.locationName
        this.visibility.update(updated.visibility)
        if (updated.team.hasMajorFields()) this.team.update(updated.team)
    }

    override fun compareTo(other: Event): Int {
        val startDateComparison = startDate.compareTo(other.startDate)
        val endDateComparison = endDate.compareTo(other.endDate)

        return when {
            startDateComparison != 0 -> startDateComparison
            endDateComparison != 0 -> endDateComparison
            else -> id.compareTo(other.id)
        }
    }

    fun updateTeam(team: Team) = this.team.update(team)

    fun setAddress(address: Address) {
        setLocationName(address.fullName)
        location = LatLng(address.latitude, address.longitude)
    }

//    internal fun setGame(game: Game) {
//        this.gameId = game.id
//    }

    override fun describeContents(): Int = 0

    class GsonAdapter : JsonSerializer<Event>, JsonDeserializer<Event> {

        override fun serialize(src: Event, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val serialized = JsonObject()

            serialized.addProperty(NAME_KEY, src.name.toString())
            serialized.addProperty(NOTES_KEY, src.notes.toString())
            serialized.addProperty(LOCATION_NAME_KEY, src.locationName.toString())
            serialized.addProperty(SPOTS_KEY, src.spots)
            serialized.addProperty(TEAM_KEY, src.team.id)
            serialized.addProperty(START_DATE_KEY, src.startDate.ISO8601Print())
            serialized.addProperty(END_DATE_KEY, src.endDate.ISO8601Print())
            if (src.gameId.isNotBlank()) serialized.addProperty(GAME, src.gameId)

            val visibilityCode = src.visibility.code
            if (!TextUtils.isEmpty(visibilityCode))
                serialized.addProperty(VISIBILITY_KEY, visibilityCode)

            if (src.location != null) {
                val coordinates = JsonArray()
                coordinates.add(src.location!!.longitude)
                coordinates.add(src.location!!.latitude)
                serialized.add(LOCATION_KEY, coordinates)
            }

            return serialized
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Event {
            if (json.isJsonPrimitive) {
                return Event(json.asString, "", "", "", "", "", Date(), Date(), Team.empty(), null, Visibility.empty(), DEFAULT_NUM_SPOTS)
            }

            val eventJson = json.asJsonObject

            val id = eventJson.asStringOrEmpty(ID_KEY)
            val gameId = eventJson.asStringOrEmpty(GAME)
            val name = eventJson.asStringOrEmpty(NAME_KEY)
            val notes = eventJson.asStringOrEmpty(NOTES_KEY)
            val imageUrl = eventJson.asStringOrEmpty(IMAGE_KEY)
            val visibilityCode = eventJson.asStringOrEmpty(VISIBILITY_KEY)
            val locationName = eventJson.asStringOrEmpty(LOCATION_NAME_KEY)
            val startDate = eventJson.asStringOrEmpty(START_DATE_KEY)
            val endDate = eventJson.asStringOrEmpty(END_DATE_KEY)
            var spots = eventJson.asFloatOrZero(SPOTS_KEY).toInt()

            if (spots == 0) spots = DEFAULT_NUM_SPOTS

            var team: Team? = context.deserialize<Team>(eventJson.get(TEAM_KEY), Team::class.java)
            val location = eventJson.parseCoordinates(LOCATION_KEY)
            val visibility = Config.visibilityFromCode(visibilityCode)

            if (team == null) team = Team.empty()

            return Event(id, gameId, imageUrl,
                    name.processEmoji(), notes.processEmoji(), locationName.processEmoji(),
                    startDate.parseISO8601Date(), endDate.parseISO8601Date(), team, location, visibility, spots)
        }

        companion object {

            private const val ID_KEY = "_id"
            private const val GAME = "game"
            private const val NAME_KEY = "name"
            private const val TEAM_KEY = "team"
            private const val NOTES_KEY = "notes"
            private const val IMAGE_KEY = "imageUrl"
            private const val VISIBILITY_KEY = "visibility"
            private const val LOCATION_NAME_KEY = "locationName"
            private const val START_DATE_KEY = "startDate"
            private const val END_DATE_KEY = "endDate"
            private const val LOCATION_KEY = "location"
            private const val SPOTS_KEY = "spots"
        }
    }

    companion object {

        const val PHOTO_UPLOAD_KEY = "event-photo"
        const val DEFAULT_NUM_SPOTS = 12

        @Ignore
        private val holder = IdCache(7)

        fun empty(): Event {
            val date = Date()
            return Event("", "", Config.getDefaultEventLogo(), "", "", "", date, date, Team.empty(), null, Visibility.empty(), DEFAULT_NUM_SPOTS)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(`in`: Parcel): Event = Event(`in`)

            override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
        }
    }
}
