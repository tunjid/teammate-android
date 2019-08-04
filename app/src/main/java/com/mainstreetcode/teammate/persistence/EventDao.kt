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
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.persistence.entity.EventEntity
import io.reactivex.Maybe
import java.util.*

/**
 * DAO for [Event]
 */

@Dao
abstract class EventDao : EntityDao<EventEntity>() {

    override val tableName: String
        get() = "events"

    @Query("SELECT * FROM events as event" +
            " WHERE :teamId = event_team" +
            " AND event_start_date < :date" +
            " ORDER BY event_start_date DESC" +
            " LIMIT :limit")
    abstract fun getEvents(teamId: String, date: Date, limit: Int): Maybe<List<Event>>

    @Query("SELECT * FROM events" + " WHERE :id = event_id")
    abstract operator fun get(id: String): Maybe<Event>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun insert(models: List<EventEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun update(models: List<EventEntity>)

    @Delete
    abstract override fun delete(model: EventEntity)
}
