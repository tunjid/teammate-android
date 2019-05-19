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

import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link User}
 */

@Dao
public abstract class MediaDao extends EntityDao<Media> {

    @Override
    protected String getTableName() {
        return "team_media";
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<Media> roles);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<Media> roles);

    @Delete
    public abstract void delete(List<Media> roles);

    @Query("SELECT *" +
            " FROM team_media" +
            " WHERE :id = media_id")
    public abstract Maybe<Media> get(String id);

    @Query("SELECT *" +
            " FROM team_media" +
            " WHERE :team = media_team" +
            " AND media_created < :date" +
            " AND media_flagged = 0" +
            " ORDER BY media_created DESC" +
            " LIMIT :limit")
    public abstract Maybe<List<Media>> getTeamMedia(Team team, Date date, int limit);
}
