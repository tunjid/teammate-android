package com.mainstreetcode.teammate.persistence.migrations;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

public class Migration12 extends Migration {

    public Migration12() {
        super(1, 2);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE users ADD COLUMN user_about TEXT DEFAULT ''");

        database.execSQL("ALTER TABLE teams ADD COLUMN team_sport TEXT DEFAULT ''");
        database.execSQL("ALTER TABLE teams ADD COLUMN team_description TEXT DEFAULT ''");
        database.execSQL("ALTER TABLE teams ADD COLUMN team_min_age INTEGER NOT NULL DEFAULT 0");
        database.execSQL("ALTER TABLE teams ADD COLUMN team_max_age INTEGER NOT NULL DEFAULT 0");

        database.execSQL("ALTER TABLE events ADD COLUMN event_visibility TEXT DEFAULT ''");
    }
}
