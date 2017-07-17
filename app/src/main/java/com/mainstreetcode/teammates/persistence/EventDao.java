package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.persistence.entity.EventEntity;

import java.util.List;

/**
 * DAO for {@link Event}
 */

@Dao
public interface EventDao {
    @Query("SELECT * FROM events as event" +
            " INNER JOIN teams as team" +
            " ON event.event_team = team.team_id" +
            " INNER JOIN roles as role" +
            " ON team.team_id = role.role_team_id" +
            " WHERE :userId = role.user_id")
    List<Event> getEvents(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<EventEntity> events);

    @Delete
    void delete(EventEntity event);
}
