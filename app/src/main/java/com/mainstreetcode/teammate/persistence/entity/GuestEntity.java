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

package com.mainstreetcode.teammate.persistence.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.User;

import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "guests",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "guest_user", onDelete = CASCADE),
                @ForeignKey(entity = EventEntity.class, parentColumns = "event_id", childColumns = "guest_event", onDelete = CASCADE)
        }
)
public class GuestEntity implements Parcelable{

    @NonNull @PrimaryKey
    @ColumnInfo(name = "guest_id") protected String id;
    @ColumnInfo(name = "guest_user") protected User user;
    @ColumnInfo(name = "guest_event") protected Event event;
    @ColumnInfo(name = "guest_created") protected Date created;
    @ColumnInfo(name = "guest_attending") protected boolean attending;

    public GuestEntity(@NonNull String id, User user, Event event, Date created, boolean attending) {
        this.id = id;
        this.user = user;
        this.event = event;
        this.created = created;
        this.attending = attending;
    }

    protected GuestEntity(Parcel in) {
        id = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());
        event = (Event) in.readValue(User.class.getClassLoader());
        long tmpCreated = in.readLong();
        created = tmpCreated != -1 ? new Date(tmpCreated) : null;
        attending = in.readByte() != 0x00;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Event getEvent() {
        return event;
    }

    public Date getCreated() {
        return created;
    }

    public boolean isAttending() {
        return attending;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuestEntity)) return false;

        GuestEntity guest = (GuestEntity) o;
        return id.equals(guest.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeValue(user);
        dest.writeValue(event);
        dest.writeLong(created != null ? created.getTime() : -1L);
        dest.writeByte((byte) (attending ? 0x01 : 0x00));
    }

    public static final Creator<GuestEntity> CREATOR = new Creator<GuestEntity>() {
        @Override
        public GuestEntity createFromParcel(Parcel in) {
            return new GuestEntity(in);
        }

        @Override
        public GuestEntity[] newArray(int size) {
            return new GuestEntity[size];
        }
    };
}
