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

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mainstreetcode.teammate.util.*
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

import java.lang.reflect.Type

/**
 * Roles on a team
 */

class StatRank internal constructor(
        private val count: Int,
        override val team: Team,
        override val user: User
) : UserHost, TeamHost, RemoteImage, Differentiable, Comparable<StatRank> {

    val rank:String
    get() = count.toString()

    val inset: String
        get() = team.imageUrl

    val title: CharSequence
        get() = user.name

    val subtitle: CharSequence
        get() = team.name

    override val imageUrl: String
        get() = user.imageUrl

    override fun getId(): String = user.id + "-" + team.id

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is StatRank) id == other.id else user.areContentsTheSame(other.user) && team.areContentsTheSame(other.team)

    override fun getChangePayload(other: Differentiable?): Any? = other

    override fun compareTo(other: StatRank): Int = -count.compareTo(other.count)

    class GsonAdapter : JsonDeserializer<StatRank> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): StatRank {

            val roleJson = json.asJsonObject

            val count = roleJson.asFloatOrZero(COUNT).toInt()
            var team: Team? = context.deserialize<Team>(roleJson.get(TEAM_KEY), Team::class.java)
            var user: User? = context.deserialize<User>(roleJson.get(USER_KEY), User::class.java)

            if (user == null) user = User.empty()
            if (team == null) team = Team.empty()

            return StatRank(count, team, user)
        }

        companion object {

            private const val COUNT = "count"
            private const val USER_KEY = "user"
            private const val TEAM_KEY = "team"
        }
    }
}
