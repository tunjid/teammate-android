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

import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.persistence.entity.CompetitorEntity

import io.reactivex.Maybe

/**
 * DAO for [Event]
 */

@Dao
abstract class CompetitorDao : EntityDao<CompetitorEntity>() {

     override val tableName: String
        get() = "competitors"

    @Query("SELECT * FROM competitors" +
            " WHERE :tournamentId = competitor_tournament" +
            " ORDER BY competitor_created DESC" +
            " LIMIT 40")
    abstract fun getCompetitors(tournamentId: String): Maybe<List<Competitor>>

    @Query("SELECT * FROM competitors" + " WHERE :id = competitor_id")
    abstract operator fun get(id: String): Maybe<Competitor>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun insert(models: List<CompetitorEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun update(models: List<CompetitorEntity>)

    @Delete
    abstract override fun delete(model: CompetitorEntity)
}
