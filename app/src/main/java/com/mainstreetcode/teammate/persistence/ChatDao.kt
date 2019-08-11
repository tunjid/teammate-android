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
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.persistence.entity.ChatEntity
import io.reactivex.Maybe
import java.util.*

/**
 * DAO for [Chat]
 */

@Dao
abstract class ChatDao : EntityDao<ChatEntity>() {

    override val tableName: String
        get() = "team_chats"

    @Query("SELECT * FROM team_chats" +
            " WHERE team_chat_team = :teamId" +
            " AND team_chat_created < :date" +
            " ORDER BY team_chat_created DESC" +
            " LIMIT :limit")
    abstract fun chatsBefore(teamId: String, date: Date, limit: Int): Maybe<List<Chat>>

    @Query("SELECT * FROM team_chats" +
            " WHERE team_chat_team = :teamId" +
            " AND team_chat_created > :date" +
            " ORDER BY team_chat_created DESC" +
            " LIMIT 10")
    abstract fun unreadChats(teamId: String, date: Date): Maybe<List<Chat>>

    @Query("SELECT * FROM team_chats" + " WHERE :id = team_chat_id")
    abstract operator fun get(id: String): Maybe<Chat>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun insert(models: List<ChatEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract override fun update(models: List<ChatEntity>)

    @Delete
    abstract override fun delete(model: ChatEntity)
}
