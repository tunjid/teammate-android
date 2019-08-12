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
import androidx.room.Ignore
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.enums.Sport
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.dateFormatter
import com.mainstreetcode.teammate.util.asIntOrFalse
import com.mainstreetcode.teammate.util.parseDate
import com.mainstreetcode.teammate.util.prettyPrinter
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit

class EventSearchRequest private constructor(
        private var distance: Int,
        sport: Sport,
        var location: LatLng?,
        private var startDate: Date?,
        private var endDate: Date?
) : ListableModel<EventSearchRequest> {

    private var address: Address? = null
    var sport: Sport
        private set

    private val items: List<Item<EventSearchRequest>>

    private val sportName: CharSequence
        get() = sport.getName()

    init {
        this.sport = sport
        items = buildItems()
    }

    fun setAddress(address: Address) {
        this.address = address
        items[0].setValue(getAddress())
    }

    fun setDistance(distance: String) {
        this.distance = distance.asIntOrFalse()
        items[1].setValue(getDistance())
    }

    private fun setSport(sport: String) {
        this.sport = Config.sportFromCode(sport)
    }

    private fun setStartDate(startDate: String) {
        this.startDate = parseDate(startDate, prettyPrinter)
    }

    private fun setEndDate(endDate: String) {
        this.endDate = parseDate(endDate, prettyPrinter)
    }

    private fun getAddress(): CharSequence =
            if (address == null) "" else address!!.locality + ", " + address!!.adminArea

    private fun getDistance(): CharSequence =
            App.getInstance().getString(R.string.event_public_distance, distance)

    private fun getStartDate(): CharSequence = prettyPrinter.format(startDate!!)

    private fun getEndDate(): CharSequence = prettyPrinter.format(endDate!!)

    private fun buildItems(): List<Item<EventSearchRequest>> = listOf(
            Item.text(holder.get(0), 0, Item.LOCATION, R.string.location, this::getAddress, Item.IGNORE_SET, this),
            Item.text(holder.get(1), 1, Item.INFO, R.string.event_distance, this::getDistance, Item.IGNORE_SET, this),
            Item.text(holder.get(2), 2, Item.SPORT, R.string.team_sport, this::sportName, this::setSport, this)
                    .textTransformer { value -> Config.sportFromCode(value.toString()).getName() },
            Item.text(holder.get(3), 3, Item.DATE, R.string.start_date, this::getStartDate, this::setStartDate, this),
            Item.text(holder.get(4), 4, Item.DATE, R.string.end_date, this::getEndDate, this::setEndDate, this)
    )

    override fun asItems(): List<Item<EventSearchRequest>> = items

    class GsonAdapter : JsonSerializer<EventSearchRequest> {

        override fun serialize(src: EventSearchRequest, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val serialized = JsonObject()

            serialized.addProperty(DISTANCE_KEY, src.distance)

            if (!src.sport.isInvalid) serialized.addProperty(SPORT_KEY, src.sport.code)

            if (src.startDate != null) {
                serialized.addProperty(START_DATE_KEY, dateFormatter.format(src.startDate!!))
                if (src.endDate != null) serialized.addProperty(END_DATE_KEY, dateFormatter.format(src.endDate!!))
            }

            if (src.location != null) {
                val coordinates = JsonArray()
                coordinates.add(src.location!!.longitude)
                coordinates.add(src.location!!.latitude)
                serialized.add(LOCATION_KEY, coordinates)
            }

            return serialized
        }

        companion object {

            private const val DISTANCE_KEY = "maxDistance"
            private const val SPORT_KEY = "sport"
            private const val LOCATION_KEY = "location"
            private const val START_DATE_KEY = "startDate"
            private const val END_DATE_KEY = "endDate"
        }

    }

    companion object {

        @Ignore
        private val holder = IdCache.cache(5)

        fun empty(): EventSearchRequest =
                EventSearchRequest(5, Sport.empty(), null, Date(), Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)))
    }
}
