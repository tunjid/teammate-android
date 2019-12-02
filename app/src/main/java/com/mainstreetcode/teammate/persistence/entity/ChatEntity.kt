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
import com.mainstreetcode.teammate.util.processEmoji
import java.util.*

@Entity(
        tableName = "team_chats",
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["user_id"], childColumns = ["team_chat_user"], onDelete = CASCADE),
            ForeignKey(entity = TeamEntity::class, parentColumns = ["team_id"], childColumns = ["team_chat_team"], onDelete = CASCADE)
        ]
)
open class ChatEntity : Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "team_chat_id")
    var id: String
        protected set

    @ColumnInfo(name = "team_chat_kind")
    var kind: String
        protected set

    @ColumnInfo(name = "team_chat_content")
    var content: CharSequence
        get() = field.processEmoji()
        protected set

    @ColumnInfo(name = "team_chat_user")
    var user: User
        protected set

    @ColumnInfo(name = "team_chat_team")
    var hiddenTeam: Team
        protected set

    @ColumnInfo(name = "team_chat_created")
    var created: Date
        protected set

    constructor(id: String, kind: String,
                content: CharSequence,
                user: User, hiddenTeam: Team, created: Date) {
        this.id = id
        this.content = content
        this.kind = kind
        this.user = user
        this.hiddenTeam = hiddenTeam
        this.created = created
    }

    constructor(`in`: Parcel) {
        id = `in`.readString()!!
        kind = `in`.readString()!!
        content = `in`.readString()!!
        user = `in`.readValue(User::class.java.classLoader) as User
        hiddenTeam = `in`.readValue(Team::class.java.classLoader) as Team
        val tmpCreated = `in`.readLong()
        created = Date(tmpCreated)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChatEntity) return false

        val chat = other as ChatEntity?

        return id == chat?.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(kind)
        dest.writeString(content.toString())
        dest.writeValue(user)
        dest.writeValue(hiddenTeam)
        dest.writeLong(created.time)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<ChatEntity> = object : Parcelable.Creator<ChatEntity> {
            override fun createFromParcel(`in`: Parcel): ChatEntity = ChatEntity(`in`)

            override fun newArray(size: Int): Array<ChatEntity?> = arrayOfNulls(size)
        }
    }
}
