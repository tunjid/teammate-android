package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.persistence.entity.TeamChatRoomEntity;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public abstract class TeamChatRoomDao extends EntityDao<TeamChatRoomEntity> {

    @Override
    protected String getTableName() {
        return "team_chat_rooms";
    }

    @Query("SELECT * FROM team_chat_rooms")
    public abstract Maybe<List<TeamChatRoom>> getTeamChatRooms();

    @Query("SELECT * FROM team_chat_rooms" +
            " WHERE :id = team_chat_room_id")
    public abstract Maybe<TeamChatRoom> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<TeamChatRoomEntity> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<TeamChatRoomEntity> teams);
}
