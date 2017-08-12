package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.mainstreetcode.teammates.persistence.entity.TeamChatRoomEntity;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public interface TeamChatRoomDao {
    @Query("SELECT * FROM team_chat_rooms")
    Maybe<List<TeamChatRoom>> getTeamChatRooms();

    @Query("SELECT * FROM team_chat_rooms" +
            " WHERE :id = team_chat_room_id")
    Maybe<TeamChatRoom> get(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<TeamChatRoomEntity> chatRooms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChats(List<TeamChat> chats);
}
