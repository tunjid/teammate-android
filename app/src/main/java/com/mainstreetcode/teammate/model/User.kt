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
import android.text.TextUtils
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
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.ModelUtils
import com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING
import com.mainstreetcode.teammate.util.ModelUtils.areNotEmpty
import com.mainstreetcode.teammate.util.ModelUtils.asString
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type

class User : UserEntity,
        Competitive,
        Model<User>,
        HeaderedModel<User>,
        ListableModel<User> {

    @Ignore
    @Transient
    private var password: String? = null

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

    override val refType: String
        get() = COMPETITOR_TYPE

    override val name: CharSequence
        get() = "$firstName $lastName"

    override val isEmpty: Boolean
        get() = id.isBlank()

    override val headerItem: Item<User>
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, Item.nullToEmpty(imageUrl), { this.imageUrl = it }, this)

    override fun asItems(): List<Item<User>> = listOf(
            Item.text(holder.get(0), 0, Item.INPUT, R.string.first_name, Item.nullToEmpty(firstName), this::setFirstName, this),
            Item.text(holder.get(1), 1, Item.INPUT, R.string.last_name, Item.nullToEmpty(lastName), this::setLastName, this),
            Item.text(holder.get(2), 2, Item.INFO, R.string.screen_name, Item.nullToEmpty(screenName), { this.screenName = it }, this),
            Item.email(holder.get(3), 3, Item.INPUT, R.string.email, Item.nullToEmpty(primaryEmail), { this.primaryEmail = it }, this),
            Item.text(holder.get(4), 4, Item.ABOUT, R.string.user_about, Item.nullToEmpty(about), this::setAbout, this)
    )

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is User) id == other.id else firstName == other.firstName && lastName == other.lastName
                    && imageUrl == other.imageUrl

//    override fun getName(): CharSequence = "$firstName $lastName"

//    override fun getRefType(): String = COMPETITOR_TYPE

    override fun hasMajorFields(): Boolean = areNotEmpty(id, firstName, lastName)

    override fun getChangePayload(other: Differentiable?): Any? = other

//    override fun isEmpty(): Boolean = TextUtils.isEmpty(id)

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

    override fun compareTo(other: User): Int {
        val firstNameComparison = firstName.toString().compareTo(other.firstName.toString())
        val lastNameComparison = lastName.toString().compareTo(other.lastName.toString())

        return when {
            firstNameComparison != 0 -> firstNameComparison
            lastNameComparison != 0 -> lastNameComparison
            else -> id.compareTo(other.id)
        }
    }

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

            val id = asString(UID_KEY, userObject)
            val imageUrl = asString(IMAGE_KEY, userObject)
            val screenName = asString(SCREEN_NAME, userObject)
            val primaryEmail = asString(PRIMARY_EMAIL_KEY, userObject)
            val firstName = asString(FIRST_NAME_KEY, userObject)
            val lastName = asString(LAST_NAME_KEY, userObject)
            val about = asString(ABOUT_KEY, userObject)

            return User(id, imageUrl, screenName, primaryEmail,
                    ModelUtils.processString(firstName), ModelUtils.processString(lastName), ModelUtils.processString(about))
        }

        override fun serialize(src: User, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val user = JsonObject()
            user.addProperty(FIRST_NAME_KEY, src.firstName.toString())
            user.addProperty(LAST_NAME_KEY, src.lastName.toString())
            user.addProperty(PRIMARY_EMAIL_KEY, src.primaryEmail)
            user.addProperty(ABOUT_KEY, src.about.toString())
            if (!TextUtils.isEmpty(src.screenName)) user.addProperty(SCREEN_NAME, src.screenName)

            if (!TextUtils.isEmpty(src.password)) user.addProperty(PASSWORD_KEY, src.password)

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
        private val holder = IdCache.cache(5)

        fun empty(): User = User("", Config.getDefaultUserAvatar(), "", "", "", "", "")

        fun empty(id: String) = empty().apply { this.id = id }

        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(`in`: Parcel): User = User(`in`)

            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }
}
