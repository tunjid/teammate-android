package com.mainstreetcode.teammate.persistence.migrations;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import static com.mainstreetcode.teammate.model.Event.DEFAULT_NUM_SPOTS;

public class Migration2To3 extends Migration {

    public Migration2To3() {
        super(2, 3);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE events ADD COLUMN event_spots INTEGER NOT NULL DEFAULT " + DEFAULT_NUM_SPOTS);
    }
}
