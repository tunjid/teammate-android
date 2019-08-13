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

package com.mainstreetcode.teammate.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.persistence.entity.GuestEntity

import java.util.Date

import io.reactivex.Maybe

@Dao
abstract class GuestDao : EntityDao<GuestEntity>() {

    override val tableName: String
        get() = "guests"

    @Query("SELECT * FROM guests" + " WHERE :id = guest_id")
    abstract fun get(id: String): Maybe<Guest>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun insert(models: List<GuestEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun update(models: List<GuestEntity>)

    @Delete
    abstract override fun delete(models: List<GuestEntity>)

    @Query("DELETE FROM guests " +
            " WHERE guest_user = :userId" +
            " AND guest_event IN (" +
            " SELECT event_id FROM events event " +
            " INNER JOIN teams team" +
            " ON (event.event_team = team.team_id)" +
            " WHERE team.team_id = :teamId" +
            ")")
    abstract fun deleteUsers(userId: String, teamId: String)

    @Query("SELECT * FROM guests" +
            " WHERE :eventId = guest_event" +
            " AND guest_created < :date" +
            " ORDER BY guest_created DESC" +
            " LIMIT :limit")
    abstract fun getGuests(eventId: String, date: Date, limit: Int): Maybe<List<Guest>>

    @Query("SELECT * FROM guests" +
            " WHERE :userId = guest_user" +
            " AND guest_created < :date" +
            " AND guest_attending = 1" +
            " ORDER BY guest_created DESC" +
            " LIMIT 40")
    abstract fun getRsvpList(userId: String, date: Date): Maybe<List<Guest>>
}
