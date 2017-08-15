package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.persistence.typeconverters.EntityDao;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public abstract class TeamChatDao extends EntityDao<TeamChat> {

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
