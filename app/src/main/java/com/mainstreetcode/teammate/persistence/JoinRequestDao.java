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
import android.text.TextUtils;

import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammate.util.Logger;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

import static com.mainstreetcode.teammate.BuildConfig.DEV;

/**
 * DAO for {@link com.mainstreetcode.teammate.model.JoinRequest}
 */

@Dao
public abstract class JoinRequestDao extends EntityDao<JoinRequestEntity> {

    private static final String MULTI_DELETION_STATEMENT = "DELETE FROM join_requests WHERE join_request_team = '%1$s' AND join_request_user IN (%2$s)";
    private static final String COMMA_DELIMITER = ", ";

    @Override
    protected String getTableName() {
        return "join_requests";
    }

    @Query("SELECT * FROM join_requests" +
            " WHERE :id = join_request_id")
    public abstract Maybe<JoinRequest> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<JoinRequestEntity> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<JoinRequestEntity> teams);

    @Delete
    public abstract void delete(List<JoinRequestEntity> roles);

    @Query("DELETE FROM join_requests WHERE join_request_team = :teamId")
    public abstract void deleteByTeam(String teamId);

    @Query("DELETE FROM join_requests WHERE join_request_user = :userId AND join_request_team = :teamId")
    public abstract void deleteUsers(String userId, String teamId);

    @Query("DELETE FROM join_requests WHERE join_request_user IN (:userIds) AND join_request_team = :teamId")
    public void deleteRequestsFromTeam(String teamId, String[] userIds) {
        for (int i = 0; i < userIds.length; i++) userIds[i] = "'" + userIds[i] + "'";

        String formattedIds = TextUtils.join(COMMA_DELIMITER, userIds);
        String sql = String.format(MULTI_DELETION_STATEMENT, teamId, formattedIds);

        int deleted = AppDatabase.getInstance().compileStatement(sql).executeUpdateDelete();
        if (DEV) Logger.log(getTableName(), "Deleted " + deleted + " rows");
    }

    @Query("SELECT * FROM join_requests as request" +
            " WHERE :teamId = join_request_team" +
            " AND join_request_created < :date" +
            " ORDER BY join_request_created DESC" +
            " LIMIT :limit")
    public abstract Maybe<List<JoinRequest>> getRequests(String teamId, Date date, int limit);
}
