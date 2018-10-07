package com.mainstreetcode.teammate;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.migrations.Migration1To2;
import com.mainstreetcode.teammate.persistence.migrations.Migration2To3;
import com.mainstreetcode.teammate.persistence.migrations.Migration3To4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class MigrationTest {

    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper;

    public MigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                AppDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrate1To2() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        //db.execSQL(...);

        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        helper.runMigrationsAndValidate(TEST_DB, 2, true, new Migration1To2());

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }

    @Test
    public void migrate2To3() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 2);
        db.close();
        helper.runMigrationsAndValidate(TEST_DB, 3, true, new Migration2To3());
    }

    @Test
    public void migrate3To4() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 3);
        db.close();
        helper.runMigrationsAndValidate(TEST_DB, 4, true, new Migration3To4());
    }


//    @Test
//    public void migrationFrom2To3_containsCorrectData() throws
//            IOException {
//        // Create the database in version 2
//        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 2);
//        // Insert some data
//        insertUser(USER.getId(), USER.getUserName(), db);
//        //Prepare for the next version
//        db.close();
//
//        // Re-open the database with version 3 and provide MIGRATION_1_2
//        // and MIGRATION_2_3 as the migration process.
//        helper.runMigrationsAndValidate(TEST_DB, 3, validateDroppedTables, MIGRATION_1_2, MIGRATION_2_3);
//
//        // MigrationTestHelper automatically verifies the schema
//        //changes, but not the data validity
//        // Validate that the data was migrated properly.
//        User dbUser = getMigratedRoomDatabase().userDao().getUser();
//        assertEquals(dbUser.getId(), USER.getId());
//        assertEquals(dbUser.getUserName(), USER.getUserName());
//        // The date was missing in version 2, so it should be null in
//        //version 3
//        assertEquals(dbUser.getDate(), null);
//    }
}
