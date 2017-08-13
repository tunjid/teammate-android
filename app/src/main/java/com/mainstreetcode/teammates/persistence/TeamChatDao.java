package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.TeamChat;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public interface TeamChatDao {

    @Query("SELECT * FROM team_chats" +
            " WHERE :id = team_chat_id")
    Maybe<TeamChat> get(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<TeamChat> chats);

    @Delete
    void delete(TeamChat teamChat);
}
