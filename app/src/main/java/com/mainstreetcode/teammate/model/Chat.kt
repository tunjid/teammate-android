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


import android.annotation.SuppressLint
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
import com.mainstreetcode.teammate.persistence.entity.ChatEntity
import com.mainstreetcode.teammate.util.*
import com.mainstreetcode.teammate.util.ObjectId
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class Chat : ChatEntity, TeamHost, Parcelable, Model<Chat> {

    @Ignore
    @Transient
    private var isSuccessful = true

    val createdDate: String
        get() = CHAT_DATE_FORMAT.format(created)

    constructor(hiddenId: String, kind: String,
                content: CharSequence,
                user: User, hiddenTeam: Team, created: Date) : super(hiddenId, kind, content, user, hiddenTeam, created)

    private constructor(`in`: Parcel) : super(`in`)

    override fun getId(): String = hiddenId

    override val team: Team
        get() = hiddenTeam

    override val imageUrl: String
        get() = user.imageUrl

    override val isEmpty: Boolean
        get() = !isSuccessful

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is Chat) id == other.id else content == other.content && user.areContentsTheSame(other.user)

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun update(updated: Chat) {
        hiddenId = updated.hiddenId
        kind = updated.kind
        content = updated.content
        created = updated.created
        isSuccessful = updated.isSuccessful

        user.update(updated.user)
        hiddenTeam.update(updated.team)
    }

    override fun compareTo(other: Chat): Int = created.compareTo(other.created)

    open class GsonAdapter : JsonSerializer<Chat>, JsonDeserializer<Chat> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Chat {

            val teamJson = json.asJsonObject

            val id = teamJson.asStringOrEmpty(UID_KEY)
            val kind = teamJson.asStringOrEmpty(KIND_KEY)
            val content = teamJson.asStringOrEmpty(CONTENT_KEY)

            val user: User = context.deserialize<User>(teamJson.get(USER_KEY), User::class.java)
                    ?: User.empty()
            val team: Team = context.deserialize<Team>(teamJson.get(TEAM_KEY), Team::class.java)
                    ?: Team.empty()
            val created = parseDate(teamJson.asStringOrEmpty(DATE_KEY))

            return Chat(id, kind, content, user, team, created)
        }

        override fun serialize(src: Chat, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val team = JsonObject()
            team.addProperty(KIND_KEY, src.kind)
            team.addProperty(USER_KEY, src.user.id)
            team.addProperty(TEAM_KEY, src.team.id)
            team.addProperty(CONTENT_KEY, src.content.toString())

            return team
        }

        companion object {

            private const val UID_KEY = "_id"
            private const val KIND_KEY = "kind"
            private const val CONTENT_KEY = "content"
            private const val USER_KEY = "user"
            private const val TEAM_KEY = "team"
            private const val DATE_KEY = "created"
        }
    }

    companion object {

        @SuppressLint("SimpleDateFormat")
        private val CHAT_DATE_FORMAT = SimpleDateFormat("h:mm a")
        private const val KIND_TEXT = "text"

        fun chat(content: CharSequence, user: User, team: Team): Chat {
            val chat = Chat(ObjectId().toHexString(), KIND_TEXT, content, user, team, Date())
            chat.isSuccessful = false
            return chat
        }

        fun empty(): Chat = chat("", User.empty(), Team.empty())

        @JvmField
        val CREATOR: Parcelable.Creator<Chat> = object : Parcelable.Creator<Chat> {
            override fun createFromParcel(`in`: Parcel): Chat = Chat(`in`)

            override fun newArray(size: Int): Array<Chat?> = arrayOfNulls(size)
        }
    }
}
