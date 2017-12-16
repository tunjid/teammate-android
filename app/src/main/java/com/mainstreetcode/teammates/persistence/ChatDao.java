package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.User;

import java.util.Date;
import java.util.List;

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
            " LIMIT 20")
    public abstract Maybe<List<Chat>> chatsBefore(String teamId, Date date);

    @Query("SELECT * FROM team_chats" +
            " WHERE team_chat_team = :teamId" +
            " AND team_chat_user != :signedInUser" +
            " AND team_chat_created > :date")
    public abstract Maybe<List<Chat>> unreadChats(String teamId, User signedInUser, Date date);

    @Query("SELECT * FROM team_chats" +
            " WHERE :id = team_chat_id")
    public abstract Maybe<Chat> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<Chat> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<Chat> teams);

    @Delete
    public abstract void delete(Chat chat);
}
