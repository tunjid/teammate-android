package com.mainstreetcode.teammate.persistence.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.Position;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "roles",
        foreignKeys = {
                @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "role_team", onDelete = CASCADE),
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "role_user", onDelete = CASCADE)
        }
)
public class RoleEntity implements Parcelable {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "role_id") protected String id;
    @ColumnInfo(name = "role_image_url") protected String imageUrl;
    @ColumnInfo(name = "role_nickname") protected String nickname;
    @ColumnInfo(name = "role_name") protected Position position;
    @ColumnInfo(name = "role_team") protected Team team;
    @ColumnInfo(name = "role_user") protected User user;
    @ColumnInfo(name = "role_created") protected Date created;

    public RoleEntity(@NonNull String id, String imageUrl, String nickname, Position position, Team team, User user, Date created) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.nickname = nickname;
        this.position = position;
        this.team = team;
        this.user = user;
        this.created = created;
    }

    protected RoleEntity(Parcel in) {
        id = in.readString();
        imageUrl = in.readString();
        position = Config.positionFromCode(in.readString());
        team = (Team) in.readValue(Team.class.getClassLoader());
        user = (User) in.readValue(User.class.getClassLoader());
        created = new Date(in.readLong());
    }

    @NonNull
    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public String getNickname() {
        return nickname;
    }

    public String getImageUrl() {
        return TextUtils.isEmpty(imageUrl) ? user.getImageUrl() : imageUrl;
    }

    public Team getTeam() {
        return team;
    }

    public User getUser() {
        return user;
    }

    public Date getCreated() {
        return created;
    }

    public void setPosition(String position) {
        this.position = Config.positionFromCode(position);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    protected void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
        dest.writeString(imageUrl);
        dest.writeString(position.getCode());
        dest.writeValue(team);
        dest.writeValue(user);
        dest.writeValue(created.getTime());
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
