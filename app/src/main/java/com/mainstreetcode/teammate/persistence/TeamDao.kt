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

import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.persistence.entity.TeamEntity

import io.reactivex.Maybe

/**
 * DAO for [Team]
 *
 *
 * Created by Shemanigans on 6/12/17.
 */

@Dao
abstract class TeamDao : EntityDao<TeamEntity>() {

    override val tableName: String
        get() = "teams"

    @get:Query("SELECT * FROM teams")
    abstract val teams: Maybe<List<Team>>

    @Query("SELECT * FROM teams" + " WHERE :id = team_id")
    abstract operator fun get(id: String): Maybe<Team>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun insert(models: List<TeamEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun update(models: List<TeamEntity>)

    @Delete
    abstract override fun delete(model: TeamEntity)
}
