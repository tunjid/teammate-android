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

package com.mainstreetcode.teammate.notifications

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.ModelStub
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type

/**
 * Notifications from a user's feed
 */

@Suppress("UNCHECKED_CAST")
inline fun <reified T> FeedItem<*>.isOf() where T : Any, T : Model<T> =
        if (model is T) this as FeedItem<T>
        else null

class FeedItem<T : Model<T>> : Parcelable, Differentiable, Comparable<FeedItem<*>> {

    private val action: String
    val title: String
    val body: String
    val type: String
    val model: T
    val itemClass: Class<T>

    val imageUrl: String
        get() = model.imageUrl

    val isDeleteAction: Boolean
        get() = !TextUtils.isEmpty(action) && "DELETE" == action

    private constructor(action: String, title: String, body: String, type: String, model: T, itemClass: Class<T>) {
        this.action = action
        this.title = title
        this.body = body
        this.type = type
        this.model = model
        this.itemClass = itemClass
    }

    @Suppress("UNCHECKED_CAST")
    private constructor(`in`: Parcel) {
        action = `in`.readString()!!
        title = `in`.readString()!!
        body = `in`.readString()!!
        type = `in`.readString()!!
        itemClass = forType(type)
        model = `in`.readValue(itemClass.classLoader) as T
    }

    override fun getId(): String = model.id

    override fun compareTo(other: FeedItem<*>): Int = FunctionalDiff.COMPARATOR.compare(model, other.model)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeedItem<*>) return false

        return model == other.model
    }

    override fun hashCode(): Int = model.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(action)
        dest.writeString(title)
        dest.writeString(body)
        dest.writeString(type)
        dest.writeValue(model)
    }

    class GsonAdapter<T : Model<T>> : JsonDeserializer<FeedItem<T>> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FeedItem<T> {
            val feedItemJson = json.asJsonObject

            val action = feedItemJson.asStringOrEmpty(ACTION_KEY)
            val type = feedItemJson.asStringOrEmpty(TYPE_KEY)
            val title = feedItemJson.asStringOrEmpty(TITLE_KEY)
            val body = feedItemJson.asStringOrEmpty(BODY_KEY)
            val itemClass = forType<T>(type)

            var modelElement = feedItemJson.get(MODEL_KEY)

            if (modelElement.isJsonPrimitive) {
                val modelId = modelElement.asString
                val modelBody = JsonObject()

                modelBody.addProperty(MODEL_ID_KEY, modelId)
                modelElement = modelBody
            }

            val model = context.deserialize<T>(modelElement, itemClass)
            return FeedItem(action, title, body, type, model, itemClass)
        }

        companion object {

            private const val ACTION_KEY = "action"
            private const val TYPE_KEY = "type"
            private const val TITLE_KEY = "title"
            private const val BODY_KEY = "body"
            private const val MODEL_KEY = "model"
            private const val MODEL_ID_KEY = "_id"
        }
    }

    companion object {

        internal const val JOIN_REQUEST = "join-request"
        internal const val EVENT = "event"
        internal const val TEAM = "team"
        internal const val ROLE = "role"
        internal const val CHAT = "team-chat"
        internal const val MEDIA = "team-media"
        internal const val TOURNAMENT = "tournament"
        internal const val COMPETITOR = "competitor"
        internal const val GAME = "game"

        private val gson = TeammateService.getGson()

        fun <T : Model<T>> fromNotification(message: RemoteMessage): FeedItem<T>? {
            val data = message.data
            if (data == null || data.isEmpty()) return null

            return try {
                gson.fromJson<FeedItem<T>>(gson.toJson(data), FeedItem::class.java)
            } catch (e: Exception) {
                Logger.log("FeedItem", "Failed to parse feed item", e)
                null
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<FeedItem<*>> = object : Parcelable.Creator<FeedItem<*>> {
            override fun createFromParcel(`in`: Parcel): FeedItem<*> = FeedItem<ModelStub>(`in`)

            override fun newArray(size: Int): Array<FeedItem<*>?> = arrayOfNulls(size)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T> forType(type: String?): Class<T> = when (type ?: "") {
            JOIN_REQUEST -> JoinRequest::class.java
            TOURNAMENT -> Tournament::class.java
            COMPETITOR -> Competitor::class.java
            EVENT -> Event::class.java
            TEAM -> Team::class.java
            ROLE -> Role::class.java
            CHAT -> Chat::class.java
            GAME -> Game::class.java
            MEDIA -> Media::class.java
            else -> Any::class.java
        } as Class<T>
    }
}
