package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.persistence.entity.GuestEntity;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

@Dao
public abstract class GuestDao extends EntityDao<GuestEntity> {

    @Override
    protected String getTableName() {
        return "guests";
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<GuestEntity> guests);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<GuestEntity> guests);

    @Delete
    public abstract void delete(List<GuestEntity> guests);

    @Query("SELECT * FROM guests as role" +
            " WHERE :eventId = guest_event" +
            " AND guest_created < :date" +
            " ORDER BY guest_created DESC" +
            " LIMIT 40")
    public abstract Maybe<List<Guest>> getGuests(String eventId, Date date);
}
