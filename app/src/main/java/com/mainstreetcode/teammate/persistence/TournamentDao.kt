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
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.persistence.entity.TournamentEntity

import java.util.Date

import io.reactivex.Maybe

/**
 * DAO for [Event]
 */

@Dao
abstract class TournamentDao : EntityDao<TournamentEntity>() {

    override val tableName: String
        get() = "tournaments"

    @Query("SELECT * FROM tournaments as tournament" +
            " INNER JOIN games AS game" +
            " ON tournament.tournament_id = game.game_tournament" +
            " WHERE :teamId = game.game_host" +
            " OR :teamId = game.game_home_entity" +
            " OR :teamId = game.game_away_entity" +
            " AND tournament.tournament_created < :date" +
            " ORDER BY tournament.tournament_created DESC" +
            " LIMIT :limit")
    abstract fun getTournaments(teamId: String, date: Date, limit: Int): Maybe<List<Tournament>>

    @Query("SELECT * FROM tournaments" + " WHERE :id = tournament_id")
    abstract fun get(id: String): Maybe<Tournament>

    @Query("DELETE FROM events " +
            " WHERE event_game_id IN (" +
            " SELECT game_id FROM games  " +
            " WHERE game_tournament = :tournamentId" +
            ")")
    abstract fun deleteTournamentEvents(tournamentId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun insert(models: List<TournamentEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun update(models: List<TournamentEntity>)

    @Delete
    abstract override fun delete(model: TournamentEntity)
}
