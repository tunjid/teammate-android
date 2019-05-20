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

import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Event;

import java.util.Date;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public abstract class ChatDao extends EntityDao<Chat> {

    @Override
    protected String getTableName() {
        return "team_chats";
    }

    @Query("SELECT * FROM team_chats" +
            " WHERE team_chat_team = :teamId" +
            " AND team_chat_created < :date" +
            " ORDER BY team_chat_created DESC" +
            " LIMIT :limit")
    public abstract Maybe<List<Chat>> chatsBefore(String teamId, Date date, int limit);

    @Query("SELECT * FROM team_chats" +
            " WHERE team_chat_team = :teamId" +
            " AND team_chat_created > :date" +
            " ORDER BY team_chat_created DESC" +
            " LIMIT 10")
    public abstract Maybe<List<Chat>> unreadChats(String teamId, Date date);

    @Query("SELECT * FROM team_chats" +
            " WHERE :id = team_chat_id")
    public abstract Maybe<Chat> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<Chat> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<Chat> teams);

    @Delete
    public abstract void delete(Chat chat);
}
