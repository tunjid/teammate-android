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
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity
import com.mainstreetcode.teammate.util.EMPTY_STRING
import com.mainstreetcode.teammate.util.IdCache
import com.mainstreetcode.teammate.util.asBooleanOrFalse
import com.mainstreetcode.teammate.util.asStringOrEmpty
import com.mainstreetcode.teammate.util.parseISO8601Date
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

/**
 * Join request for a [Team]
 */

class JoinRequest : JoinRequestEntity,
        UserHost,
        TeamHost,
        HeaderedModel<JoinRequest>,
        ListableModel<JoinRequest>,
        TeamMemberModel<JoinRequest> {

    constructor(
            teamApproved: Boolean,
            userApproved: Boolean,
            id: String,
            position: Position,
            team: Team,
            user: User,
            created: Date
    ) : super(teamApproved, userApproved, id, position, team, user, created)

    private constructor(`in`: Parcel) : super(`in`)

    override val imageUrl: String
        get() = user.imageUrl

    override val isEmpty: Boolean
        get() = id.isBlank()

    override val headerItem: Item<JoinRequest>
        get() {
            val image: RemoteImage = if (isUserApproved) team else user
            return Item.text(EMPTY_STRING, 0, Item.IMAGE, R.string.profile_picture, { image.imageUrl }, Item.IGNORE_SET, this)
        }

    override fun asItems(): List<Item<JoinRequest>> = listOf(
            Item.text(holder[0], 0, Item.INPUT, R.string.first_name, user::firstName, user::setFirstName, this),
            Item.text(holder[1], 1, Item.INPUT, R.string.last_name, user::lastName, user::setLastName, this),
            Item.text(holder[2], 2, Item.ABOUT, R.string.user_about, user::about, Item.IGNORE_SET, this),
            Item.email(holder[3], 3, Item.INPUT, R.string.email, user::primaryEmail, { user.primaryEmail = it }, this),
            // END USER ITEMS
            Item.text(holder[4], 4, Item.ROLE, R.string.team_role, position::code, this::setPosition, this)
                    .textTransformer { value -> Config.positionFromCode(value.toString()).getName() },
            // START TEAM ITEMS
            Item.text(holder[5], 5, Item.INPUT, R.string.team_name, team::name, Item.IGNORE_SET, this),
            Item.text(holder[6], 6, Item.SPORT, R.string.team_sport, team.sport::code, Item.IGNORE_SET, this).textTransformer { value -> Config.sportFromCode(value.toString()).getName() },
            Item.text(holder[7], 7, Item.CITY, R.string.city, team::city, Item.IGNORE_SET, this),
            Item.text(holder[8], 8, Item.STATE, R.string.state, team::state, Item.IGNORE_SET, this),
            Item.text(holder[9], 9, Item.ZIP, R.string.zip, team::zip, Item.IGNORE_SET, this),
            Item.text(holder.get(10), 10, Item.DESCRIPTION, R.string.team_description, team::description, Item.IGNORE_SET, this),
            Item.number(holder.get(11), 11, Item.NUMBER, R.string.team_min_age, team.minAge::toString, Item.IGNORE_SET, this),
            Item.number(holder.get(12), 12, Item.NUMBER, R.string.team_max_age, team.maxAge::toString, Item.IGNORE_SET, this)
    )

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is JoinRequest) id == other.id else position == other.position && user.areContentsTheSame(other.user)

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun update(updated: JoinRequest) {
        this.isTeamApproved = updated.isTeamApproved
        this.isUserApproved = updated.isUserApproved
        this.id = updated.id

        position.update(updated.position)
        if (updated.team.hasMajorFields()) team.update(updated.team)
        if (updated.user.hasMajorFields()) user.update(updated.user)
    }

    override fun compareTo(other: JoinRequest): Int {
        val roleComparison = position.code.compareTo(other.position.code)
        val userComparison = user.compareTo(other.user)

        return when {
            roleComparison != 0 -> roleComparison
            userComparison != 0 -> userComparison
            else -> id.compareTo(other.id)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
    }

    class GsonAdapter : JsonSerializer<JoinRequest>, JsonDeserializer<JoinRequest> {

        override fun serialize(src: JoinRequest, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val result = JsonObject()

            result.addProperty(TEAM_KEY, src.team.id)
            result.addProperty(TEAM_APPROVAL_KEY, src.isTeamApproved)
            result.addProperty(USER_APPROVAL_KEY, src.isUserApproved)

            val user = src.user

            if (src.isTeamApproved) {
                result.addProperty(USER_FIRST_NAME_KEY, user.firstName.toString())
                result.addProperty(USER_LAST_NAME_KEY, user.lastName.toString())
                result.addProperty(USER_PRIMARY_EMAIL_KEY, user.primaryEmail)
            } else {
                result.addProperty(USER_KEY, src.user.id)
            }

            val positionCode = src.position.code
            if (!TextUtils.isEmpty(positionCode)) result.addProperty(NAME_KEY, positionCode)

            return result
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): JoinRequest {

            val requestJson = json.asJsonObject

            val teamApproved = requestJson.asBooleanOrFalse(TEAM_APPROVAL_KEY)
            val userApproved = requestJson.asBooleanOrFalse(USER_APPROVAL_KEY)

            val id = requestJson.asStringOrEmpty(ID_KEY)
            val positionName = requestJson.asStringOrEmpty(NAME_KEY)
            val position = Config.positionFromCode(positionName)

            var team: Team? = context.deserialize<Team>(requestJson.get(TEAM_KEY), Team::class.java)
            var user: User? = context.deserialize<User>(requestJson.get(USER_KEY), User::class.java)
            val created = requestJson.asStringOrEmpty(CREATED_KEY).parseISO8601Date()

            if (team == null) team = Team.empty()
            if (user == null) user = User.empty()

            return JoinRequest(teamApproved, userApproved, id, position, team, user, created)
        }

        companion object {

            private const val ID_KEY = "_id"
            private const val NAME_KEY = "roleName"
            private const val USER_KEY = "user"
            private const val USER_FIRST_NAME_KEY = "firstName"
            private const val USER_LAST_NAME_KEY = "lastName"
            private const val USER_PRIMARY_EMAIL_KEY = "primaryEmail"
            private const val TEAM_KEY = "team"
            private const val TEAM_APPROVAL_KEY = "teamApproved"
            private const val USER_APPROVAL_KEY = "userApproved"
            private const val CREATED_KEY = "created"
        }
    }

    companion object {

        @Ignore
        private val holder = IdCache(13)

        private fun copyTeam(team: Team): Team {
            val copy = Team.empty()
            copy.update(team)
            return copy
        }

        fun join(team: Team, user: User): JoinRequest =
                JoinRequest(teamApproved = false, userApproved = true, id = "", position = Config.positionFromCode(""), team = copyTeam(team), user = user, created = Date())

        fun invite(team: Team): JoinRequest =
                JoinRequest(teamApproved = true, userApproved = false, id = "", position = Config.positionFromCode(""), team = copyTeam(team), user = User.empty(), created = Date())

        @JvmField
        val CREATOR: Parcelable.Creator<JoinRequest> = object : Parcelable.Creator<JoinRequest> {
            override fun createFromParcel(`in`: Parcel): JoinRequest = JoinRequest(`in`)

            override fun newArray(size: Int): Array<JoinRequest?> = arrayOfNulls(size)
        }
    }
}
