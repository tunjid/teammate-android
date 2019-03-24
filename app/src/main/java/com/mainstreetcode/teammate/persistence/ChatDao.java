package com.mainstreetcode.teammate.persistence;

import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Event;

import java.util.Date;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public abstract class ChatDao extends EntityDao<Chat> {

    @Override
    protected String getTableName() {
        return "team_chats";
    }

    @Query("SELECT * FROM team_chats" +
            " WHERE team_chat_team = :teamId" +
            " AND team_chat_created < :date" +
            " ORDER BY team_chat_created DESC" +
            " LIMIT :limit")
    public abstract Maybe<List<Chat>> chatsBefore(String teamId, Date date, int limit);

    @Query("SELECT * FROM team_chats" +
            " WHERE team_chat_team = :teamId" +
            " AND team_chat_created > :date" +
            " ORDER BY team_chat_created DESC" +
            " LIMIT 10")
    public abstract Maybe<List<Chat>> unreadChats(String teamId, Date date);

    @Query("SELECT * FROM team_chats" +
            " WHERE :id = team_chat_id")
    public abstract Maybe<Chat> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<Chat> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<Chat> teams);

    @Delete
    public abstract void delete(Chat chat);
}
