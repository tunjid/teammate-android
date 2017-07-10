package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mainstreetcode.teammates.model.Event;

import java.util.List;

/**
 * DAO for {@link Event}
 */

@Dao
public interface EventDao {
    @Query("SELECT * FROM events as event" +
            " INNER JOIN teams as team")
    List<Event> getEvents(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Event> events);

    @Delete
    void delete(Event event);
}
