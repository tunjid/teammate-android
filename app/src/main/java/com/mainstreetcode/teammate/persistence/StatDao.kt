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
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.persistence.entity.StatEntity

import java.util.Date

import io.reactivex.Maybe

/**
 * DAO for [Event]
 */

@Dao
abstract class StatDao : EntityDao<StatEntity>() {

    override val tableName: String
        get() = "stats"

    @Query("SELECT * FROM stats as stat" +
            " WHERE :game_id = stat_game" +
            " AND stat_created < :date" +
            " ORDER BY stat_created DESC" +
            " LIMIT :limit")
    abstract fun getStats(game_id: String, date: Date, limit: Int): Maybe<List<Stat>>

    @Query("SELECT * FROM stats" + " WHERE :id = stat_id")
    abstract fun get(id: String): Maybe<Stat>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun insert(models: List<StatEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun update(models: List<StatEntity>)

    @Delete
    abstract override fun delete(model: StatEntity)
}
