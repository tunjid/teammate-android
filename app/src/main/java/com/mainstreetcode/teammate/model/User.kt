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
import com.mainstreetcode.teammate.persistence.entity.UserEntity
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.areNotEmpty
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.isNotNullOrBlank
import com.mainstreetcode.teammate.util.processEmoji
import com.tunjid.androidx.recyclerview.diff.Differentiable
import java.lang.reflect.Type

class User : UserEntity,
        Competitive,
        Model<User>,
        HeaderedModel<User>,
        ListableModel<User> {

    constructor(
            id: String,
            imageUrl: String,
            screenName: String,
            primaryEmail: String,
            firstName: CharSequence,
            lastName: CharSequence,
            about: CharSequence
    ) : super(id, imageUrl, screenName, primaryEmail, firstName, lastName, about)

    private constructor(`in`: Parcel) : super(`in`)

    @Ignore
    @Transient
    private var password: String? = null

    override val name: CharSequence
        get() = "$firstName $lastName"

    override val refType: String
        get() = COMPETITOR_TYPE

    override val isEmpty: Boolean
        get() = id.isBlank()

    override val headerItem: Item
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, Item.nullToEmpty(imageUrl), { this.imageUrl = it })

    override fun asItems(): List<Item> = listOf(
            Item.text(holder[0], 0, Item.INPUT, R.string.first_name, Item.nullToEmpty(firstName), this::setFirstName),
            Item.text(holder[1], 1, Item.INPUT, R.string.last_name, Item.nullToEmpty(lastName), this::setLastName),
            Item.text(holder[2], 2, Item.INFO, R.string.screen_name, Item.nullToEmpty(screenName), { this.screenName = it }),
            Item.email(holder[3], 3, Item.INPUT, R.string.email, Item.nullToEmpty(primaryEmail), { this.primaryEmail = it }),
            Item.text(holder[4], 4, Item.ABOUT, R.string.user_about, Item.nullToEmpty(about), this::setAbout)
    )

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is User) diffId == other.diffId else firstName == other.firstName && lastName == other.lastName
                    && imageUrl == other.imageUrl

    override fun hasMajorFields(): Boolean = areNotEmpty(id, firstName, lastName)

    override fun getChangePayload(other: Differentiable): Any? = other

    override fun update(updated: User) {
        this.id = updated.id
        this.about = updated.about
        this.firstName = updated.firstName
        this.lastName = updated.lastName
        this.imageUrl = updated.imageUrl
        this.screenName = updated.screenName
        this.primaryEmail = updated.primaryEmail
    }

    override fun update(other: Competitive): Boolean {
        if (other !is User) return false
        update(other)
        return true
    }

    override fun makeCopy(): Competitive {
        val copy = empty()
        copy.update(this)
        return copy
    }

    override fun compareTo(other: User): Int =
            compareValuesBy(this, other, { it.firstName.toString() }, { it.lastName.toString() }, User::id)

    fun setPassword(password: String) {
        this.password = password
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = super.writeToParcel(dest, flags)

    class GsonAdapter : JsonSerializer<User>, JsonDeserializer<User> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): User {
            if (json.isJsonPrimitive) {
                return User(json.asString, "", "", "", "", "", "")
            }

            val userObject = json.asJsonObject

            val id = userObject.asStringOrEmpty(UID_KEY)
            val imageUrl = userObject.asStringOrEmpty(IMAGE_KEY)
            val screenName = userObject.asStringOrEmpty(SCREEN_NAME)
            val primaryEmail = userObject.asStringOrEmpty(PRIMARY_EMAIL_KEY)
            val firstName = userObject.asStringOrEmpty(FIRST_NAME_KEY)
            val lastName = userObject.asStringOrEmpty(LAST_NAME_KEY)
            val about = userObject.asStringOrEmpty(ABOUT_KEY)

            return User(id, imageUrl, screenName, primaryEmail,
                    firstName.processEmoji(), lastName.processEmoji(), about.processEmoji())
        }

        override fun serialize(src: User, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val user = JsonObject()
            user.addProperty(FIRST_NAME_KEY, src.firstName.toString())
            user.addProperty(LAST_NAME_KEY, src.lastName.toString())
            user.addProperty(PRIMARY_EMAIL_KEY, src.primaryEmail)
            user.addProperty(ABOUT_KEY, src.about.toString())
            if (src.screenName.isNotBlank()) user.addProperty(SCREEN_NAME, src.screenName)

            if (src.password.isNotNullOrBlank()) user.addProperty(PASSWORD_KEY, src.password)

            return user
        }

        companion object {

            private const val UID_KEY = "_id"
            private const val LAST_NAME_KEY = "lastName"
            private const val FIRST_NAME_KEY = "firstName"
            private const val SCREEN_NAME = "screenName"
            private const val IMAGE_KEY = "imageUrl"
            private const val PRIMARY_EMAIL_KEY = "primaryEmail"
            private const val ABOUT_KEY = "about"
            private const val PASSWORD_KEY = "password"
        }
    }

    companion object {

        const val PHOTO_UPLOAD_KEY = "user-photo"
        const val COMPETITOR_TYPE = "user"
        @Ignore
        private val holder = IdCache(5)

        fun empty(): User = User("", Config.getDefaultUserAvatar(), "", "", "", "", "")

        fun empty(id: String) = empty().apply { this.id = id }

        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(`in`: Parcel): User = User(`in`)

            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }
}
