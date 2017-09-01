package com.mainstreetcode.teammates.persistence.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.mainstreetcode.teammates.model.Team;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "team_chat_rooms",
        foreignKeys = @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "team_chat_room_team", onDelete = CASCADE)
)
public class TeamChatRoomEntity implements
        Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "team_chat_room_id") protected String id;
    @ColumnInfo(name = "team_chat_room_team") protected Team team;
    @ColumnInfo(name = "team_chat_room_last_seen") protected Date lastSeen;

    public TeamChatRoomEntity(String id, Team team) {
        this.id = id;
        this.team = team;
        lastSeen = new Date();
    }

    protected TeamChatRoomEntity(Parcel in) {
        id = in.readString();
        team = (Team) in.readValue(Team.class.getClassLoader());
        lastSeen = new Date(in.readLong());
    }

    public String getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamChatRoomEntity)) return false;

        TeamChatRoomEntity that = (TeamChatRoomEntity) o;

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
        dest.writeValue(team);
        dest.writeLong(lastSeen.getTime());
    }

    public static final Creator<TeamChatRoomEntity> CREATOR = new Creator<TeamChatRoomEntity>() {
        @Override
        public TeamChatRoomEntity createFromParcel(Parcel in) {
            return new TeamChatRoomEntity(in);
        }

        @Override
        public TeamChatRoomEntity[] newArray(int size) {
            return new TeamChatRoomEntity[size];
        }
    };
}
