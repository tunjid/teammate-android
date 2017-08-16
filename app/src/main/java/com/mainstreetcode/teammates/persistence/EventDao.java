package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.persistence.entity.EventEntity;
import com.mainstreetcode.teammates.persistence.typeconverters.EntityDao;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public abstract class EventDao extends EntityDao<EventEntity> {
    @Query("SELECT * FROM events as event" +
            " INNER JOIN teams as team" +
            " ON event.event_team = team.team_id" +
            " INNER JOIN roles as role" +
            " ON team.team_id = role.role_team_id" +
            " WHERE :userId = role.user_id")
    public abstract Maybe<List<Event>> getEvents(String userId);

    @Query("SELECT * FROM events" +
            " WHERE :id = event_id")
    public abstract Maybe<Event> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<EventEntity> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<EventEntity> teams);

    @Delete
    public abstract void delete(EventEntity event);
}
