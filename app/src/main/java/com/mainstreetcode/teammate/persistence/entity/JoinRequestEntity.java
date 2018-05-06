package com.mainstreetcode.teammate.persistence.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.Position;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "join_requests",
        foreignKeys = {
                @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "join_request_team", onDelete = CASCADE),
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "join_request_user", onDelete = CASCADE)
        }
)
public class JoinRequestEntity implements Parcelable {

    @ColumnInfo(name = "join_request_team_approved") protected boolean teamApproved;
    @ColumnInfo(name = "join_request_team_userApproved") protected boolean userApproved;

    @NonNull @PrimaryKey
    @ColumnInfo(name = "join_request_id") protected String id;
    @ColumnInfo(name = "join_request_role_name") protected Position position;

    @ColumnInfo(name = "join_request_team") protected Team team;
    @ColumnInfo(name = "join_request_user") protected User user;
    @ColumnInfo(name = "join_request_created") protected Date created;

    protected JoinRequestEntity(boolean teamApproved, boolean userApproved,
                                @NonNull String id, Position position, Team team, User user, Date created) {
        this.teamApproved = teamApproved;
        this.userApproved = userApproved;
        this.id = id;
        this.position = position;
        this.team = team;
        this.user = user;
        this.created = created;
    }

    protected JoinRequestEntity(Parcel in) {
        teamApproved = in.readByte() != 0x00;
        userApproved = in.readByte() != 0x00;
        id = in.readString();
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

    public Team getTeam() {
        return team;
    }

    public User getUser() {
        return user;
    }

    public Date getCreated() {
        return created;
    }


    public boolean isTeamApproved() {
        return teamApproved;
    }

    public boolean isUserApproved() {
        return userApproved;
    }

    public void setPosition(String position) {
        this.position = Config.positionFromCode(position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JoinRequestEntity)) return false;

        JoinRequestEntity that = (JoinRequestEntity) o;

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
        dest.writeByte((byte) (teamApproved ? 0x01 : 0x00));
        dest.writeByte((byte) (userApproved ? 0x01 : 0x00));
        dest.writeString(id);
        dest.writeString(position.getCode());
        dest.writeValue(team);
        dest.writeValue(user);
        dest.writeValue(created.getTime());
    }

    public static final Creator<JoinRequestEntity> CREATOR = new Creator<JoinRequestEntity>() {
        @Override
        public JoinRequestEntity createFromParcel(Parcel in) {
            return new JoinRequestEntity(in);
        }

        @Override
        public JoinRequestEntity[] newArray(int size) {
            return new JoinRequestEntity[size];
        }
    };
}
