/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("SELECT * FROM guests" +
            " WHERE :id = guest_id")
    public abstract Maybe<Guest> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<GuestEntity> guests);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<GuestEntity> guests);

    @Delete
    public abstract void delete(List<GuestEntity> guests);

    @Query("DELETE FROM guests " +
            " WHERE guest_user = :userId" +
            " AND guest_event IN (" +
            " SELECT event_id FROM events event " +
            " INNER JOIN teams team" +
            " ON (event.event_team = team.team_id)" +
            " WHERE team.team_id = :teamId" +
            ")")
    public abstract void deleteUsers(String userId, String teamId);

    @Query("SELECT * FROM guests" +
            " WHERE :eventId = guest_event" +
            " AND guest_created < :date" +
            " ORDER BY guest_created DESC" +
            " LIMIT :limit")
    public abstract Maybe<List<Guest>> getGuests(String eventId, Date date, int limit);

    @Query("SELECT * FROM guests" +
            " WHERE :userId = guest_user" +
            " AND guest_created < :date" +
            " AND guest_attending = 1" +
            " ORDER BY guest_created DESC" +
            " LIMIT 40")
    public abstract Maybe<List<Guest>> getRsvpList(String userId, Date date);
}
