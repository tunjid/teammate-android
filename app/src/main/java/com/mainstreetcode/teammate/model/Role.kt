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
import com.mainstreetcode.teammate.model.enums.Position
import com.mainstreetcode.teammate.persistence.entity.RoleEntity
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.parseDate
import com.tunjid.androidbootstrap.core.text.SpanBuilder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

/**
 * Roles on a team
 */

class Role : RoleEntity,
        UserHost,
        TeamHost,
        HeaderedModel<Role>,
        ListableModel<Role>,
        TeamMemberModel<Role> {

    constructor(
            id: String,
            imageUrl: String,
            nickname: String,
            position: Position,
            team: Team,
            user: User,
            created: Date
    ) : super(id, imageUrl, nickname, position, team, user, created)

    private constructor(`in`: Parcel) : super(`in`)

    val isPrivilegedRole: Boolean
        get() {
            val positionCode = position.code
            return !TextUtils.isEmpty(positionCode) && !isEmpty && Config.getPrivileged().contains(positionCode)
        }

    val title: CharSequence
        get() = if (!TextUtils.isEmpty(nickname)) SpanBuilder.of(user.firstName)
                .appendNewLine().append("\"" + nickname + "\"")
                .build() else user.firstName

    override val isEmpty: Boolean
        get() = id.isBlank()

    override val headerItem: Item<Role>
        get() = Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, Item.nullToEmpty(imageUrl), { this.imageUrl = it }, this)

    override fun asItems(): List<Item<Role>> = listOf(
            Item.text(holder.get(0), 0, Item.INPUT, R.string.first_name, user::firstName, user::setFirstName, this),
            Item.text(holder.get(1), 1, Item.INPUT, R.string.last_name, user::lastName, user::setLastName, this),
            Item.text(holder.get(2), 2, Item.NICKNAME, R.string.nickname, this::nickname, { this.nickname = it }, this),
            Item.text(holder.get(3), 3, Item.ABOUT, R.string.user_about, user::about, Item.IGNORE_SET, this),
            Item.text(holder.get(4), 4, Item.ROLE, R.string.team_role, position::code, this::setPosition, this)
                    .textTransformer { value -> Config.positionFromCode(value.toString()).getName() }
    )

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is Role) id == other.id else position == other.position
                    && user.areContentsTheSame(other.user)
                    && team.areContentsTheSame(other.team)

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun update(updated: Role) {
        this.id = updated.id
        this.imageUrl = updated.imageUrl
        this.nickname = updated.nickname
        this.position.update(updated.position)
        if (updated.team.hasMajorFields()) this.team.update(updated.team)
        if (updated.user.hasMajorFields()) this.user.update(updated.user)
    }

    override fun compareTo(other: Role): Int {
        val roleComparison = position.code.compareTo(other.position.code)
        val userComparison = user.compareTo(other.user)
        val teamComparison = team.compareTo(other.team)

        return when {
            roleComparison != 0 -> roleComparison
            userComparison != 0 -> userComparison
            teamComparison != 0 -> teamComparison
            else -> id.compareTo(other.id)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = super.writeToParcel(dest, flags)

    class GsonAdapter : JsonSerializer<Role>, JsonDeserializer<Role> {

        override fun serialize(src: Role, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val serialized = JsonObject()

            serialized.addProperty(ID_KEY, src.id)
            serialized.addProperty(IMAGE_KEY, src.imageUrl)
            serialized.addProperty(NICK_NAME_KEY, src.nickname)
            serialized.add(USER_KEY, context.serialize(src.user))

            val positionCode = src.position.code
            if (!TextUtils.isEmpty(positionCode)) serialized.addProperty(NAME_KEY, positionCode)

            return serialized
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Role {

            val roleJson = json.asJsonObject

            val id = roleJson.asStringOrEmpty(ID_KEY)
            val imageUrl = roleJson.asStringOrEmpty(IMAGE_KEY)
            val nickname = roleJson.asStringOrEmpty(NICK_NAME_KEY)
            val positionName = roleJson.asStringOrEmpty(NAME_KEY)

            val position = Config.positionFromCode(positionName)
            val team = context.deserialize<Team>(roleJson.get(TEAM_KEY), Team::class.java)
            var user: User? = context.deserialize<User>(roleJson.get(USER_KEY), User::class.java)
            val created = parseDate(roleJson.asStringOrEmpty(CREATED_KEY))

            if (user == null) user = User.empty()

            return Role(id, imageUrl, nickname, position, team, user, created)
        }

        companion object {

            private const val ID_KEY = "_id"
            private const val NAME_KEY = "name"
            private const val USER_KEY = "user"
            private const val TEAM_KEY = "team"
            private const val IMAGE_KEY = "imageUrl"
            private const val CREATED_KEY = "created"
            private const val NICK_NAME_KEY = "nickname"
        }
    }

    companion object {

        const val PHOTO_UPLOAD_KEY = "role-photo"

        @Ignore
        private val holder = IdCache.cache(5)

        fun empty(): Role =
                Role("", Config.getDefaultUserAvatar(), "", Position.empty(), Team.empty(), User.empty(), Date())

        @JvmField
        val CREATOR: Parcelable.Creator<Role> = object : Parcelable.Creator<Role> {
            override fun createFromParcel(`in`: Parcel): Role = Role(`in`)

            override fun newArray(size: Int): Array<Role?> = arrayOfNulls(size)
        }
    }
}
