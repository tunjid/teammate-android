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
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.persistence.entity.GuestEntity
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.*
import com.mainstreetcode.teammate.util. EMPTY_STRING
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

class Guest : GuestEntity,
        UserHost,
        TeamHost,
        Model<Guest>,
        HeaderedModel<Guest>,
        ListableModel<Guest> {

    constructor(id: String, user: User, event: Event, created: Date, attending: Boolean) : super(id, user, event, created, attending)

    private constructor(`in`: Parcel) : super(`in`)

    override val imageUrl: String
        get() = user.imageUrl

    override val team: Team
        get() = event.team

    override val isEmpty: Boolean
        get() = id.isBlank()

    override val headerItem: Item<Guest>
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, user::imageUrl, Item.IGNORE_SET, this)

    override fun asItems(): List<Item<Guest>> = listOf(
            Item.text(holder.get(0), 0, Item.INPUT, R.string.first_name, user::firstName, Item.IGNORE_SET, this),
            Item.text(holder.get(1), 1, Item.INPUT, R.string.last_name, user::lastName, Item.IGNORE_SET, this),
            Item.email(holder.get(2), 2, Item.ABOUT, R.string.user_about, user::about, Item.IGNORE_SET, this)
    )

    override fun hasMajorFields(): Boolean = user.hasMajorFields()

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is Guest) id == other.id else isAttending == other.isAttending

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun update(updated: Guest) {
        id = updated.id
        isAttending = updated.isAttending
        created = updated.created
        if (updated.user.hasMajorFields()) user.update(updated.user)
    }

    override fun compareTo(other: Guest): Int = created.compareTo(other.created)

    class GsonAdapter : JsonSerializer<Guest>, JsonDeserializer<Guest> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Guest {

            val teamJson = json.asJsonObject

            val id = teamJson.asStringOrEmpty(ID_KEY)
            var user: User? = context.deserialize<User>(teamJson.get(USER_KEY), User::class.java)
            val event = context.deserialize<Event>(teamJson.get(EVENT_KEY), Event::class.java)
            val created = parseDate(teamJson.asStringOrEmpty(DATE_KEY))
            val attending = teamJson.get(ATTENDING_KEY).asBoolean

            if (user == null) user = User.empty()

            return Guest(id, user, event, created, attending)
        }

        override fun serialize(src: Guest, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val guest = JsonObject()
            guest.addProperty(USER_KEY, src.user.id)
            guest.addProperty(EVENT_KEY, src.event.id)
            guest.addProperty(ATTENDING_KEY, src.isAttending)

            return guest
        }

        companion object {

            private const val ID_KEY = "_id"
            private const val USER_KEY = "user"
            private const val EVENT_KEY = "event"
            private const val DATE_KEY = "created"
            private const val ATTENDING_KEY = "attending"
        }
    }

    companion object {

        @Ignore
        private val holder = IdCache.cache(3)

        fun forEvent(event: Event, attending: Boolean): Guest =
                Guest("", User.empty(), event, Date(), attending)

        @JvmField
        val CREATOR: Parcelable.Creator<Guest> = object : Parcelable.Creator<Guest> {
            override fun createFromParcel(`in`: Parcel): Guest = Guest(`in`)

            override fun newArray(size: Int): Array<Guest?> = arrayOfNulls(size)
        }
    }
}
