package com.mainstreetcode.teammates.persistence.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "roles",
        foreignKeys = {
                @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "role_team", onDelete = CASCADE),
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "role_user")
        }
)
public class RoleEntity implements Parcelable {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "role_id") protected String id;
    @ColumnInfo(name = "role_name") protected String name;
    @ColumnInfo(name = "role_image_url") protected String imageUrl;

    @ColumnInfo(name = "role_team") protected Team team;
    @ColumnInfo(name = "role_user") protected User user;

    public RoleEntity(@NonNull String id, String name, String imageUrl, Team team, User user) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.team = team;
        this.user = user;
    }

    protected RoleEntity(Parcel in) {
        id = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        team = (Team) in.readValue(Team.class.getClassLoader());
        user = (User) in.readValue(User.class.getClassLoader());
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Team getTeam() {
        return team;
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
        dest.writeString(imageUrl);
        dest.writeValue(team);
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
