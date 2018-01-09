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
    @ColumnInfo(name = "join_request_role_name") protected String roleName;

    @ColumnInfo(name = "join_request_team") protected Team team;
    @ColumnInfo(name = "join_request_user") protected User user;

    protected JoinRequestEntity(boolean teamApproved, boolean userApproved,
                                @NonNull String id, String roleName, Team team, User user) {
        this.teamApproved = teamApproved;
        this.userApproved = userApproved;
        this.id = id;
        this.roleName = roleName;
        this.team = team;
        this.user = user;
    }

    protected JoinRequestEntity(Parcel in) {
        teamApproved = in.readByte() != 0x00;
        userApproved = in.readByte() != 0x00;
        id = in.readString();
        roleName = in.readString();
        team = (Team) in.readValue(Team.class.getClassLoader());
        user = (User) in.readValue(User.class.getClassLoader());
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public Team getTeam() {
        return team;
    }

    public User getUser() {
        return user;
    }

    public boolean isTeamApproved() {
        return teamApproved;
    }

    public boolean isUserApproved() {
        return userApproved;
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
        dest.writeString(roleName);
        dest.writeValue(team);
        dest.writeValue(user);
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
