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


import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.entity.TeamEntity
import com.mainstreetcode.teammate.persistence.entity.UserEntity
import java.util.*

@Entity(
        tableName = "team_media",
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["user_id"], childColumns = ["media_user"], onDelete = CASCADE),
            ForeignKey(entity = TeamEntity::class, parentColumns = ["team_id"], childColumns = ["media_team"], onDelete = CASCADE)
        ]
)

open class MediaEntity : Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "media_id")
    var hiddenId: String
        protected set

    @ColumnInfo(name = "media_url")
    var url: String
        protected set

    @ColumnInfo(name = "media_mime_type")
    var mimeType: String
        protected set

    @ColumnInfo(name = "media_thumbnail")
    var thumbnail: String
        protected set

    @ColumnInfo(name = "media_user")
    var user: User
        protected set

    @ColumnInfo(name = "media_team")
    var hiddenTeam: Team
        protected set

    @ColumnInfo(name = "media_created")
    var created: Date
        protected set

    @ColumnInfo(name = "media_flagged")
    var isFlagged: Boolean = false
        protected set

    constructor(hiddenId: String, url: String, mimeType: String, thumbnail: String,
                user: User, hiddenTeam: Team, created: Date, flagged: Boolean) {
        this.hiddenId = hiddenId
        this.url = url
        this.mimeType = mimeType
        this.thumbnail = thumbnail
        this.user = user
        this.hiddenTeam = hiddenTeam
        this.created = created
        this.isFlagged = flagged
    }

    constructor(`in`: Parcel) {
        hiddenId = `in`.readString()!!
        url = `in`.readString()!!
        mimeType = `in`.readString()!!
        thumbnail = `in`.readString()!!
        user = `in`.readValue(User::class.java.classLoader) as User
        hiddenTeam = `in`.readValue(Team::class.java.classLoader) as Team
        val tmpCreated = `in`.readLong()
        created = Date(tmpCreated)
        isFlagged = `in`.readByte().toInt() != 0x00
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaEntity) return false

        val chat = other as MediaEntity?

        return hiddenId == chat?.hiddenId
    }

    override fun hashCode(): Int = hiddenId.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(hiddenId)
        dest.writeString(url)
        dest.writeString(mimeType)
        dest.writeString(thumbnail)
        dest.writeValue(user)
        dest.writeValue(hiddenTeam)
        dest.writeLong(created.time)
        dest.writeByte((if (isFlagged) 0x01 else 0x00).toByte())
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<MediaEntity> = object : Parcelable.Creator<MediaEntity> {
            override fun createFromParcel(`in`: Parcel): MediaEntity = MediaEntity(`in`)

            override fun newArray(size: Int): Array<MediaEntity?> = arrayOfNulls(size)
        }
    }
}
