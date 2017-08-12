package com.mainstreetcode.teammates.persistence.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.mainstreetcode.teammates.model.User;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "roles",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "user_id"),
                @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "role_team_id", onDelete = CASCADE)
        }
)
public class RoleEntity implements Parcelable {

    @PrimaryKey @ColumnInfo(name = "role_id") protected String id;
    @ColumnInfo(name = "role_name") protected String name;
    @ColumnInfo(name = "role_team_id") protected String teamId;
    @ColumnInfo(name = "role_image_url") protected String imageUrl;

    @Embedded protected User user;

    public RoleEntity(String id, String name, String teamId, String imageUrl, User user) {
        this.id = id;
        this.name = name;
        this.teamId = teamId;
        this.imageUrl = imageUrl;
        this.user = user;
    }

    protected RoleEntity(Parcel in) {
        id = in.readString();
        name = in.readString();
        teamId = in.readString();
        imageUrl = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public User getUser() {
        return user;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleEntity)) return false;

        RoleEntity that = (RoleEntity) o;

        return id.equals(that.id);
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
        dest.writeString(name);
        dest.writeString(teamId);
        dest.writeString(imageUrl);
        dest.writeValue(user);
    }

    public static final Creator<RoleEntity> CREATOR = new Creator<RoleEntity>() {
        @Override
        public RoleEntity createFromParcel(Parcel in) {
            return new RoleEntity(in);
        }

        @Override
        public RoleEntity[] newArray(int size) {
            return new RoleEntity[size];
        }
    };
}
