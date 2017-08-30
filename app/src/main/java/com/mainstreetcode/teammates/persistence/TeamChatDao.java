package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.TeamChat;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public abstract class TeamChatDao extends EntityDao<TeamChat> {

    @Override
    protected String getTableName() {
        return "team_chats";
    }

    @Query("SELECT * FROM team_chats" +
            " WHERE parent_chat_room_id = :chatRoomId" +
            " AND team_chat_created < :date")
    public abstract Maybe<List<TeamChat>> chatsBefore(String chatRoomId, Date date);

    @Query("SELECT * FROM team_chats" +
            " WHERE :id = team_chat_id")
    public abstract Maybe<TeamChat> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<TeamChat> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<TeamChat> teams);

    @Delete
    public abstract void delete(TeamChat teamChat);
}
