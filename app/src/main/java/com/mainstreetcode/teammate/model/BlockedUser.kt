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
import com.mainstreetcode.teammate.model.enums.BlockReason
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.noOp
import com.mainstreetcode.teammate.util.parseISO8601Date
import java.lang.reflect.Type
import java.util.*

class BlockedUser private constructor(
        private var id: String,
        override val user: User,
        override val team: Team,
        val reason: BlockReason,
        val created: Date
) :
        UserHost,
        TeamHost,
        Model<BlockedUser>,
        HeaderedModel<BlockedUser>,
        ListableModel<BlockedUser> {

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readValue(User::class.java.classLoader) as User,
            parcel.readValue(Team::class.java.classLoader) as Team,
            Config.reasonFromCode(parcel.readString()!!),
            Date(parcel.readLong())
    )

    override fun getId(): String = id

    override val isEmpty: Boolean
        get() = id.isBlank()

    override val imageUrl: String
        get() = user.imageUrl

    override val headerItem: Item<BlockedUser>
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, Item.nullToEmpty(user.imageUrl), CharSequence::noOp)

    override fun asItems(): List<Item<BlockedUser>> = listOf(
            Item.text(holder[0], 0, Item.INPUT, R.string.first_name, user::firstName, user::setFirstName),
            Item.text(holder[1], 1, Item.INPUT, R.string.last_name, user::lastName, user::setLastName),
            Item.text(holder[2], 2, Item.ROLE, R.string.team_role, reason::code, CharSequence::noOp)
                    .textTransformer { value -> Config.reasonFromCode(value.toString()).getName() })

    override fun update(updated: BlockedUser) {
        this.id = updated.id
        this.reason.update(updated.reason)
        if (updated.user.hasMajorFields()) this.user.update(updated.user)
        if (updated.team.hasMajorFields()) this.team.update(updated.team)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockedUser) return false
        val that = other as BlockedUser?
        return id == that!!.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun compareTo(other: BlockedUser): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeValue(user)
        parcel.writeValue(team)
        parcel.writeString(reason.code)
        parcel.writeLong(created.time)
    }

    override fun describeContents(): Int = 0

    class GsonAdapter : JsonSerializer<BlockedUser>, JsonDeserializer<BlockedUser> {

        override fun serialize(src: BlockedUser, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val serialized = JsonObject()

            if (!src.isEmpty) serialized.addProperty(UID_KEY, src.getId())
            serialized.addProperty(REASON_KEY, src.reason.code)
            serialized.addProperty(USER_KEY, src.user.id)
            serialized.addProperty(TEAM_KEY, src.team.id)

            return serialized
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BlockedUser {
            if (json.isJsonPrimitive) {
                return BlockedUser(json.asString, User.empty(), Team.empty(), BlockReason.empty(), Date())
            }

            val jsonObject = json.asJsonObject

            val id = jsonObject.asStringOrEmpty(UID_KEY)
            val reasonCode = jsonObject.asStringOrEmpty(REASON_KEY)
            val reason = Config.reasonFromCode(reasonCode)
            val team = context.deserialize<Team>(jsonObject.get(TEAM_KEY), Team::class.java)
            val user = context.deserialize<User>(jsonObject.get(USER_KEY), User::class.java)
            val created = jsonObject.asStringOrEmpty(CREATED_KEY).parseISO8601Date()

            return BlockedUser(id, user, team, reason, created)
        }

        companion object {

            private const val UID_KEY = "_id"
            private const val USER_KEY = "user"
            private const val TEAM_KEY = "team"
            private const val REASON_KEY = "reason"
            private const val CREATED_KEY = "created"
        }
    }

    companion object {

        @Ignore
        private val holder = IdCache(3)

        fun block(user: User, team: Team, reason: BlockReason): BlockedUser =
                BlockedUser("", user, team, reason, Date())

        @JvmField
        val CREATOR: Parcelable.Creator<BlockedUser> = object : Parcelable.Creator<BlockedUser> {
            override fun createFromParcel(parcel: Parcel): BlockedUser = BlockedUser(parcel)

            override fun newArray(size: Int): Array<BlockedUser?> = arrayOfNulls(size)
        }
    }

}
