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

package com.mainstreetcode.teammate.persistence.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.enums.Position

import java.util.Date

import androidx.room.ForeignKey.CASCADE

@Entity(tableName = "roles", foreignKeys = [ForeignKey(entity = TeamEntity::class, parentColumns = ["team_id"], childColumns = ["role_team"], onDelete = CASCADE), ForeignKey(entity = UserEntity::class, parentColumns = ["user_id"], childColumns = ["role_user"], onDelete = CASCADE)])
open class RoleEntity : Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "role_id")
    var id: String
        protected set

    @ColumnInfo(name = "role_image_url")
    var imageUrl: String

    @ColumnInfo(name = "role_nickname")
    var nickname: String

    @ColumnInfo(name = "role_name")
    var position: Position
        protected set

    @ColumnInfo(name = "role_team")
    var team: Team
        protected set

    @ColumnInfo(name = "role_user")
    var user: User
        protected set

    @ColumnInfo(name = "role_created")
    var created: Date
        protected set

    constructor(id: String, imageUrl: String, nickname: String, position: Position, team: Team, user: User, created: Date) {
        this.id = id
        this.imageUrl = imageUrl
        this.nickname = nickname
        this.position = position
        this.team = team
        this.user = user
        this.created = created
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()!!
        imageUrl = `in`.readString()!!
        nickname = `in`.readString()!!
        position = Config.positionFromCode(`in`.readString())
        team = `in`.readValue(Team::class.java.classLoader) as Team
        user = `in`.readValue(User::class.java.classLoader) as User
        created = Date(`in`.readLong())
    }

    fun setPosition(position: String) {
        this.position = Config.positionFromCode(position)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RoleEntity) return false

        val that = other as RoleEntity?

        return id == that!!.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(imageUrl)
        dest.writeString(nickname)
        dest.writeString(position.code)
        dest.writeValue(team)
        dest.writeValue(user)
        dest.writeLong(created.time)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<RoleEntity> = object : Parcelable.Creator<RoleEntity> {
            override fun createFromParcel(`in`: Parcel): RoleEntity = RoleEntity(`in`)

            override fun newArray(size: Int): Array<RoleEntity?> = arrayOfNulls(size)
        }
    }
}
