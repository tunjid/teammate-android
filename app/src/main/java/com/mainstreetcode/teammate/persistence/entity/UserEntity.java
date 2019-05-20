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
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.mainstreetcode.teammate.model.User;

import static com.mainstreetcode.teammate.util.ModelUtils.processString;

@Entity(tableName = "users")
public class UserEntity implements Parcelable {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "user_id") protected String id;
    @ColumnInfo(name = "user_image_url") protected String imageUrl;
    @ColumnInfo(name = "user_screen_name") protected String screenName;
    @ColumnInfo(name = "user_primary_email") protected String primaryEmail;
    @ColumnInfo(name = "user_first_name") protected CharSequence firstName;
    @ColumnInfo(name = "user_last_name") protected CharSequence lastName;
    @ColumnInfo(name = "user_about") protected CharSequence about;

    public UserEntity(@NonNull String id, String imageUrl, String screenName, String primaryEmail,
                      CharSequence firstName, CharSequence lastName, CharSequence about) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.screenName = screenName;
        this.primaryEmail = primaryEmail;
        this.firstName = firstName;
        this.lastName = lastName;
        this.about = about;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {return "com.mainstreetcode.teammates.model.User(id=" + this.id + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ", primaryEmail=" + this.primaryEmail + ")";}

    @NonNull
    public String getId() {return this.id;}

    public CharSequence getFirstName() {return processString(this.firstName);}

    public CharSequence getLastName() {return processString(this.lastName);}

    public String getScreenName() { return screenName; }

    public String getPrimaryEmail() {return this.primaryEmail;}

    public String getImageUrl() {
        return imageUrl;
    }

    public CharSequence getAbout() {
        return processString(about);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public void setScreenName(String screenName) { this.screenName = screenName; }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    @SuppressWarnings("WeakerAccess")
    public void setAbout(String about) {
        this.about = about;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    protected UserEntity(Parcel in) {
        id = in.readString();
        imageUrl = in.readString();
        screenName = in.readString();
        primaryEmail = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        about = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(imageUrl);
        dest.writeString(screenName);
        dest.writeString(primaryEmail);
        dest.writeString(firstName.toString());
        dest.writeString(lastName.toString());
        dest.writeString(about.toString());
    }

    public static final Parcelable.Creator<UserEntity> CREATOR = new Parcelable.Creator<UserEntity>() {
        @Override
        public UserEntity createFromParcel(Parcel in) {
            return new UserEntity(in);
        }

        @Override
        public UserEntity[] newArray(int size) {
            return new UserEntity[size];
        }
    };
}
