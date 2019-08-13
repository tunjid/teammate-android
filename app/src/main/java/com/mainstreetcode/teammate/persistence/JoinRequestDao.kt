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
import android.text.TextUtils

import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity
import com.mainstreetcode.teammate.util.Logger

import java.util.Date

import io.reactivex.Maybe

import com.mainstreetcode.teammate.BuildConfig.DEV

/**
 * DAO for [com.mainstreetcode.teammate.model.JoinRequest]
 */

@Dao
abstract class JoinRequestDao : EntityDao<JoinRequestEntity>() {

    override val tableName: String
        get() = "join_requests"

    @Query("SELECT * FROM join_requests" + " WHERE :id = join_request_id")
    abstract fun get(id: String): Maybe<JoinRequest>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun insert(models: List<JoinRequestEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun update(models: List<JoinRequestEntity>)

    @Delete
    abstract override fun delete(models: List<JoinRequestEntity>)

    @Query("DELETE FROM join_requests WHERE join_request_team = :teamId")
    abstract fun deleteByTeam(teamId: String)

    @Query("DELETE FROM join_requests WHERE join_request_user = :userId AND join_request_team = :teamId")
    abstract fun deleteUsers(userId: String, teamId: String)

    @Query("DELETE FROM join_requests WHERE join_request_user IN (:userIds) AND join_request_team = :teamId")
    fun deleteRequestsFromTeam(teamId: String, userIds: Array<String>) {
        for (i in userIds.indices) userIds[i] = "'" + userIds[i] + "'"

        val formattedIds = TextUtils.join(COMMA_DELIMITER, userIds)
        val sql = String.format(MULTI_DELETION_STATEMENT, teamId, formattedIds)

        val deleted = AppDatabase.instance.compileStatement(sql).executeUpdateDelete()
        if (DEV) Logger.log(tableName, "Deleted $deleted rows")
    }

    @Query("SELECT * FROM join_requests as request" +
            " WHERE :teamId = join_request_team" +
            " AND join_request_created < :date" +
            " ORDER BY join_request_created DESC" +
            " LIMIT :limit")
    abstract fun getRequests(teamId: String, date: Date, limit: Int): Maybe<List<JoinRequest>>

    companion object {

        private const val MULTI_DELETION_STATEMENT = "DELETE FROM join_requests WHERE join_request_team = '%1\$s' AND join_request_user IN (%2\$s)"
        private const val COMMA_DELIMITER = ", "
    }
}
