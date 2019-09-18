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
import androidx.annotation.NonNull

import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.User

import java.util.Date

import androidx.room.ForeignKey.CASCADE

@Entity(
        tableName = "guests",
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["user_id"], childColumns = ["guest_user"], onDelete = CASCADE),
            ForeignKey(entity = EventEntity::class, parentColumns = ["event_id"], childColumns = ["guest_event"], onDelete = CASCADE)
        ]
)
open class GuestEntity : Parcelable {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "guest_id")
    var id: String

    @ColumnInfo(name = "guest_user")
    var user: User
        protected set

    @ColumnInfo(name = "guest_event")
    var event: Event
        protected set

    @ColumnInfo(name = "guest_created")
    var created: Date
        protected set

    @ColumnInfo(name = "guest_attending")
    var isAttending: Boolean = false
        protected set

    constructor(id: String, user: User, event: Event, created: Date, attending: Boolean) {
        this.id = id
        this.user = user
        this.event = event
        this.created = created
        this.isAttending = attending
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()!!
        user = `in`.readValue(User::class.java.classLoader) as User
        event = `in`.readValue(User::class.java.classLoader) as Event
        val tmpCreated = `in`.readLong()
        created = Date(tmpCreated)
        isAttending = `in`.readByte().toInt() != 0x00
    }

    

    

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GuestEntity) return false

        val guest = other as GuestEntity?
        return id == guest!!.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeValue(user)
        dest.writeValue(event)
        dest.writeLong(created.time)
        dest.writeByte((if (isAttending) 0x01 else 0x00).toByte())
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<GuestEntity> = object : Parcelable.Creator<GuestEntity> {
            override fun createFromParcel(`in`: Parcel): GuestEntity = GuestEntity(`in`)

            override fun newArray(size: Int): Array<GuestEntity?> = arrayOfNulls(size)
        }
    }
}
