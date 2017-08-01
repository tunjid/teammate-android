package com.mainstreetcode.teammates.persistence.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.mainstreetcode.teammates.model.User;

@Entity(
        tableName = "join-requests",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "user_id"),
                @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "join_request_team_id")
        }
)
public class JoinRequestEntity implements Parcelable {

    @ColumnInfo(name = "join_request_team_approved") protected boolean teamApproved;
    @ColumnInfo(name = "join_request_team_userApproved") protected boolean userApproved;

    @PrimaryKey @ColumnInfo(name = "join_request_id") protected String id;
    @ColumnInfo(name = "join_request_role_name") protected String roleName;
    @ColumnInfo(name = "join_request_team_id") protected String teamId;

    @Embedded protected User user;

    protected JoinRequestEntity(boolean teamApproved, boolean userApproved, String id, String roleName, String teamId, User user) {
        this.teamApproved = teamApproved;
        this.userApproved = userApproved;
        this.id = id;
        this.roleName = roleName;
        this.teamId = teamId;
        this.user = user;
    }

    protected JoinRequestEntity(Parcel in) {
        teamApproved = in.readByte() != 0x00;
        userApproved = in.readByte() != 0x00;
        id = in.readString();
        roleName = in.readString();
        teamId = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());
    }

    public String getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getTeamId() {
        return teamId;
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
        dest.writeString(teamId);
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
