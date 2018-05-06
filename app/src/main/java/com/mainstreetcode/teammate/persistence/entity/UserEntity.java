package com.mainstreetcode.teammate.persistence.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mainstreetcode.teammate.model.User;

@Entity(tableName = "users")
public class UserEntity implements Parcelable {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "user_id") protected String id;
    @ColumnInfo(name = "user_first_name") protected String firstName;
    @ColumnInfo(name = "user_last_name") protected String lastName;
    @ColumnInfo(name = "user_primary_email") protected String primaryEmail;
    @ColumnInfo(name = "user_about") protected String about;
    @ColumnInfo(name = "user_image_url") protected String imageUrl;

    public UserEntity(@NonNull String id, String firstName, String lastName, String primaryEmail, String about, String imageUrl) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageUrl = imageUrl;
        this.about = about;
        this.primaryEmail = primaryEmail;
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

    public String getFirstName() {return this.firstName;}

    @SuppressWarnings("unused")
    public String getLastName() {return this.lastName;}

    public String getPrimaryEmail() {return this.primaryEmail;}

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAbout() {
        return about;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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
        firstName = in.readString();
        lastName = in.readString();
        primaryEmail = in.readString();
        about = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(primaryEmail);
        dest.writeString(about);
        dest.writeString(imageUrl);
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
