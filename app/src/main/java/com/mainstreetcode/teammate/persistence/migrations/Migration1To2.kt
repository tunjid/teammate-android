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

package com.mainstreetcode.teammate.persistence.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

import java.util.Date

class Migration1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val now = Date().time

        database.execSQL("ALTER TABLE users ADD COLUMN user_about TEXT DEFAULT ''")

        database.execSQL("ALTER TABLE teams ADD COLUMN team_sport TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE teams ADD COLUMN team_description TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE teams ADD COLUMN team_min_age INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE teams ADD COLUMN team_max_age INTEGER NOT NULL DEFAULT 0")

        database.execSQL("ALTER TABLE roles ADD COLUMN role_nickname TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE roles ADD COLUMN role_created INTEGER DEFAULT $now")

        database.execSQL("ALTER TABLE join_requests ADD COLUMN join_request_created INTEGER DEFAULT $now")

        database.execSQL("ALTER TABLE events ADD COLUMN event_visibility TEXT DEFAULT ''")

        database.execSQL("CREATE TABLE IF NOT EXISTS `guests` " +
                "(`guest_id` TEXT NOT NULL, " +
                "`guest_user` TEXT, " +
                "`guest_event` TEXT, " +
                "`guest_created` INTEGER, " +
                "`guest_attending` INTEGER NOT NULL, " +
                "PRIMARY KEY(`guest_id`), " +
                "FOREIGN KEY(`guest_user`) " +
                "REFERENCES `users`(`user_id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`guest_event`) " +
                "REFERENCES `events`(`event_id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
    }
}
