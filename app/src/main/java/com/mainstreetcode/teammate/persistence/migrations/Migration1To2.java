package com.mainstreetcode.teammate.persistence.migrations;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;
import androidx.annotation.NonNull;

import java.util.Date;

public class Migration1To2 extends Migration {

    public Migration1To2() {
        super(1, 2);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        long now = new Date().getTime();

        database.execSQL("ALTER TABLE users ADD COLUMN user_about TEXT DEFAULT ''");

        database.execSQL("ALTER TABLE teams ADD COLUMN team_sport TEXT DEFAULT ''");
        database.execSQL("ALTER TABLE teams ADD COLUMN team_description TEXT DEFAULT ''");
        database.execSQL("ALTER TABLE teams ADD COLUMN team_min_age INTEGER NOT NULL DEFAULT 0");
        database.execSQL("ALTER TABLE teams ADD COLUMN team_max_age INTEGER NOT NULL DEFAULT 0");

        database.execSQL("ALTER TABLE roles ADD COLUMN role_nickname TEXT DEFAULT ''");
        database.execSQL("ALTER TABLE roles ADD COLUMN role_created INTEGER DEFAULT " + now);

        database.execSQL("ALTER TABLE join_requests ADD COLUMN join_request_created INTEGER DEFAULT " + now);

        database.execSQL("ALTER TABLE events ADD COLUMN event_visibility TEXT DEFAULT ''");

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
        );
    }
}
